const { fetchPaginatedCards, fetchLocationPriorityBenefits } = require('../queries/cards');
const { GeminiClientError } = require('./geminiClient');

const DEFAULT_LIMIT = 10;
const MAX_LIMIT = 25;
const CACHE_TTL_MS = 5 * 60 * 1000;

class RecommendationAbortedError extends Error {
  constructor(message = 'Recommendation request aborted') {
    super(message);
    this.name = 'RecommendationAbortedError';
    this.code = 'ABORTED';
  }
}

class RecommendationEngine {
  constructor({ db, geminiClient, cacheTtlMs = CACHE_TTL_MS, logger = console, now = () => Date.now() } = {}) {
    if (!db) {
      throw new Error('RecommendationEngine requires a database connection');
    }
    this.db = db;
    this.geminiClient = geminiClient;
    this.cacheTtlMs = cacheTtlMs;
    this.logger = logger || console;
    this.now = now;
    this.cache = new Map();
  }

  clearCache() {
    this.cache.clear();
  }

  async getRecommendations(params = {}, options = {}) {
    const { signal, requestId } = options || {};
    this.ensureActive(signal);
    const resolvedLimit = this.normalizeLimit(params.limit);
    const normalizedParams = {
      ...params,
      limit: resolvedLimit,
      discover: Boolean(params.discover),
    };

    this.logStage('engine.start', requestId, { limit: resolvedLimit, discover: Boolean(params.discover) });

    if (typeof normalizedParams.storeName === 'string') {
      normalizedParams.storeName = normalizedParams.storeName.trim();
    }

    const cacheKey = this.buildCacheKey(normalizedParams);
    const cached = this.cache.get(cacheKey);
    const startedAt = this.now();

    if (cached && cached.expiresAt > startedAt) {
      this.ensureActive(signal);
      this.logStage('engine.cacheHit', requestId, { cachedAt: cached.expiresAt - this.cacheTtlMs });
      return this.decorateResult(cached.payload, startedAt, true);
    }

    let computed;
    try {
      this.logStage('engine.compute.start', requestId);
      computed = await this.computeRecommendations(normalizedParams, { signal, requestId });
      this.logStage('engine.compute.done', requestId, { total: computed.data.length });
    } catch (error) {
      if (error instanceof RecommendationAbortedError) {
        throw error;
      }
      if (error instanceof GeminiClientError && error.code === 'CANCELLED') {
        throw new RecommendationAbortedError();
      }
      this.logStage('engine.compute.error', requestId, { message: error.message });
      throw error;
    }
    this.ensureActive(signal);
    this.logStage('engine.cache.store', requestId, { expiresInMs: this.cacheTtlMs });
    this.cache.set(cacheKey, {
      payload: computed,
      expiresAt: startedAt + this.cacheTtlMs,
    });

    this.logStage('engine.finish', requestId, { cached: false });
    return this.decorateResult(computed, startedAt, false);
  }

  decorateResult(baseResult, startedAt, cached) {
    const latencyMs = Math.max(0, this.now() - startedAt);
    return {
      data: baseResult.data.map((entry) => ({ ...entry })),
      meta: {
        ...baseResult.meta,
        cached,
        latencyMs,
      },
    };
  }

  buildCacheKey(params) {
    const sortedOwned = this.sortIds(params.ownedCardIds || []);
    const normalizedCategory = this.normalizeCategory(params.storeCategory);
    const keywords = this.sanitizeKeywords(params.storeName, params.locationKeywords);

    return JSON.stringify({
      storeId: params.storeId || null,
      category: normalizedCategory,
      discover: Boolean(params.discover),
      owned: sortedOwned,
      keywords,
      limit: params.limit,
    });
  }

  sortIds(ids) {
    return [...new Set(ids.filter((id) => Number.isInteger(id) && id > 0))].sort((a, b) => a - b);
  }

  normalizeCategory(category) {
    if (typeof category !== 'string') {
      return null;
    }
    const trimmed = category.trim();
    return trimmed ? trimmed.toUpperCase() : null;
  }

  normalizeLimit(limit) {
    const parsed = Number(limit);
    if (Number.isInteger(parsed) && parsed > 0) {
      return Math.min(parsed, MAX_LIMIT);
    }
    return DEFAULT_LIMIT;
  }

  sanitizeKeywords(storeName, extraKeywords = []) {
    const pool = [];
    if (typeof storeName === 'string' && storeName.trim().length > 0) {
      pool.push(storeName);
    }
    if (Array.isArray(extraKeywords)) {
      pool.push(...extraKeywords);
    }

    const seen = new Set();
    const sanitized = [];
    pool.forEach((keyword) => {
      if (typeof keyword !== 'string') return;
      const normalized = keyword.trim().toLowerCase();
      if (!normalized || seen.has(normalized)) return;
      seen.add(normalized);
      sanitized.push(normalized);
    });

    return sanitized;
  }

  normalizeOwnedCardIds(rawIds) {
    if (!Array.isArray(rawIds)) {
      return [];
    }
    const seen = new Set();
    const result = [];
    rawIds.forEach((value) => {
      const parsed = Number(value);
      if (!Number.isInteger(parsed) || parsed <= 0 || seen.has(parsed)) {
        return;
      }
      seen.add(parsed);
      result.push(parsed);
    });
    return result;
  }

  async computeRecommendations(params, options = {}) {
    const { signal, requestId } = options || {};
    this.ensureActive(signal);
    const normalizedCategory = this.normalizeCategory(params.storeCategory);
    const storeName = typeof params.storeName === 'string' ? params.storeName.trim() : '';
    const ownedCardIds = this.normalizeOwnedCardIds(params.ownedCardIds);
    const keywords = this.sanitizeKeywords(storeName, params.locationKeywords);

    const candidateCards = await this.resolveCandidates({
      normalizedCategory,
      ownedCardIds,
      discover: params.discover,
      limit: params.limit,
    }, { requestId });
    this.ensureActive(signal);
    this.logStage('engine.candidates.resolved', requestId, { count: candidateCards.length });

    const cardMap = new Map(candidateCards.map((card) => [card.id, card]));
    const locationBenefits = fetchLocationPriorityBenefits(this.db, {
      normalizedCategory,
      keywords,
      locationOnly: true,
      limit: Math.min(params.limit * 2, MAX_LIMIT * 2),
    });
    this.logStage('engine.location.benefits', requestId, { count: locationBenefits.length });

    const locationByCardId = new Map();
    locationBenefits.forEach((benefit) => {
      if (!locationByCardId.has(benefit.cardId)) {
        locationByCardId.set(benefit.cardId, benefit);
      }
    });

    const missingLocationCardIds = locationBenefits
      .map((benefit) => benefit.cardId)
      .filter((cardId) => !cardMap.has(cardId));

    if (missingLocationCardIds.length > 0) {
      this.logStage('engine.location.hydrate.start', requestId, { missing: missingLocationCardIds.length });
      const hydrated = await this.hydrateCardsByIds(missingLocationCardIds, { requestId });
      this.ensureActive(signal);
      hydrated.forEach((card) => cardMap.set(card.id, card));
      this.logStage('engine.location.hydrate.done', requestId, { hydrated: hydrated.length });
    }

    const orderedCardIds = this.buildInitialOrder({
      locationBenefits,
      candidateCards,
    });
    this.logStage('engine.order.built', requestId, { total: orderedCardIds.length });

    if (orderedCardIds.length === 0) {
      this.logStage('engine.order.empty', requestId);
      const fallbacks = await this.fetchCategoryFallbackCards(normalizedCategory, params.limit, { requestId });
      this.ensureActive(signal);
      fallbacks.forEach((card) => {
        if (!cardMap.has(card.id)) {
          cardMap.set(card.id, card);
        }
        if (!orderedCardIds.includes(card.id)) {
          orderedCardIds.push(card.id);
        }
      });
    }

    const scored = await this.scoreCards({
      orderedCardIds,
      cardMap,
      locationByCardId,
      normalizedCategory,
      storeName,
      storeCategory: params.storeCategory,
      discover: params.discover,
    }, { signal, requestId });
    this.ensureActive(signal);
    this.logStage('engine.score.complete', requestId, {
      entries: scored.entries.length,
      llm: scored.scoreSources.llm,
      location: scored.scoreSources.location,
      fallback: scored.scoreSources.fallback,
    });

    const sorted = scored.entries.sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;
      const priorityDiff = this.sourcePriority(a.scoreSource) - this.sourcePriority(b.scoreSource);
      if (priorityDiff !== 0) return priorityDiff;
      return a.cardName.localeCompare(b.cardName, 'en');
    });

    const limited = sorted.slice(0, params.limit);

    return {
      data: limited,
      meta: {
        total: sorted.length,
        limit: params.limit,
        discover: params.discover,
        storeId: params.storeId || null,
        scoreSources: scored.scoreSources,
      },
    };
  }

  buildInitialOrder({ locationBenefits, candidateCards }) {
    const seen = new Set();
    const order = [];

    locationBenefits.forEach((benefit) => {
      if (seen.has(benefit.cardId)) return;
      seen.add(benefit.cardId);
      order.push(benefit.cardId);
    });

    candidateCards.forEach((card) => {
      if (seen.has(card.id)) return;
      seen.add(card.id);
      order.push(card.id);
    });

    return order;
  }

  async resolveCandidates({ normalizedCategory, ownedCardIds, discover, limit }, options = {}) {
    const { requestId } = options || {};
    this.logStage('engine.candidates.start', requestId, {
      discover,
      ownedCardCount: ownedCardIds.length,
      limit,
      normalizedCategory,
    });

    if (discover) {
      const cards = await this.fetchDiscoverCandidates({ normalizedCategory, ownedCardIds, limit }, { requestId });
      this.logStage('engine.candidates.discover', requestId, { count: cards.length });
      return cards;
    }

    if (ownedCardIds.length > 0) {
      const hydrated = await this.hydrateCardsByIds(ownedCardIds, { requestId });
      this.logStage('engine.candidates.hydrated', requestId, { count: hydrated.length });
      return hydrated;
    }

    const fallback = await this.fetchCategoryFallbackCards(normalizedCategory, limit, { requestId });
    this.logStage('engine.candidates.fallback', requestId, { count: fallback.length });
    return fallback;
  }

  async fetchDiscoverCandidates({ normalizedCategory, ownedCardIds, limit }, options = {}) {
    const { requestId } = options || {};
    this.logStage('engine.discover.start', requestId, { normalizedCategory, ownedCardCount: ownedCardIds.length });
    const ownedSet = new Set(ownedCardIds);
    const { cards } = fetchPaginatedCards(this.db, {
      normalizedCategory,
      limit: Math.min(limit * 2, MAX_LIMIT * 2),
    });

    const filtered = cards.filter((card) => !ownedSet.has(card.id));
    this.logStage('engine.discover.filtered', requestId, { total: cards.length, filtered: filtered.length });
    return filtered;
  }

  async fetchCategoryFallbackCards(normalizedCategory, limit, options = {}) {
    const { requestId } = options || {};
    this.logStage('engine.fallback.start', requestId, { normalizedCategory, limit });
    const { cards } = fetchPaginatedCards(this.db, {
      normalizedCategory,
      limit: Math.min(limit * 2, MAX_LIMIT * 2),
    });
    this.logStage('engine.fallback.done', requestId, { count: cards.length });
    return cards;
  }

  async hydrateCardsByIds(cardIds, options = {}) {
    const { requestId } = options || {};
    this.logStage('engine.hydrate.start', requestId, { requested: cardIds.length });
    const seen = new Set();
    const uniqueIds = [];
    cardIds.forEach((id) => {
      if (!Number.isInteger(id) || id <= 0 || seen.has(id)) {
        return;
      }
      seen.add(id);
      uniqueIds.push(id);
    });
    if (uniqueIds.length === 0) {
      this.logStage('engine.hydrate.skip', requestId, { reason: 'no_unique_ids' });
      return [];
    }

    const placeholders = uniqueIds.map((_, index) => `@id${index}`).join(', ');
    const params = uniqueIds.reduce((acc, id, index) => {
      acc[`id${index}`] = id;
      return acc;
    }, {});

    const cards = this.db
      .prepare(`SELECT id, name, issuer FROM cards WHERE id IN (${placeholders})`)
      .all(params);

    if (cards.length === 0) {
      this.logStage('engine.hydrate.empty', requestId, { uniqueIds: uniqueIds.length });
      return [];
    }

    const categories = this.db
      .prepare(`
        SELECT card_id, normalized_category
        FROM card_benefits
        WHERE card_id IN (${placeholders})
        GROUP BY card_id, normalized_category
      `)
      .all(params);

    const categoriesByCardId = categories.reduce((acc, row) => {
      if (!acc.has(row.card_id)) {
        acc.set(row.card_id, []);
      }
      acc.get(row.card_id).push(row.normalized_category);
      return acc;
    }, new Map());

    const cardMap = new Map(cards.map((card) => [card.id, card]));

    const result = uniqueIds
      .map((id) => {
        const card = cardMap.get(id);
        if (!card) return null;
        return {
          id: card.id,
          name: card.name,
          issuer: card.issuer,
          normalizedCategories: categoriesByCardId.get(card.id) || [],
        };
      })
      .filter(Boolean);
    this.logStage('engine.hydrate.done', requestId, { returned: result.length });
    return result;
  }

  sourcePriority(source) {
    switch (source) {
      case 'location':
        return 0;
      case 'llm':
        return 1;
      default:
        return 2;
    }
  }

  async scoreCards({
    orderedCardIds,
    cardMap,
    locationByCardId,
    normalizedCategory,
    storeName,
    storeCategory,
    discover,
  }, options = {}) {
    const { signal, requestId } = options || {};
    this.logStage('engine.score.start', requestId, { ordered: orderedCardIds.length });
    const scoreSources = { location: 0, llm: 0, fallback: 0 };
    const entries = [];
    let llmSuppressed = false;

    for (const cardId of orderedCardIds) {
      this.ensureActive(signal);
      const card = cardMap.get(cardId);
      if (!card) continue;

      if (locationByCardId.has(cardId)) {
        const benefit = locationByCardId.get(cardId);
        entries.push({
          cardId: card.id,
          cardName: card.name,
          issuer: card.issuer,
          normalizedCategories: card.normalizedCategories,
          score: 100,
          scoreSource: 'location',
          rationale: benefit.keyword
            ? `Matched keyword \"${benefit.keyword}\"`
            : 'Matched location-based benefit',
        });
        scoreSources.location += 1;
        continue;
      }

      const llmEligible = Boolean(this.geminiClient) && !discover && !llmSuppressed;
      if (llmEligible) {
        try {
          const llmResult = await this.geminiClient.scoreCard({
            storeName,
            storeCategory: normalizedCategory || storeCategory,
            cardName: card.name,
            normalizedCategories: card.normalizedCategories,
            discover,
            signal,
          });
          const clampedScore = Math.max(0, Math.min(100, Math.round(llmResult.score)));
          entries.push({
            cardId: card.id,
            cardName: card.name,
            issuer: card.issuer,
            normalizedCategories: card.normalizedCategories,
            score: clampedScore,
            scoreSource: 'llm',
            rationale: llmResult.rationale || 'LLM score applied.',
          });
          scoreSources.llm += 1;
          continue;
        } catch (error) {
          if (error instanceof GeminiClientError && error.code === 'CANCELLED') {
            throw error;
          }
          if (!(error instanceof GeminiClientError)) {
            this.logger?.warn?.('Unexpected Gemini error during scoring', { message: error.message });
          } else if (['NETWORK', 'TIMEOUT', 'CONFIGURATION'].includes(error.code)) {
            if (!llmSuppressed) {
              llmSuppressed = true;
              const logPayload = { code: error.code };
              if (requestId) {
                logPayload.requestId = requestId;
              }
              this.logger?.info?.('Gemini scoring disabled for current request', logPayload);
            }
          }
        }
      }

      const fallback = this.buildFallbackEntry({
        card,
        normalizedCategory,
        storeName,
        discover,
      });
      entries.push(fallback);
      scoreSources.fallback += 1;
    }
    this.logStage('engine.score.finish', requestId, { entries: entries.length });
    return { entries, scoreSources };
  }

  buildFallbackEntry({ card, normalizedCategory, storeName, discover }) {
    let score = discover ? 55 : 45;
    if (normalizedCategory && card.normalizedCategories.includes(normalizedCategory)) {
      score += 25;
    }
    score += Math.min(card.normalizedCategories.length * 5, 15);
    score = Math.round(Math.max(25, Math.min(90, score)));

    const reasonParts = [];
    if (normalizedCategory) {
      reasonParts.push(`Shares category ${normalizedCategory}`);
    }
    if (storeName) {
      reasonParts.push(`heuristic near ${storeName}`);
    }
    if (reasonParts.length === 0) {
      reasonParts.push('Heuristic fallback applied');
    }

    return {
      cardId: card.id,
      cardName: card.name,
      issuer: card.issuer,
      normalizedCategories: card.normalizedCategories,
      score: score,
      scoreSource: 'fallback',
      rationale: reasonParts.join('; '),
    };
  }

  ensureActive(signal) {
    if (signal?.aborted) {
      throw new RecommendationAbortedError();
    }
  }

  logStage(stage, requestId, extra = {}) {
    if (!this.logger) {
      return;
    }
    const payload = requestId ? { requestId, ...extra } : extra;
    if (typeof this.logger.debug === 'function') {
      this.logger.debug(`[recommendations] ${stage}`, payload);
      return;
    }
    if (typeof this.logger.info === 'function') {
      this.logger.info(`[recommendations] ${stage}`, payload);
      return;
    }
    if (typeof this.logger.log === 'function') {
      this.logger.log('[recommendations]', stage, payload);
    }
  }
}

module.exports = {
  RecommendationEngine,
  DEFAULT_LIMIT,
  MAX_LIMIT,
  RecommendationAbortedError,
};
