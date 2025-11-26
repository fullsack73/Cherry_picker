function requireDb(db) {
  if (!db) {
    throw new Error('Database instance is required');
  }
}

function toRad(deg) {
  return deg * (Math.PI / 180);
}

function haversineDistance(lat1, lon1, lat2, lon2) {
  const R = 6371000; // Earth radius in meters
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function fetchNearbyStores(db, { latitude, longitude, radius = 500, categories = [] }) {
  requireDb(db);

  const lat = Number(latitude);
  const lon = Number(longitude);
  const rad = Number(radius);

  if (isNaN(lat) || isNaN(lon) || isNaN(rad)) {
    return [];
  }

  // Bounding box calculation for initial SQL filtering
  // 1 degree latitude is approximately 111,320 meters
  const latDelta = rad / 111320;
  // 1 degree longitude depends on latitude: 111,320 * cos(lat)
  // We use the larger delta (at the pole-ward side of the box) or just the center for approximation
  // Using center latitude for longitude delta approximation is usually sufficient for small radii
  const lonDelta = rad / (111320 * Math.cos(toRad(lat)));

  const minLat = lat - latDelta;
  const maxLat = lat + latDelta;
  const minLon = lon - Math.abs(lonDelta);
  const maxLon = lon + Math.abs(lonDelta);

  let query = `
    SELECT id, name, branch, address, longitude, latitude, source_category, normalized_category
    FROM merchants
    WHERE latitude BETWEEN ? AND ?
    AND longitude BETWEEN ? AND ?
  `;

  const params = [minLat, maxLat, minLon, maxLon];

  if (categories && Array.isArray(categories) && categories.length > 0) {
    const placeholders = categories.map(() => '?').join(',');
    query += ` AND normalized_category IN (${placeholders})`;
    params.push(...categories);
  }

  const stmt = db.prepare(query);
  const candidates = stmt.all(...params);

  // Refine results with precise Haversine distance
  return candidates
    .map(store => {
      const distance = haversineDistance(lat, lon, store.latitude, store.longitude);
      return { ...store, distance };
    })
    .filter(store => store.distance <= rad)
    .sort((a, b) => a.distance - b.distance);
}

module.exports = {
  fetchNearbyStores,
};
