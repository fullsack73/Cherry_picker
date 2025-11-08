const request = require('supertest');
const app = require('../index'); // Assuming your express app is exported from index.js

describe('API Endpoints', () => {
  describe('GET /stores', () => {
    it('should return a list of nearby stores', async () => {
      const response = await request(app).get('/stores');
      expect(response.status).toBe(200);
      expect(response.body).toBeInstanceOf(Array);
      expect(response.body.length).toBeGreaterThan(0);
      expect(response.body[0]).toHaveProperty('id');
      expect(response.body[0]).toHaveProperty('name');
      expect(response.body[0]).toHaveProperty('latitude');
      expect(response.body[0]).toHaveProperty('longitude');
    });
  });

  describe('GET /cards', () => {
    it('should return a list of credit card categories and cards', async () => {
      const response = await request(app).get('/cards');
      expect(response.status).toBe(200);
      expect(response.body).toBeInstanceOf(Array);
      expect(response.body.length).toBeGreaterThan(0);
      expect(response.body[0]).toHaveProperty('category');
      expect(response.body[0]).toHaveProperty('cards');
      expect(response.body[0].cards).toBeInstanceOf(Array);
      expect(response.body[0].cards.length).toBeGreaterThan(0);
      expect(response.body[0].cards[0]).toHaveProperty('id');
      expect(response.body[0].cards[0]).toHaveProperty('name');
    });
  });
});
