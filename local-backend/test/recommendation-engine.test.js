const fs = require('fs');
const os = require('os');
const path = require('path');
const { createDatabase } = require('../src/db');
const { loadData } = require('../src/ingest/loadData');
const { RecommendationEngine } = require('../src/recommendations/recommendationEngine');

const fixturesDir = path.join(__dirname, 'fixtures');
const cardsCsv = path.join(fixturesDir, 'cards_location_variants.csv');
const merchantsCsv = path.join(fixturesDir, 'merchants_sample.csv');

function createTempDbPath() {
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'recommendation-engine-'));
  return path.join(tmpDir, 'test.sqlite');
}

async function seedDatabase(db) {
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
}

function getCardId(db, cardName) {
  const row = db.prepare('SELECT id FROM cards WHERE name = ?').get(cardName);
  return row?.id;
}

describe('RecommendationEngine', () => {
  let db;

  afterEach(() => {
    if (db) {
      db.close();
    }
  });

  it('prioritizes location-based matches with top scores', async () => {
    db = createDatabase(createTempDbPath());
    await seedDatabase(db);

    const alphaId = getCardId(db, '추천카드 ALPHA');
    const engine = new RecommendationEngine({ db, geminiClient: null, cacheTtlMs: 60_000 });

    const response = await engine.getRecommendations({
      storeId: 1,
      storeName: '테스트식당',
      storeCategory: 'DINING',
      ownedCardIds: [alphaId],
      limit: 5,
    });

    expect(response.meta.cached).toBe(false);
    expect(response.data[0]).toMatchObject({
      cardId: alphaId,
      score: 100,
      scoreSource: 'location',
    });
  });

  it('applies Gemini scores when no location match exists', async () => {
    db = createDatabase(createTempDbPath());
    await seedDatabase(db);

    const gammaId = getCardId(db, '추천카드 GAMMA');
    const geminiMock = {
      scoreCard: jest.fn().mockResolvedValue({ score: 88, rationale: 'LLM confident' }),
    };
    const engine = new RecommendationEngine({
      db,
      geminiClient: geminiMock,
      cacheTtlMs: 60_000,
      logger: { warn: jest.fn() },
    });

    const response = await engine.getRecommendations({
      storeId: 2,
      storeName: '테스트마켓',
      storeCategory: 'SHOPPING',
      ownedCardIds: [gammaId],
      limit: 5,
    });

    expect(geminiMock.scoreCard).toHaveBeenCalledTimes(1);
    expect(response.data[0]).toMatchObject({
      cardId: gammaId,
      score: 88,
      scoreSource: 'llm',
    });
  });

  it('falls back to heuristic scoring when Gemini fails', async () => {
    db = createDatabase(createTempDbPath());
    await seedDatabase(db);

    const deltaId = getCardId(db, '추천카드 DELTA');
    const geminiMock = {
      scoreCard: jest.fn().mockRejectedValue(new Error('network unavailable')),
    };
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
    const engine = new RecommendationEngine({ db, geminiClient: geminiMock, cacheTtlMs: 60_000 });

    const response = await engine.getRecommendations({
      storeId: 3,
      storeName: '도시락하우스',
      storeCategory: 'DINING',
      ownedCardIds: [deltaId],
      limit: 5,
    });

    expect(geminiMock.scoreCard).toHaveBeenCalledTimes(1);
    expect(response.data[0].scoreSource).toBe('fallback');
    expect(response.data[0].score).toBeGreaterThan(0);
    warnSpy.mockRestore();
  });

  it('caches identical requests for five minutes', async () => {
    db = createDatabase(createTempDbPath());
    await seedDatabase(db);

    const gammaId = getCardId(db, '추천카드 GAMMA');
    const geminiMock = {
      scoreCard: jest.fn().mockResolvedValue({ score: 72, rationale: 'cached path' }),
    };
    const engine = new RecommendationEngine({ db, geminiClient: geminiMock, cacheTtlMs: 5 * 60 * 1000 });

    const payload = {
      storeId: 4,
      storeName: '테스트마켓',
      storeCategory: 'SHOPPING',
      ownedCardIds: [gammaId],
      limit: 5,
    };

    const first = await engine.getRecommendations(payload);
    const second = await engine.getRecommendations(payload);

    expect(geminiMock.scoreCard).toHaveBeenCalledTimes(1);
    expect(first.meta.cached).toBe(false);
    expect(second.meta.cached).toBe(true);
  });
});
