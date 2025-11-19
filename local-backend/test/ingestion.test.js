const fs = require('fs');
const os = require('os');
const path = require('path');
const { createDatabase } = require('../src/db');
const { loadData } = require('../src/ingest/loadData');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_sample.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

function createTempDbPath() {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cherry-picker-db-'));
  return path.join(tmpDir, 'test.sqlite');
}

describe('CSV ingestion pipeline', () => {
  it('loads cards and benefits with normalized categories', async () => {
    const dbPath = createTempDbPath();
    const db = createDatabase(dbPath);

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

    const cardCount = db.prepare('SELECT COUNT(*) AS count FROM cards').get().count;
    const benefitCategories = db
      .prepare('SELECT DISTINCT normalized_category FROM card_benefits ORDER BY normalized_category')
      .all();
    const merchantCount = db.prepare('SELECT COUNT(*) AS count FROM merchants').get().count;

    expect(cardCount).toBe(2);
    expect(merchantCount).toBe(3);
    expect(benefitCategories.map((row) => row.normalized_category)).toEqual(['CAFE', 'DINING', 'SHOPPING']);

    db.close();
  });

  it('is idempotent when run multiple times', async () => {
    const dbPath = createTempDbPath();
    const db = createDatabase(dbPath);

    const params = {
      db,
      cardsCsvPath: cardsCsv,
      merchantsCsvPath: merchantsCsv,
      categoryMap: {
        외식: 'DINING',
        '카페/음료': 'CAFE',
        쇼핑: 'SHOPPING',
      },
    };

    const firstRun = await loadData(params);
    const secondRun = await loadData(params);

    const cardCount = db.prepare('SELECT COUNT(*) AS count FROM cards').get().count;
    const benefitCount = db.prepare('SELECT COUNT(*) AS count FROM card_benefits').get().count;
    const merchantCount = db.prepare('SELECT COUNT(*) AS count FROM merchants').get().count;
    const logs = db.prepare('SELECT COUNT(*) AS count FROM refresh_logs').get().count;

    expect(firstRun.cardsInserted).toBe(2);
    expect(secondRun.cardsInserted).toBe(2);
    expect(cardCount).toBe(2);
    expect(benefitCount).toBe(3);
    expect(merchantCount).toBe(3);
    expect(logs).toBe(2);

    db.close();
  });

  it('records mapping entries and refresh log metadata', async () => {
    const dbPath = createTempDbPath();
    const db = createDatabase(dbPath);
    const categoryMap = {
      외식: 'DINING',
      '카페/음료': 'CAFE',
      쇼핑: 'SHOPPING',
    };

    const summary = await loadData({
      db,
      cardsCsvPath: cardsCsv,
      merchantsCsvPath: merchantsCsv,
      categoryMap,
    });

    const log = db.prepare('SELECT * FROM refresh_logs ORDER BY id DESC LIMIT 1').get();
    const mappingCount = db.prepare('SELECT COUNT(*) AS count FROM category_mappings').get().count;

    expect(summary.mappingEntries).toBe(Object.keys(categoryMap).length);
    expect(log.cards_loaded).toBe(2);
    expect(log.merchants_loaded).toBe(3);
    expect(mappingCount).toBe(Object.keys(categoryMap).length);
    expect(log.completed_at).not.toBeNull();
    expect(log.error).toBeNull();

    db.close();
  });
});
