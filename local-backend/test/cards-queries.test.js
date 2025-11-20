const fs = require('fs');
const os = require('os');
const path = require('path');
const { createDatabase } = require('../src/db');
const { loadData } = require('../src/ingest/loadData');
const {
  fetchPaginatedCards,
  fetchCardBenefits,
  fetchLatestRefreshMetadata,
} = require('../src/queries/cards');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

function createTempDb() {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cards-query-'));
  const dbPath = path.join(tmpDir, 'test.sqlite');
  return createDatabase(dbPath);
}

describe('Card query utilities', () => {
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
    db.close();
  });

  it('fetches paginated cards and reports totals', () => {
    const { cards, total, limit, offset } = fetchPaginatedCards(db, { limit: 1, offset: 0 });

    expect(total).toBe(2);
    expect(limit).toBe(1);
    expect(offset).toBe(0);
    expect(cards).toHaveLength(1);
    expect(cards[0]).toMatchObject({
      id: expect.any(Number),
      name: expect.any(String),
      issuer: expect.any(String),
    });
    expect(Array.isArray(cards[0].normalizedCategories)).toBe(true);
  });

  it('filters cards by normalized category and returns ordered benefits', () => {
    const filtered = fetchPaginatedCards(db, { normalizedCategory: 'DINING' });

    expect(filtered.total).toBe(1);
    expect(filtered.cards).toHaveLength(1);
    expect(filtered.cards[0].normalizedCategories).toContain('DINING');

    const benefits = fetchCardBenefits(db, filtered.cards[0].id);
    expect(benefits.map((benefit) => benefit.normalized_category)).toEqual(['CAFE', 'DINING']);
    expect(fetchCardBenefits(db, 9999)).toEqual([]);
  });

  it('loads latest refresh metadata for response envelopes', () => {
    const metadata = fetchLatestRefreshMetadata(db);

    expect(metadata.lastRefreshedAt).toEqual(expect.any(String));
    expect(metadata.dataSource).toEqual(expect.any(String));
  });
});
