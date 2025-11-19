const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const csv = require('csv-parser');
const { createDatabase } = require('../db');

const DEFAULT_CARDS_CSV = path.join(__dirname, '..', '..', '..', 'data', 'cards_db.csv');

const DATA_DIR = path.join(__dirname, '..', '..', '..', 'data');
const MERCHANT_CSV_CANDIDATES = ['merchants_db.csv', 'merchants_db (1).csv'];

function resolveDefaultMerchantsCsv() {
  for (const candidate of MERCHANT_CSV_CANDIDATES) {
    const candidatePath = path.join(DATA_DIR, candidate);
    if (fs.existsSync(candidatePath)) {
      return candidatePath;
    }
  }

  return path.join(DATA_DIR, MERCHANT_CSV_CANDIDATES[0]);
}

const DEFAULT_MERCHANTS_CSV = resolveDefaultMerchantsCsv();
const DEFAULT_MAPPING_PATH = path.join(__dirname, '..', '..', 'config', 'category-mapping.json');

function readJsonFile(mappingPath = DEFAULT_MAPPING_PATH) {
  const raw = fs.readFileSync(mappingPath, 'utf8');
  return JSON.parse(raw);
}

function normalizeUnicode(value) {
  if (typeof value !== 'string') return '';
  return value.replace(/\uFEFF/g, '').trim();
}

function issuerFromCardName(cardName) {
  if (!cardName) return '';
  const [issuer] = cardName.split(' ');
  return issuer || '';
}

function hashMapping(mapping) {
  const hash = crypto.createHash('sha256');
  hash.update(JSON.stringify(mapping));
  return hash.digest('hex');
}

function parseCsv(filePath, onData) {
  return new Promise((resolve, reject) => {
    if (!fs.existsSync(filePath)) {
      reject(new Error(`CSV file not found: ${filePath}`));
      return;
    }

    fs.createReadStream(filePath)
      .pipe(csv())
      .on('data', (row) => {
        onData(row);
      })
      .on('error', (error) => {
        reject(error);
      })
      .on('end', () => {
        resolve();
      });
  });
}

async function loadCardsData(cardsCsvPath, categoryMap) {
  const cardMap = new Map();
  const benefits = [];

  await parseCsv(cardsCsvPath, (row) => {
    const cardName = normalizeUnicode(row['card_name']);
    if (!cardName) {
      return;
    }

    const issuer = issuerFromCardName(cardName);
    if (!cardMap.has(cardName)) {
      cardMap.set(cardName, { name: cardName, issuer });
    }

    const description = normalizeUnicode(row['상세_혜택']);
    const keyword = normalizeUnicode(row['혜택_키워드']);
    const sourceCategory = normalizeUnicode(row['카드혜택_분류']) || '기타';
    const normalizedCategory = categoryMap[sourceCategory] || 'OTHER';

    benefits.push({
      cardName,
      description,
      keyword,
      sourceCategory,
      normalizedCategory,
    });
  });

  return {
    cards: Array.from(cardMap.values()),
    benefits,
  };
}

async function loadMerchantData(merchantsCsvPath, categoryMap) {
  const merchants = [];

  await parseCsv(merchantsCsvPath, (row) => {
    const name = normalizeUnicode(row['상호명']);
    if (!name) {
      return;
    }

    const branch = normalizeUnicode(row['지점명']);
    const address = normalizeUnicode(row['도로명주소']);
    const lng = Number(normalizeUnicode(row['경도']));
    const lat = Number(normalizeUnicode(row['위도']));
    if (Number.isNaN(lat) || Number.isNaN(lng)) {
      return;
    }

    const sourceCategory = normalizeUnicode(row['카드혜택_분류']) || '기타';
    const normalizedCategory = categoryMap[sourceCategory] || 'OTHER';

    merchants.push({
      name,
      branch: branch || null,
      address,
      longitude: lng,
      latitude: lat,
      sourceCategory,
      normalizedCategory,
    });
  });

  return merchants;
}

function insertCategoryMappings(db, categoryMap) {
  db.prepare('DELETE FROM category_mappings').run();
  const upsertMapping = db.prepare(`
    INSERT INTO category_mappings (source_category, normalized_category)
    VALUES (@source_category, @normalized_category)
    ON CONFLICT(source_category) DO UPDATE SET
      normalized_category = excluded.normalized_category,
      updated_at = CURRENT_TIMESTAMP
  `);

  const entries = Object.entries(categoryMap);
  const insert = db.transaction(() => {
    for (const [source, normalized] of entries) {
      upsertMapping.run({ source_category: source, normalized_category: normalized });
    }
  });

  insert();
  return entries.length;
}

function clearData(db) {
  const statements = [
    'DELETE FROM card_benefits',
    'DELETE FROM merchant_categories',
    'DELETE FROM merchants',
    'DELETE FROM cards',
  ];

  db.transaction(() => {
    statements.forEach((sql) => db.prepare(sql).run());
  })();
}

function upsertCardsAndBenefits(db, cards, benefits) {
  const insertCard = db.prepare(`
    INSERT INTO cards (name, issuer)
    VALUES (@name, @issuer)
    RETURNING id
  `);
  const insertBenefit = db.prepare(`
    INSERT INTO card_benefits (card_id, description, keyword, source_category, normalized_category)
    VALUES (@card_id, @description, @keyword, @source_category, @normalized_category)
  `);

  const cardIds = new Map();

  const insertCards = db.transaction(() => {
    for (const card of cards) {
      const result = insertCard.get(card);
      cardIds.set(card.name, result.id);
    }
  });

  insertCards();

  const insertBenefits = db.transaction(() => {
    for (const benefit of benefits) {
      const cardId = cardIds.get(benefit.cardName);
      if (!cardId) continue;
      insertBenefit.run({
        card_id: cardId,
        description: benefit.description,
        keyword: benefit.keyword,
        source_category: benefit.sourceCategory,
        normalized_category: benefit.normalizedCategory,
      });
    }
  });

  insertBenefits();

  return {
    cardsInserted: cards.length,
    benefitsInserted: benefits.length,
  };
}

function upsertMerchants(db, merchants) {
  const insertMerchant = db.prepare(`
    INSERT INTO merchants (name, branch, address, longitude, latitude, source_category, normalized_category)
    VALUES (@name, @branch, @address, @longitude, @latitude, @source_category, @normalized_category)
    RETURNING id
  `);
  const insertMerchantCategory = db.prepare(`
    INSERT INTO merchant_categories (merchant_id, source_category, normalized_category)
    VALUES (@merchant_id, @source_category, @normalized_category)
  `);

  const insert = db.transaction(() => {
    for (const merchant of merchants) {
      const result = insertMerchant.get({
        name: merchant.name,
        branch: merchant.branch,
        address: merchant.address,
        longitude: merchant.longitude,
        latitude: merchant.latitude,
        source_category: merchant.sourceCategory,
        normalized_category: merchant.normalizedCategory,
      });
      insertMerchantCategory.run({
        merchant_id: result.id,
        source_category: merchant.sourceCategory,
        normalized_category: merchant.normalizedCategory,
      });
    }
  });

  insert();

  return merchants.length;
}

async function loadData({
  db = createDatabase(),
  cardsCsvPath = DEFAULT_CARDS_CSV,
  merchantsCsvPath = DEFAULT_MERCHANTS_CSV,
  categoryMappingPath = DEFAULT_MAPPING_PATH,
  categoryMap,
} = {}) {
  const resolvedCategoryMap = categoryMap || readJsonFile(categoryMappingPath);
  const mappingHash = hashMapping(resolvedCategoryMap);

  const startedAt = new Date().toISOString();
  const insertLog = db.prepare(`
    INSERT INTO refresh_logs (started_at, mapping_hash)
    VALUES (?, ?)
  `);
  const updateLog = db.prepare(`
    UPDATE refresh_logs
    SET completed_at = ?,
        cards_loaded = ?,
        benefits_loaded = ?,
        merchants_loaded = ?,
        mapping_entries = ?,
        error = NULL
    WHERE id = ?
  `);
  const errorLog = db.prepare(`
    UPDATE refresh_logs
    SET completed_at = ?, error = ?
    WHERE id = ?
  `);

  const logId = insertLog.run(startedAt, mappingHash).lastInsertRowid;

  try {
    const [{ cards, benefits }, merchants] = await Promise.all([
      loadCardsData(cardsCsvPath, resolvedCategoryMap),
      loadMerchantData(merchantsCsvPath, resolvedCategoryMap),
    ]);

    clearData(db);

    const mappingEntries = insertCategoryMappings(db, resolvedCategoryMap);
    const { cardsInserted, benefitsInserted } = upsertCardsAndBenefits(db, cards, benefits);
    const merchantsInserted = upsertMerchants(db, merchants);

    const completedAt = new Date().toISOString();
    updateLog.run(
      completedAt,
      cardsInserted,
      benefitsInserted,
      merchantsInserted,
      mappingEntries,
      logId
    );

    return {
      cardsInserted,
      benefitsInserted,
      merchantsInserted,
      mappingEntries,
      logId,
    };
  } catch (error) {
    const completedAt = new Date().toISOString();
    errorLog.run(completedAt, error.message, logId);
    throw error;
  }
}

module.exports = {
  loadData,
  loadCardsData,
  loadMerchantData,
  normalizeUnicode,
  readJsonFile,
};
