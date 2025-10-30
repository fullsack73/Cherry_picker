

const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const port = 3000;

app.use(express.json());

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

if (process.env.NODE_ENV !== 'test') {
  app.listen(port, () => {
    console.log(`Server listening at http://localhost:${port}`);
  });
}

module.exports = app;

