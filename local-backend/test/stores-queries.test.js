const fs = require('fs');
const os = require('os');
const path = require('path');
const { createDatabase } = require('../src/db');
const { loadData } = require('../src/ingest/loadData');
const { fetchNearbyStores } = require('../src/queries/stores');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

function createTempDb() {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'stores-query-'));
  const dbPath = path.join(tmpDir, 'test.sqlite');
  return createDatabase(dbPath);
}

describe('Store query utilities', () => {
  let db;

  beforeAll(async () => {
    db = createTempDb();
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
  });

  afterAll(() => {
    if (db && db.open) {
      db.close();
    }
  });

  it('finds stores within a simple radius', () => {
    // Center near City Hall (near '테스트식당' and '테스트카페')
    const center = { latitude: 37.5665, longitude: 126.9780 };
    const radius = 500; // 500 meters

    const stores = fetchNearbyStores(db, { ...center, radius });

    // Should find '테스트식당' (0m) and '테스트카페' (approx 100m away)
    // Should NOT find '테스트샵' (Gangnam, ~8km away)
    expect(stores).toHaveLength(2);
    const names = stores.map(s => s.name);
    expect(names).toContain('테스트식당');
    expect(names).toContain('테스트카페');
    expect(names).not.toContain('테스트샵');
  });

  it('filters stores by category', () => {
    const center = { latitude: 37.5665, longitude: 126.9780 };
    const radius = 500;
    const categories = ['CAFE'];

    const stores = fetchNearbyStores(db, { ...center, radius, categories });

    expect(stores).toHaveLength(1);
    expect(stores[0].name).toBe('테스트카페');
    expect(stores[0].normalized_category).toBe('CAFE');
  });

  it('handles no stores found', () => {
    // Center far away
    const center = { latitude: 38.0000, longitude: 127.0000 };
    const radius = 500;

    const stores = fetchNearbyStores(db, { ...center, radius });

    expect(stores).toHaveLength(0);
  });

  it('handles invalid coordinates gracefully', () => {
    const stores = fetchNearbyStores(db, { latitude: 'invalid', longitude: 126.9780, radius: 500 });
    expect(stores).toHaveLength(0);
  });

  it('returns correct store data structure', () => {
    const center = { latitude: 37.5665, longitude: 126.9780 };
    const radius = 100;
    
    const stores = fetchNearbyStores(db, { ...center, radius });
    const store = stores.find(s => s.name === '테스트식당');

    expect(store).toMatchObject({
      id: expect.any(Number),
      name: '테스트식당',
      branch: '본점',
      address: '서울시 중구 테스트로 1',
      latitude: 37.5665,
      longitude: 126.9780,
      source_category: '외식',
      normalized_category: 'DINING'
    });
    // Check if distance is calculated and included (optional but good)
    expect(store.distance).toBeLessThan(10); // Should be very close to 0
  });
});
