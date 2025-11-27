const request = require('supertest');
const path = require('path');
const app = require('../index');
const { loadData } = require('../src/ingest/loadData');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

beforeAll(async () => {
  await loadData({
    db: app.db,
    cardsCsvPath: cardsCsv,
    merchantsCsvPath: merchantsCsv,
    categoryMap: {
      외식: 'DINING',
      '카페/음료': 'CAFE',
      쇼핑: 'SHOPPING',
    },
  });
});

describe('GET /api/stores/nearby', () => {

  it('should return 200 and a list of stores with valid parameters', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        radius: 500
      });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('data');
    expect(Array.isArray(response.body.data)).toBe(true);
    // Based on fixtures, we expect '테스트식당' and '테스트카페'
    expect(response.body.data.length).toBeGreaterThanOrEqual(1);
    
    const store = response.body.data[0];
    expect(store).toHaveProperty('id');
    expect(store).toHaveProperty('name');
    expect(store).toHaveProperty('latitude');
    expect(store).toHaveProperty('longitude');
    expect(store).toHaveProperty('distance');
  });

  it('should default radius to 500m if not provided', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780
      });

    expect(response.status).toBe(200);
    expect(response.body.data.length).toBeGreaterThan(0);
  });

  it('should filter by categories', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        categories: 'CAFE'
      });

    expect(response.status).toBe(200);
    const stores = response.body.data;
    expect(stores.length).toBe(1);
    expect(stores[0].normalized_category).toBe('CAFE');
  });

  it('should return 400 if latitude or longitude is missing', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665
        // missing longitude
      });

    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('INVALID_LOCATION');
  });

  it('should return 400 if latitude or longitude is invalid', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 'invalid',
        longitude: 126.9780
      });

    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('INVALID_LOCATION');
  });

  it('should return empty list if no stores found', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 0,
        longitude: 0
      });

    expect(response.status).toBe(200);
    expect(response.body.data).toEqual([]);
  });
});

describe('GET /api/stores/search', () => {
  it('returns matches when query text exists', async () => {
    const response = await request(app)
      .get('/api/stores/search')
      .query({ query: '카페' });

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.data)).toBe(true);
    expect(response.body.data.length).toBeGreaterThanOrEqual(1);
    expect(response.body.data[0].name).toContain('카페');
  });

  it('honors the limit parameter', async () => {
    const response = await request(app)
      .get('/api/stores/search')
      .query({ query: '테스트', limit: 1 });

    expect(response.status).toBe(200);
    expect(response.body.data.length).toBeLessThanOrEqual(1);
  });

  it('returns 400 when query is missing', async () => {
    const response = await request(app)
      .get('/api/stores/search');

    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('INVALID_QUERY');
  });
});
