const fs = require('fs');
const os = require('os');
const path = require('path');
const { createDatabase } = require('../src/db');
const { loadData } = require('../src/ingest/loadData');
const { fetchLocationPriorityBenefits } = require('../src/queries/cards');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_location_variants.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

function createTempDbPath() {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'location-benefits-'));
  return path.join(tmpDir, 'test.sqlite');
}

function createCategoryMap() {
  return {
    외식: 'DINING',
    '카페/음료': 'CAFE',
    쇼핑: 'SHOPPING',
  };
}

async function seedDb(db) {
  await loadData({
    db,
    cardsCsvPath: cardsCsv,
    merchantsCsvPath: merchantsCsv,
    categoryMap: createCategoryMap(),
  });
}

describe('Location-based benefit schema upgrades', () => {
  let db;

  afterEach(() => {
    if (db) {
      db.close();
    }
  });

  it('defaults is_location_based to 0 when column value is omitted', () => {
    db = createDatabase(createTempDbPath());
    const cardId = db
      .prepare("INSERT INTO cards (name, issuer) VALUES ('Legacy Card', 'Legacy') RETURNING id")
      .get().id;

    const benefitId = db
      .prepare(`
        INSERT INTO card_benefits (card_id, description, keyword, source_category, normalized_category)
        VALUES (@card_id, 'Legacy benefit', '테스트식당', '외식', 'DINING')
        RETURNING id
      `)
      .get({ card_id: cardId }).id;

    const row = db.prepare('SELECT is_location_based FROM card_benefits WHERE id = ?').get(benefitId);
    expect(row.is_location_based).toBe(0);
  });

  it('parses CSV boolean variants into the is_location_based flag', async () => {
    db = createDatabase(createTempDbPath());
    await seedDb(db);

    const rows = db
      .prepare(`
        SELECT description, is_location_based
        FROM card_benefits
        ORDER BY description ASC
      `)
      .all();

    const flagByDescription = Object.fromEntries(rows.map((row) => [row.description, row.is_location_based]));

    expect(flagByDescription['외식 혜택 A']).toBe(1);
    expect(flagByDescription['외식 혜택 B']).toBe(0);
    expect(flagByDescription['외식 혜택 C']).toBe(1);
    expect(flagByDescription['쇼핑 혜택 D']).toBe(0);
    expect(flagByDescription['외식 혜택 E']).toBe(0);
    expect(flagByDescription['카페 혜택 F']).toBe(1);
  });

  it('orders benefits with location matches before others when filtering by category', async () => {
    db = createDatabase(createTempDbPath());
    await seedDb(db);

    const results = fetchLocationPriorityBenefits(db, { normalizedCategory: 'DINING', limit: 10 });
    const flags = results.map((result) => result.isLocationBased);

    expect(flags.slice(0, 2)).toEqual([true, true]);
    expect(flags.slice(2)).toEqual([false, false]);
  });

  it('filters to location-only keywords using case-insensitive matching', async () => {
    db = createDatabase(createTempDbPath());
    await seedDb(db);

    const matches = fetchLocationPriorityBenefits(db, {
      locationOnly: true,
      keywords: [' 테스트식당 '],
    });

    expect(matches).toHaveLength(2);
    expect(matches.every((match) => match.keyword === '테스트식당')).toBe(true);
    expect(matches.every((match) => match.isLocationBased)).toBe(true);
  });
});
