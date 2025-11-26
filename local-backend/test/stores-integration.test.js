const request = require('supertest');
const path = require('path');
const app = require('../index');
const { loadData } = require('../src/ingest/loadData');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

describe('Stores Integration & Edge Cases', () => {
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

  it('should handle multiple categories in filter', async () => {
    // Assuming the API supports comma-separated categories
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        categories: 'DINING,CAFE'
      });

    expect(response.status).toBe(200);
    const categories = response.body.data.map(s => s.normalized_category);
    // Should contain both if available in fixtures
    // In fixtures: '테스트식당' (DINING), '테스트카페' (CAFE)
    expect(categories).toContain('DINING');
    expect(categories).toContain('CAFE');
  });

  it('should ignore unknown categories but still return results for valid ones', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        categories: 'DINING,UNKNOWN_CAT'
      });

    expect(response.status).toBe(200);
    const categories = response.body.data.map(s => s.normalized_category);
    expect(categories).toContain('DINING');
  });

  it('should return empty list if category does not exist', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        categories: 'NON_EXISTENT'
      });

    expect(response.status).toBe(200);
    expect(response.body.data).toEqual([]);
  });

  it('should handle large radius values (e.g. cap at max allowed or just work)', async () => {
    const response = await request(app)
      .get('/api/stores/nearby')
      .query({
        latitude: 37.5665,
        longitude: 126.9780,
        radius: 10000 // 10km
      });

    expect(response.status).toBe(200);
    expect(response.body.data.length).toBeGreaterThan(0);
  });
});
