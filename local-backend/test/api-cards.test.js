const path = require('path');
const request = require('supertest');
const app = require('../index');
const { loadData } = require('../src/ingest/loadData');

const db = app.db;

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

describe('Cards API', () => {
  let seededCardId;

  beforeAll(async () => {
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

    const row = db.prepare('SELECT id FROM cards ORDER BY id ASC LIMIT 1').get();
    seededCardId = row.id;
  });

  it('returns paginated cards with metadata', async () => {
    const response = await request(app).get('/api/cards');

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
    expect(response.body.meta).toMatchObject({
      total: expect.any(Number),
      limit: expect.any(Number),
      offset: expect.any(Number),
    });
    expect(response.body.meta.lastRefreshedAt).toEqual(expect.any(String));
    expect(response.body.meta.dataSource).toEqual(expect.any(String));
  });

  it('supports normalized category filtering', async () => {
    const response = await request(app).get('/api/cards').query({ category: 'dining' });

    expect(response.status).toBe(200);
    expect(response.body.meta.total).toBe(1);
    expect(response.body.data).toHaveLength(1);
    expect(response.body.data[0].normalizedCategories).toContain('DINING');
  });

  it('rejects invalid pagination parameters', async () => {
    const response = await request(app).get('/api/cards').query({ limit: 'abc' });

    expect(response.status).toBe(400);
    expect(response.body.error).toMatchObject({
      code: 'INVALID_LIMIT',
      message: expect.any(String),
    });
  });

  it('returns ordered benefits for a card', async () => {
    const response = await request(app).get(`/api/cards/${seededCardId}/benefits`);

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
    expect(response.body.data.length).toBeGreaterThan(0);
    expect(response.body.data[0]).toHaveProperty('normalized_category');
  });

  it('returns 404 when card is not found', async () => {
    const response = await request(app).get('/api/cards/9999/benefits');

    expect(response.status).toBe(404);
    expect(response.body.error).toMatchObject({
      code: 'CARD_NOT_FOUND',
      message: expect.any(String),
    });
  });
});
