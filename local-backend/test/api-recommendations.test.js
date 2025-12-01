const path = require('path');
const request = require('supertest');
const app = require('../index');
const { loadData } = require('../src/ingest/loadData');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_location_variants.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

const db = app.db;

async function seedData() {
  await loadData({
    db,
    cardsCsvPath: cardsCsv,
    merchantsCsvPath: merchantsCsv,
    categoryMap: {
      외식: 'DINING',
      '카페/음료': 'CAFE',
      쇼핑: 'SHOPPING',
    },
  });
  app.recommendationEngine?.clearCache();
}

describe('POST /api/recommendations', () => {
  beforeAll(async () => {
    await seedData();
  });

  it('returns validation errors for malformed payloads', async () => {
    const response = await request(app)
      .post('/api/recommendations')
      .send({ storeId: 1, storeCategory: 'DINING' });

    expect(response.status).toBe(400);
    expect(response.body.error).toMatchObject({
      code: 'INVALID_STORE_NAME',
      message: expect.any(String),
    });
  });

  it('responds with recommendation data and rate limit headers', async () => {
    const alphaId = db.prepare('SELECT id FROM cards WHERE name = ?').get('추천카드 ALPHA').id;

    const response = await request(app)
      .post('/api/recommendations')
      .send({
        storeId: 10,
        storeName: '테스트식당',
        storeCategory: 'DINING',
        ownedCardIds: [alphaId],
        limit: 5,
      });

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
    expect(response.body.meta).toMatchObject({
      discover: false,
      limit: 5,
    });
    expect(response.headers['x-ratelimit-limit']).toBe('60');
    expect(response.headers['x-ratelimit-remaining']).toBe('59');
  });
});
