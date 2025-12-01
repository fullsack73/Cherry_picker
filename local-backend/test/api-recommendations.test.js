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

  beforeEach(() => {
    app.recommendationEngine?.clearCache();
  });

  function getCardId(cardName) {
    return db.prepare('SELECT id FROM cards WHERE name = ?').get(cardName)?.id;
  }

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

  it('prioritizes owned cards for a selected store', async () => {
    const alphaId = getCardId('추천카드 ALPHA');

    const response = await request(app)
      .post('/api/recommendations')
      .send({
        storeId: 42,
        storeName: '테스트식당',
        storeCategory: 'DINING',
        ownedCardIds: [alphaId],
        limit: 3,
      });

    expect(response.status).toBe(200);
    expect(response.body.meta.storeId).toBe(42);
    expect(response.body.data[0]).toMatchObject({
      cardId: alphaId,
      scoreSource: 'location',
    });
    expect(response.body.meta.scoreSources.location).toBeGreaterThan(0);
  });

  it('excludes owned cards when discover mode is enabled', async () => {
    const alphaId = getCardId('추천카드 ALPHA');

    const response = await request(app)
      .post('/api/recommendations')
      .send({
        storeId: 43,
        storeName: '테스트카페',
        storeCategory: 'CAFE',
        ownedCardIds: [alphaId],
        discover: true,
        limit: 5,
      });

    expect(response.status).toBe(200);
    expect(response.body.meta.discover).toBe(true);
    const cardIds = response.body.data.map((card) => card.cardId);
    expect(cardIds).not.toContain(alphaId);
    expect(cardIds.length).toBeGreaterThan(0);
    expect(response.body.meta.scoreSources.llm).toBe(0);
  });

  it('surfaces fallback metadata when Gemini scoring times out', async () => {
    const gammaId = getCardId('추천카드 GAMMA');
    const originalGeminiClient = app.recommendationEngine.geminiClient;
    const failingClient = {
      scoreCard: jest.fn().mockRejectedValue(new Error('timeout')),
    };
    app.recommendationEngine.geminiClient = failingClient;

    try {
      const response = await request(app)
        .post('/api/recommendations')
        .send({
          storeId: 44,
          storeName: 'Fallback Bistro',
          storeCategory: 'DINING',
          ownedCardIds: [gammaId],
          limit: 4,
        });

      expect(response.status).toBe(200);
      expect(failingClient.scoreCard).toHaveBeenCalled();
      expect(response.body.data[0].scoreSource).toBe('fallback');
      expect(response.body.meta.scoreSources.fallback).toBeGreaterThan(0);
    } finally {
      app.recommendationEngine.geminiClient = originalGeminiClient;
    }
  });
});
