
const request = require('supertest');
const app = require('../index');
const fs = require('fs');
const path = require('path');
const axios = require('axios');

describe('/api/location', () => {
  const locationDataPath = path.join(__dirname, '../location_data.txt');
  let server;
  let port;

  beforeAll((done) => {
    server = app.listen(0, () => {
      port = server.address().port;
      done();
    });
  });

  afterAll((done) => {
    server.close(done);
  });

  beforeEach(() => {
    if (fs.existsSync(locationDataPath)) {
      fs.unlinkSync(locationDataPath);
    }
  });

  it('should return 200 OK and success message for valid data', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128, longitude: -74.006 });

    expect(response.statusCode).toBe(200);
    expect(response.body).toEqual({ status: 'success' });
  });

  it('should write location data to file', async () => {
    await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128, longitude: -74.006 });

    const data = fs.readFileSync(locationDataPath, 'utf8');
    expect(data).toContain('Latitude: 40.7128, Longitude: -74.006');
  });

  it('should log location data to the console', async () => {
    const consoleSpy = jest.spyOn(console, 'log');
    await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128, longitude: -74.006 });

    expect(consoleSpy).toHaveBeenCalledWith('Received location: latitude=40.7128, longitude=-74.006');
    consoleSpy.mockRestore();
  });

  it('should return 200 OK when receiving a request from an external client', async () => {
    const response = await axios.post(`http://localhost:${port}/api/location`, {
      latitude: 40.7128,
      longitude: -74.006
    });

    expect(response.status).toBe(200);
    expect(response.data).toEqual({ status: 'success' });
  });

  it('should return 400 for missing latitude', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({ longitude: -74.006 });

    expect(response.statusCode).toBe(400);
  });

  it('should return 400 for missing longitude', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128 });

    expect(response.statusCode).toBe(400);
  });

  it('should return 400 for invalid latitude type', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({ latitude: 'invalid', longitude: -74.006 });

      expect(response.statusCode).toBe(400);
  });

  it('should return 400 for invalid longitude type', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128, longitude: 'invalid' });

    expect(response.statusCode).toBe(400);
  });

  it('should append new location data to the file on subsequent requests', async () => {
    // First request
    await request(app)
      .post('/api/location')
      .send({ latitude: 40.7128, longitude: -74.006 });

    // Second request
    await request(app)
      .post('/api/location')
      .send({ latitude: 34.0522, longitude: -118.2437 });

    const data = fs.readFileSync(locationDataPath, 'utf8');
    expect(data).toContain('Latitude: 40.7128, Longitude: -74.006');
    expect(data).toContain('Latitude: 34.0522, Longitude: -118.2437');
  });

  it('should return 400 for an empty request body', async () => {
    const response = await request(app)
      .post('/api/location')
      .send({});

    expect(response.statusCode).toBe(400);
  });
});
