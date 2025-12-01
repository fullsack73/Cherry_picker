const fs = require('fs');
const path = require('path');
const Database = require('better-sqlite3');

const DEFAULT_DB_PATH = process.env.DB_PATH || path.join(__dirname, '..', 'storage', 'app.sqlite');

const schemaStatements = `
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS cards (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  issuer TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS card_benefits (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  card_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  keyword TEXT,
  source_category TEXT NOT NULL,
  normalized_category TEXT NOT NULL,
  is_location_based INTEGER NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_card_benefits_card_location
  ON card_benefits (card_id, is_location_based);

CREATE INDEX IF NOT EXISTS idx_card_benefits_category_location
  ON card_benefits (normalized_category, is_location_based);

CREATE TABLE IF NOT EXISTS merchants (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  branch TEXT,
  address TEXT,
  longitude REAL NOT NULL,
  latitude REAL NOT NULL,
  source_category TEXT NOT NULL,
  normalized_category TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS merchant_categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  merchant_id INTEGER NOT NULL,
  source_category TEXT NOT NULL,
  normalized_category TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS category_mappings (
  source_category TEXT PRIMARY KEY,
  normalized_category TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  started_at DATETIME NOT NULL,
  completed_at DATETIME,
  cards_loaded INTEGER DEFAULT 0,
  benefits_loaded INTEGER DEFAULT 0,
  merchants_loaded INTEGER DEFAULT 0,
  mapping_entries INTEGER DEFAULT 0,
  mapping_hash TEXT,
  error TEXT
);
`;

function ensureDirectory(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function createDatabase(dbPath = DEFAULT_DB_PATH) {
  ensureDirectory(dbPath);
  const db = new Database(dbPath);
  db.pragma('journal_mode = WAL');
  db.exec(schemaStatements);
  ensureLocationBasedSchema(db);
  return db;
}

function ensureLocationBasedSchema(db) {
  const hasColumn = db
    .prepare("SELECT 1 FROM pragma_table_info('card_benefits') WHERE name = 'is_location_based' LIMIT 1")
    .get();

  if (!hasColumn) {
    db.exec('ALTER TABLE card_benefits ADD COLUMN is_location_based INTEGER NOT NULL DEFAULT 0');
  }

  db.exec(`
    CREATE INDEX IF NOT EXISTS idx_card_benefits_card_location
    ON card_benefits (card_id, is_location_based)
  `);

  db.exec(`
    CREATE INDEX IF NOT EXISTS idx_card_benefits_category_location
    ON card_benefits (normalized_category, is_location_based)
  `);
}

module.exports = {
  createDatabase,
  DEFAULT_DB_PATH,
};
