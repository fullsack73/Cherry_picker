const express = require('express');
const fs = require('fs');
const path = require('path');
const os = require('os');
const cors = require('cors');
const { createDatabase } = require('./src/db');
const { loadData } = require('./src/ingest/loadData');

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

const runtimeDbPath = process.env.DB_PATH || (process.env.NODE_ENV === 'test'
  ? path.join(os.tmpdir(), `cherry-picker-test-${process.pid}.sqlite`)
  : undefined);

const db = createDatabase(runtimeDbPath);
const shouldBootstrapData = process.env.SKIP_DATA_BOOTSTRAP === '1'
  ? false
  : process.env.NODE_ENV !== 'test';

const bootstrapPromise = (shouldBootstrapData ? loadData({ db }) : Promise.resolve()).catch((error) => {
  console.error('Failed to bootstrap data store:', error);
  if (process.env.NODE_ENV !== 'test') {
    process.exit(1);
  }
});

const stores = [
  { id: 1, name: 'Store A', latitude: 34.0522, longitude: -118.2437 },
  { id: 2, name: 'Store B', latitude: 34.0525, longitude: -118.2440 },
  { id: 3, name: 'Store C', latitude: 34.0530, longitude: -118.2435 },
];

const cards = [
  {
    category: 'Travel',
    cards: [
      { id: 1, name: 'Chase Sapphire Preferred' },
      { id: 2, name: 'Capital One Venture Rewards' },
    ],
  },
  {
    category: 'Cash Back',
    cards: [
      { id: 3, name: 'Citi Double Cash' },
      { id: 4, name: 'Chase Freedom Unlimited' },
    ],
  },
];

app.post('/api/location', (req, res) => {
  const { latitude, longitude } = req.body;

  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    return res.status(400).json({ error: 'Invalid latitude or longitude' });
  }

  console.log(`Received location: latitude=${latitude}, longitude=${longitude}`);

  const locationData = `Latitude: ${latitude}, Longitude: ${longitude}\n`;
  fs.appendFile(path.join(__dirname, 'location_data.txt'), locationData, (err) => {
    if (err) {
      console.error('Failed to write to file:', err);
      return res.status(500).json({ error: 'Failed to save location data' });
    }
  });

  res.status(200).json({ status: 'success' });
});

app.get('/stores', (req, res) => {
  res.json(stores);
});

app.get('/cards', (req, res) => {
  res.json(cards);
});

if (process.env.NODE_ENV !== 'test') {
  bootstrapPromise.then(() => {
    app.listen(port, () => {
      console.log(`Server listening at http://localhost:${port}`);
    });
  });
}

module.exports = app;
module.exports.db = db;
module.exports.bootstrapPromise = bootstrapPromise;


