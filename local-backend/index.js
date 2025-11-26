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
const { fetchNearbyStores } = require('./src/queries/stores');

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


