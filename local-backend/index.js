const express = require('express');
const fs = require('fs');
const path = require('path');
const os = require('os');
const cors = require('cors');
const { createDatabase } = require('./src/db');
const { loadData } = require('./src/ingest/loadData');
const {
  fetchPaginatedCards,
  fetchCardBenefits,
  fetchLatestRefreshMetadata,
} = require('./src/queries/cards');
const { fetchNearbyStores, searchStoresByKeyword } = require('./src/queries/stores');
const { RecommendationEngine, DEFAULT_LIMIT, MAX_LIMIT } = require('./src/recommendations/recommendationEngine');
const { GeminiClient } = require('./src/recommendations/geminiClient');

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

const runtimeDbPath = process.env.DB_PATH || (process.env.NODE_ENV === 'test'
  ? path.join(os.tmpdir(), `cherry-picker-test-${process.pid}.sqlite`)
  : undefined);

const db = createDatabase(runtimeDbPath);
const shouldBootstrapData = process.env.SKIP_DATA_BOOTSTRAP === '1'
  ? false
  : process.env.NODE_ENV !== 'test';

const bootstrapPromise = (shouldBootstrapData ? loadData({ db }) : Promise.resolve()).catch((error) => {
  console.error('Failed to bootstrap data store:', error);
  if (process.env.NODE_ENV !== 'test') {
    process.exit(1);
  }
});

const geminiClient = new GeminiClient();
const recommendationEngine = new RecommendationEngine({ db, geminiClient });

const stores = [
  { id: 1, name: 'Store A', latitude: 34.0522, longitude: -118.2437 },
  { id: 2, name: 'Store B', latitude: 34.0525, longitude: -118.2440 },
  { id: 3, name: 'Store C', latitude: 34.0530, longitude: -118.2435 },
];

function sendError(res, status, code, message) {
  return res.status(status).json({ error: { code, message } });
}

function parseNonNegativeInteger(value) {
  if (value === undefined) {
    return { ok: true, value: undefined };
  }

  const parsed = Number(value);

  if (!Number.isInteger(parsed) || parsed < 0) {
    return { ok: false };
  }

  return { ok: true, value: parsed };
}

function validateRecommendationPayload(body) {
  if (!body || typeof body !== 'object') {
    return { ok: false, code: 'INVALID_PAYLOAD', message: 'Request body must be a JSON object' };
  }

  const storeId = Number(body.storeId);
  if (!Number.isInteger(storeId) || storeId <= 0) {
    return { ok: false, code: 'INVALID_STORE_ID', message: 'storeId must be a positive integer' };
  }

  const storeName = typeof body.storeName === 'string' ? body.storeName.trim() : '';
  if (!storeName) {
    return { ok: false, code: 'INVALID_STORE_NAME', message: 'storeName is required' };
  }

  const storeCategory = typeof body.storeCategory === 'string' ? body.storeCategory.trim() : '';
  if (!storeCategory) {
    return { ok: false, code: 'INVALID_STORE_CATEGORY', message: 'storeCategory is required' };
  }

  if (body.discover !== undefined && typeof body.discover !== 'boolean') {
    return { ok: false, code: 'INVALID_DISCOVER_FLAG', message: 'discover must be a boolean value' };
  }

  let limit = DEFAULT_LIMIT;
  if (body.limit !== undefined) {
    const parsedLimit = Number(body.limit);
    if (!Number.isInteger(parsedLimit) || parsedLimit <= 0) {
      return { ok: false, code: 'INVALID_LIMIT', message: 'limit must be a positive integer' };
    }
    limit = Math.min(parsedLimit, MAX_LIMIT);
  }

  let ownedCardIds = [];
  if (body.ownedCardIds !== undefined) {
    if (!Array.isArray(body.ownedCardIds)) {
      return { ok: false, code: 'INVALID_OWNED_CARDS', message: 'ownedCardIds must be an array of integers' };
    }
    ownedCardIds = body.ownedCardIds.map((value) => Number(value)).filter((value) => Number.isInteger(value) && value > 0);
    if (body.ownedCardIds.length > 0 && ownedCardIds.length === 0) {
      return { ok: false, code: 'INVALID_OWNED_CARDS', message: 'ownedCardIds must contain positive integers' };
    }
  }

  const locationKeywords = Array.isArray(body.locationKeywords)
    ? body.locationKeywords
        .map((keyword) => (typeof keyword === 'string' ? keyword.trim() : ''))
        .filter((keyword) => keyword.length > 0)
    : [];

  return {
    ok: true,
    value: {
      storeId,
      storeName,
      storeCategory,
      ownedCardIds,
      discover: Boolean(body.discover),
      locationKeywords,
      limit,
    },
  };
}

app.post('/api/location', (req, res) => {
  const { latitude, longitude } = req.body;

  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    return res.status(400).json({ error: 'Invalid latitude or longitude' });
  }

  console.log(`Received location: latitude=${latitude}, longitude=${longitude}`);

  const locationData = `Latitude: ${latitude}, Longitude: ${longitude}\n`;
  fs.appendFile(path.join(__dirname, 'location_data.txt'), locationData, (err) => {
    if (err) {
      console.error('Failed to write to file:', err);
      return res.status(500).json({ error: 'Failed to save location data' });
    }
  });

  res.status(200).json({ status: 'success' });
});

app.get('/stores', (req, res) => {
  res.json(stores);
});

app.get('/api/cards', (req, res) => {
  const { limit: limitParam, offset: offsetParam, category } = req.query;

  const limitResult = parseNonNegativeInteger(limitParam);
  if (!limitResult.ok) {
    return sendError(res, 400, 'INVALID_LIMIT', 'limit must be a non-negative integer');
  }

  const offsetResult = parseNonNegativeInteger(offsetParam);
  if (!offsetResult.ok) {
    return sendError(res, 400, 'INVALID_OFFSET', 'offset must be a non-negative integer');
  }

  let normalizedCategory;
  if (category !== undefined) {
    if (typeof category !== 'string' || category.trim().length === 0) {
      return sendError(res, 400, 'INVALID_CATEGORY', 'category must be a non-empty string when provided');
    }
    normalizedCategory = category.trim().toUpperCase();
  }

  const { cards: data, total, limit, offset } = fetchPaginatedCards(db, {
    limit: limitResult.value,
    offset: offsetResult.value,
    normalizedCategory,
  });

  const metadata = fetchLatestRefreshMetadata(db);

  return res.json({
    data,
    meta: {
      total,
      limit,
      offset,
      lastRefreshedAt: metadata.lastRefreshedAt,
      dataSource: metadata.dataSource,
    },
  });
});

app.get('/api/cards/:cardId/benefits', (req, res) => {
  const cardId = Number(req.params.cardId);

  if (!Number.isInteger(cardId) || cardId <= 0) {
    return sendError(res, 400, 'INVALID_CARD_ID', 'cardId must be a positive integer');
  }

  const cardExists = db.prepare('SELECT 1 FROM cards WHERE id = ? LIMIT 1').get(cardId);
  if (!cardExists) {
    return sendError(res, 404, 'CARD_NOT_FOUND', 'Card not found');
  }

  const benefits = fetchCardBenefits(db, cardId);

  return res.json({ data: benefits });
});

app.get('/api/stores/nearby', (req, res) => {
  const { latitude, longitude, radius, categories } = req.query;

  const lat = Number(latitude);
  const lon = Number(longitude);

  if (isNaN(lat) || isNaN(lon)) {
    return sendError(res, 400, 'INVALID_LOCATION', 'Latitude and longitude are required and must be numbers');
  }

  let parsedRadius = 500;
  if (radius !== undefined) {
    const r = Number(radius);
    if (!isNaN(r) && r > 0) {
      parsedRadius = r;
    }
  }

  let parsedCategories = [];
  if (categories) {
    if (typeof categories === 'string') {
      parsedCategories = categories.split(',').map(c => c.trim()).filter(c => c.length > 0);
    } else if (Array.isArray(categories)) {
      parsedCategories = categories;
    }
  }

  try {
    const stores = fetchNearbyStores(db, {
      latitude: lat,
      longitude: lon,
      radius: parsedRadius,
      categories: parsedCategories
    });

    res.json({ data: stores });
  } catch (error) {
    console.error('Error fetching nearby stores:', error);
    sendError(res, 500, 'INTERNAL_ERROR', 'Failed to fetch nearby stores');
  }
});

app.get('/api/stores/search', (req, res) => {
  const queryParam = typeof req.query.query === 'string' ? req.query.query : req.query.q;
  const normalizedQuery = typeof queryParam === 'string' ? queryParam.trim() : '';

  if (!normalizedQuery) {
    return sendError(res, 400, 'INVALID_QUERY', 'query parameter is required');
  }

  let limit;
  if (req.query.limit !== undefined) {
    const parsed = Number(req.query.limit);
    if (Number.isInteger(parsed) && parsed > 0) {
      limit = Math.min(parsed, 50);
    }
  }

  try {
    const stores = searchStoresByKeyword(db, {
      query: normalizedQuery,
      limit,
    });
    res.json({ data: stores });
  } catch (error) {
    console.error('Error searching stores:', error);
    sendError(res, 500, 'INTERNAL_ERROR', 'Failed to search stores');
  }
});

app.post('/api/recommendations', async (req, res) => {
  const validation = validateRecommendationPayload(req.body);
  if (!validation.ok) {
    return sendError(res, 400, validation.code, validation.message);
  }

  try {
    const result = await recommendationEngine.getRecommendations(validation.value);
    res.set({
      'X-RateLimit-Limit': '60',
      'X-RateLimit-Remaining': '59',
      'X-RateLimit-Reset': String(Math.floor(Date.now() / 1000) + 60),
    });
    return res.json(result);
  } catch (error) {
    console.error('Failed to generate recommendations:', error);
    return sendError(res, 500, 'RECOMMENDATION_FAILED', 'Unable to generate recommendations at this time');
  }
});

if (process.env.NODE_ENV !== 'test') {
  bootstrapPromise.then(() => {
    app.listen(port, () => {
      console.log(`Server listening at http://localhost:${port}`);
    });
  });
}

module.exports = app;
module.exports.db = db;
module.exports.bootstrapPromise = bootstrapPromise;
module.exports.recommendationEngine = recommendationEngine;


