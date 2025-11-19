#!/usr/bin/env node

const path = require('path');
const { createDatabase } = require('./db');
const { loadData } = require('./ingest/loadData');

async function run() {
  try {
    const db = createDatabase();
    const summary = await loadData({ db });
    console.log('Data load complete:', summary);
    db.close();
    process.exit(0);
  } catch (error) {
    console.error('Failed to load data:', error);
    process.exit(1);
  }
}

run();
