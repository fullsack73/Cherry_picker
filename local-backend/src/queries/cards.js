const DEFAULT_LIMIT = 25;
const MAX_LIMIT = 100;

function requireDb(db) {
  if (!db) {
    throw new Error('Database instance is required');
  }
}

function coercePositiveInteger(value, defaultValue) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed < 0) {
    return defaultValue;
  }
  return Math.floor(parsed);
}

function normalizeLimit(limit) {
  const coerced = coercePositiveInteger(limit, DEFAULT_LIMIT);
  if (coerced === 0) {
    return DEFAULT_LIMIT;
  }
  return Math.min(coerced, MAX_LIMIT);
}

function fetchPaginatedCards(db, { limit, offset, normalizedCategory } = {}) {
  requireDb(db);

  const resolvedLimit = normalizeLimit(limit);
  const resolvedOffset = coercePositiveInteger(offset, 0);

  const params = {
    limit: resolvedLimit,
    offset: resolvedOffset,
  };

  const whereClause = normalizedCategory ? 'WHERE cb.normalized_category = @normalizedCategory' : '';
  if (normalizedCategory) {
    params.normalizedCategory = normalizedCategory;
  }

  const totalStatement = db.prepare(`
    SELECT COUNT(DISTINCT cards.id) AS total
    FROM cards
    LEFT JOIN card_benefits cb ON cb.card_id = cards.id
    ${whereClause}
  `);

  const totalResult = totalStatement.get(params);
  const total = totalResult ? totalResult.total : 0;

  if (total === 0) {
    return {
      cards: [],
      total: 0,
      limit: resolvedLimit,
      offset: resolvedOffset,
    };
  }

  const dataStatement = db.prepare(`
    SELECT DISTINCT cards.id, cards.name, cards.issuer
    FROM cards
    LEFT JOIN card_benefits cb ON cb.card_id = cards.id
    ${whereClause}
    ORDER BY cards.id ASC
    LIMIT @limit
    OFFSET @offset
  `);

  const rows = dataStatement.all(params);
  const cardIds = rows.map((row) => row.id);

  if (cardIds.length === 0) {
    return {
      cards: [],
      total,
      limit: resolvedLimit,
      offset: resolvedOffset,
    };
  }

  const placeholderKeys = cardIds.map((_, index) => `id${index}`);
  const placeholderMap = cardIds.reduce((acc, id, index) => {
    acc[placeholderKeys[index]] = id;
    return acc;
  }, {});

  const categoriesStatement = db.prepare(`
    SELECT card_id, normalized_category
    FROM card_benefits
    WHERE card_id IN (${placeholderKeys.map((key) => `@${key}`).join(', ')})
    GROUP BY card_id, normalized_category
    ORDER BY card_id ASC, normalized_category ASC
  `);

  const categoriesRows = categoriesStatement.all(placeholderMap);
  const categoriesByCardId = categoriesRows.reduce((acc, row) => {
    if (!acc.has(row.card_id)) {
      acc.set(row.card_id, []);
    }
    acc.get(row.card_id).push(row.normalized_category);
    return acc;
  }, new Map());

  const cards = rows.map((row) => ({
    id: row.id,
    name: row.name,
    issuer: row.issuer,
    normalizedCategories: categoriesByCardId.get(row.id) || [],
  }));

  return {
    cards,
    total,
    limit: resolvedLimit,
    offset: resolvedOffset,
  };
}

function fetchCardBenefits(db, cardId) {
  requireDb(db);
  const benefitsStatement = db.prepare(`
    SELECT id, card_id, description, keyword, source_category, normalized_category
    FROM card_benefits
    WHERE card_id = ?
    ORDER BY normalized_category ASC, id ASC
  `);

  return benefitsStatement.all(cardId);
}

function fetchLatestRefreshMetadata(db) {
  requireDb(db);
  const metadataStatement = db.prepare(`
    SELECT completed_at, mapping_hash
    FROM refresh_logs
    WHERE completed_at IS NOT NULL
    ORDER BY datetime(completed_at) DESC
    LIMIT 1
  `);

  const row = metadataStatement.get();
  if (!row) {
    return {
      lastRefreshedAt: null,
      dataSource: null,
    };
  }

  return {
    lastRefreshedAt: row.completed_at,
    dataSource: row.mapping_hash || null,
  };
}

module.exports = {
  fetchPaginatedCards,
  fetchCardBenefits,
  fetchLatestRefreshMetadata,
  DEFAULT_LIMIT,
  MAX_LIMIT,
};
