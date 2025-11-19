# Specification: Making Endpoint For /api/cards

## Goal
Deliver production-ready `/api/cards` endpoints backed by the ingested SQLite data so the mobile app can fetch real credit card recommendations with category filtering, pagination, and metadata.

## User Stories
- As a rewards-focused shopper, I want to request available credit cards by category so that I can quickly see the best options for a given spending context.
- As a mobile developer, I want a paginated card API with clear metadata so that I can display fresh, accurate information without custom aggregation logic.
- As a backend engineer, I want error responses to follow our standard envelope so that client handling remains consistent across services.

## Core Requirements
### Functional Requirements
- Provide `GET /api/cards` JSON responses sourced from the SQLite store, returning cards with issuer and normalized category information.
- Support `category` query parameter to filter cards by `normalized_category`; omit filter to return all categories.
- Support pagination via `limit` (default 25, max TBD) and `offset` query parameters; include pagination metadata (`total`, `limit`, `offset`).
- Include dataset metadata fields such as `lastRefreshedAt` (from latest `refresh_logs.completed_at`) and `dataSource` descriptor in the response envelope.
- Expose card benefit details through a dedicated `GET /api/cards/:cardId/benefits` endpoint returning the associated benefit rows.
- Remove legacy `/cards` mock route and associated static data to prevent stale responses.
- Return standardized JSON error objects for invalid parameters, missing resources, or server errors.

### Non-Functional Requirements
- API remains public (no authentication); responses must be cache-friendly and stateless.
- Response times should stay within current Express + SQLite performance envelopes (<200 ms for typical queries on laptop-class hardware).
- Follow existing JSON casing, error envelope, and validation standards documented in global conventions.

## Visual Design
No visual assets provided for this feature; follow existing API naming and response structure conventions.

## Reusable Components
### Existing Code to Leverage
- `local-backend/src/db.js` for database connection pooling and schema initialization.
- `local-backend/src/ingest/loadData.js` for knowledge of table structures and refresh logging metadata.
- `local-backend/index.js` Express app setup for middleware, CORS, and server bootstrap pattern.
- `local-backend/test/api.test.js` with SuperTest harness as a template for new endpoint tests.
- SQLite tables `cards`, `card_benefits`, `refresh_logs`, and `category_mappings` populated by the ingestion pipeline.

### New Components Required
- Query utilities to fetch paginated cards with optional category filtering (`local-backend/src/queries/cards.js` or similar) because no read-model helpers exist yet.
- Benefit retrieval helper to load benefits for a given card id while ensuring consistent ordering.
- Updated Express route handlers under `/api` namespace since existing routes live at root and return mock data.

## Technical Approach
- Database: Leverage existing tables; implement SQL queries using prepared statements for pagination (`LIMIT`/`OFFSET`) and for counting totals. Join `cards` with `category_mappings` when needed to expose normalized categories. Use `refresh_logs` to derive `lastRefreshedAt` and `dataSource` (e.g., file hash or static label from ingestion).
- API: Add `GET /api/cards` handler that validates query params, executes count and data queries, and returns `{ data: [...], meta: { total, limit, offset, lastRefreshedAt, dataSource } }`. Add `GET /api/cards/:cardId/benefits` returning associated benefit rows or 404. Remove `GET /cards` mock. Ensure errors follow `{ error: { code, message } }` signature.
- Frontend: Update Android data layer to consume `/api/cards` and `/api/cards/:id/benefits` once available; UI stays unchanged but will receive real data.
- Testing: Extend SuperTest suite to cover success cases (default pagination, category filter, bounds checking) and error cases (invalid parameters, unknown card). Update ingestion tests if needed to ensure refresh log metadata accessible.

## Out of Scope
- Additional filtering criteria (issuer, keyword, merchant proximity).
- Authentication, authorization, or rate limiting layers.
- Recommendation ranking or personalization logic beyond dataset filtering.
- Bulk export endpoints or GraphQL interfaces.

## Success Criteria
- `/api/cards` returns paginated JSON sourced from SQLite with accurate metadata and passes automated tests.
- `/api/cards/:cardId/benefits` returns benefits for valid cards and 404 for missing ids.
- Legacy `/cards` route and mock data removed without breaking existing tests; new tests cover functionality.
- Android client can fetch live card data without code changes beyond endpoint URL updates.
