const { fetchPaginatedCards, fetchLocationPriorityBenefits } = require('../queries/cards');
const { GeminiClientError } = require('./geminiClient');

const MAX_LIMIT = 25;

/**
 * Generator that yields recommendation cards as they are scored.
 * Scores owned cards via LLM one-by-one and yields each result immediately.
 */
async function* streamRecommendations(
  { db, geminiClient, logger = console },
  params,
  options = {}
) {
  const { signal, requestId } = options;
  const normalizedCategory = normalizeCategory(params.storeCategory);
  const storeName = typeof params.storeName === 'string' ? params.storeName.trim() : '';
  const ownedCardIds = normalizeOwnedCardIds(params.ownedCardIds);
  const limit = Math.min(params.limit || 10, MAX_LIMIT);

  logger?.info?.('[stream] start', { requestId, ownedCardCount: ownedCardIds.length, limit });

  // Yield a heartbeat immediately so the client knows connection is alive
  yield { type: 'heartbeat', data: { requestId, timestamp: Date.now() } };

  // If user has owned cards, hydrate and score them
  let candidateCards = [];
  if (ownedCardIds.length > 0) {
    candidateCards = hydrateCardsByIds(db, ownedCardIds);
  }

  // If no owned cards or hydration returned nothing, use fallback
  if (candidateCards.length === 0) {
    const { cards } = fetchPaginatedCards(db, {
      normalizedCategory,
      limit: limit * 2,
    });
    candidateCards = cards;
  }

  ensureActive(signal);

  // Check for location-based benefits
  const locationBenefits = fetchLocationPriorityBenefits(db, {
    normalizedCategory,
    keywords: [storeName],
    locationOnly: true,
    limit: limit * 2,
  });

  const locationByCardId = new Map();
  locationBenefits.forEach((benefit) => {
    if (!locationByCardId.has(benefit.cardId)) {
      locationByCardId.set(benefit.cardId, benefit);
    }
  });

  const scoreSources = { location: 0, llm: 0, fallback: 0 };
  const yieldedCardIds = new Set();
  let llmSuppressed = false;
  let yieldedCount = 0;

  // First, yield location-based matches (instant, high confidence)
  for (const benefit of locationBenefits) {
    if (yieldedCount >= limit) break;
    ensureActive(signal);

    const card = candidateCards.find(c => c.id === benefit.cardId);
    if (!card || yieldedCardIds.has(card.id)) continue;

    yieldedCardIds.add(card.id);
    scoreSources.location += 1;
    yieldedCount += 1;

    yield {
      type: 'card',
      data: {
        cardId: card.id,
        cardName: card.name,
        issuer: card.issuer,
        normalizedCategories: card.normalizedCategories || [],
        score: 100,
        scoreSource: 'location',
        rationale: benefit.keyword
          ? `Matched keyword "${benefit.keyword}"`
          : 'Matched location-based benefit',
      },
    };
  }

  // Then score remaining cards via LLM (or fallback)
  for (const card of candidateCards) {
    if (yieldedCount >= limit) break;
    if (yieldedCardIds.has(card.id)) continue;
    ensureActive(signal);

    yieldedCardIds.add(card.id);

    // Try LLM scoring if available and not suppressed
    const llmEligible = Boolean(geminiClient) && !params.discover && !llmSuppressed;
    if (llmEligible) {
      try {
        const llmResult = await geminiClient.scoreCard({
          storeName,
          storeCategory: normalizedCategory || params.storeCategory,
          cardName: card.name,
          normalizedCategories: card.normalizedCategories || [],
          discover: false,
          signal,
        });

        const clampedScore = Math.max(0, Math.min(100, Math.round(llmResult.score)));
        scoreSources.llm += 1;
        yieldedCount += 1;

        yield {
          type: 'card',
          data: {
            cardId: card.id,
            cardName: card.name,
            issuer: card.issuer,
            normalizedCategories: card.normalizedCategories || [],
            score: clampedScore,
            scoreSource: 'llm',
            rationale: llmResult.rationale || 'LLM score applied.',
          },
        };
        continue;
      } catch (error) {
        if (error instanceof GeminiClientError && error.code === 'CANCELLED') {
          throw error;
        }
        if (['NETWORK', 'TIMEOUT', 'CONFIGURATION'].includes(error?.code)) {
          llmSuppressed = true;
          logger?.info?.('[stream] LLM suppressed', { requestId, code: error.code });
        }
      }
    }

    // Fallback scoring
    const fallbackScore = buildFallbackScore(card, normalizedCategory, storeName, params.discover);
    scoreSources.fallback += 1;
    yieldedCount += 1;

    yield {
      type: 'card',
      data: {
        cardId: card.id,
        cardName: card.name,
        issuer: card.issuer,
        normalizedCategories: card.normalizedCategories || [],
        score: fallbackScore.score,
        scoreSource: 'fallback',
        rationale: fallbackScore.rationale,
      },
    };
  }

  // Final "done" event with metadata
  yield {
    type: 'done',
    data: {
      total: yieldedCount,
      limit,
      discover: Boolean(params.discover),
      storeId: params.storeId || null,
      scoreSources,
    },
  };

  logger?.info?.('[stream] complete', { requestId, yieldedCount, scoreSources });
}

function normalizeCategory(category) {
  if (typeof category !== 'string') return null;
  const trimmed = category.trim();
  return trimmed ? trimmed.toUpperCase() : null;
}

function normalizeOwnedCardIds(rawIds) {
  if (!Array.isArray(rawIds)) return [];
  const seen = new Set();
  const result = [];
  rawIds.forEach((value) => {
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed <= 0 || seen.has(parsed)) return;
    seen.add(parsed);
    result.push(parsed);
  });
  return result;
}

function hydrateCardsByIds(db, cardIds) {
  if (cardIds.length === 0) return [];

  const placeholders = cardIds.map((_, i) => `@id${i}`).join(', ');
  const params = cardIds.reduce((acc, id, i) => {
    acc[`id${i}`] = id;
    return acc;
  }, {});

  const cards = db
    .prepare(`SELECT id, name, issuer FROM cards WHERE id IN (${placeholders})`)
    .all(params);

  if (cards.length === 0) return [];

  const categories = db
    .prepare(`
      SELECT card_id, normalized_category
      FROM card_benefits
      WHERE card_id IN (${placeholders})
      GROUP BY card_id, normalized_category
    `)
    .all(params);

  const categoriesByCardId = categories.reduce((acc, row) => {
    if (!acc.has(row.card_id)) acc.set(row.card_id, []);
    acc.get(row.card_id).push(row.normalized_category);
    return acc;
  }, new Map());

  const cardMap = new Map(cards.map((card) => [card.id, card]));

  return cardIds
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
}

function buildFallbackScore(card, normalizedCategory, storeName, discover) {
  let score = discover ? 55 : 45;
  if (normalizedCategory && card.normalizedCategories?.includes(normalizedCategory)) {
    score += 25;
  }
  score += Math.min((card.normalizedCategories?.length || 0) * 5, 15);
  score = Math.round(Math.max(25, Math.min(90, score)));

  const reasonParts = [];
  if (normalizedCategory) reasonParts.push(`Shares category ${normalizedCategory}`);
  if (storeName) reasonParts.push(`heuristic near ${storeName}`);
  if (reasonParts.length === 0) reasonParts.push('Heuristic fallback applied');

  return { score, rationale: reasonParts.join('; ') };
}

function ensureActive(signal) {
  if (signal?.aborted) {
    const error = new Error('Stream aborted');
    error.code = 'ABORTED';
    throw error;
  }
}

module.exports = { streamRecommendations };
