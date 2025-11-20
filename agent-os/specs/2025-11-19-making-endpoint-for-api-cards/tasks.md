# Task Breakdown: Making Endpoint For /api/cards

## Overview
Total Tasks: 9
Assigned roles: database-engineer, api-engineer, testing-engineer

## Task List

### Data Access Layer

#### Task Group 1: Card Query Utilities
**Assigned implementer:** database-engineer  
**Dependencies:** Ingestion pipeline already loads data

- [x] 1.0 Implement database helpers for cards
  - [x] 1.1 Write 2-4 focused unit tests covering pagination, category filtering, and metadata query helpers
  - [x] 1.2 Create query module (e.g., `src/queries/cards.js`) that fetches paginated cards with optional `normalized_category` filter
  - [x] 1.3 Add helper to retrieve benefits for a given card id ordered by normalized category
  - [x] 1.4 Expose latest refresh log info utility to provide `lastRefreshedAt` and optional `dataSource` label
  - [x] 1.5 Run only the new tests from 1.1 to confirm helpers behave as expected

**Acceptance Criteria:**
- Pagination and category filtering return consistent results backed by SQLite
- Benefit lookup returns correct rows for valid ids and empty set for missing ids
- Refresh metadata helper surfaces the latest `completed_at`
- Tests from 1.1 pass locally

### API Layer

#### Task Group 2: Cards API Endpoints
**Assigned implementer:** api-engineer  
**Dependencies:** Task Group 1

- [x] 2.0 Build `/api/cards` and benefits endpoints
  - [x] 2.1 Write 3-6 SuperTest cases covering default pagination, category filter, invalid params, unknown card, and metadata fields
  - [x] 2.2 Implement `GET /api/cards` handler using query utilities; return `{ data, meta }` envelope with pagination + `lastRefreshedAt`, `dataSource`
  - [x] 2.3 Implement `GET /api/cards/:cardId/benefits` returning benefit array or `404` with standard error envelope
  - [x] 2.4 Remove legacy `/cards` mock route and any static data references
  - [x] 2.5 Ensure new API tests from 2.1 pass (run only those tests)

**Acceptance Criteria:**
- `/api/cards` returns real card data with pagination, optional category filter, and metadata
- `/api/cards/:cardId/benefits` returns benefits or 404 error payload
- Mock `/cards` endpoint deleted without breaking other routes
- Tests from 2.1 pass

### Client Integration & Testing

#### Task Group 3: Android Data Integration
**Assigned implementer:** android-engineer  
**Dependencies:** Task Group 2

- [x] 3.0 Update Android client to consume new API
  - [x] 3.1 Write 2-4 focused unit or integration tests covering repository parsing of new payload, metadata handling, and pagination inputs
  - [x] 3.2 Update network layer to point to `/api/cards` endpoint and new benefits route
  - [x] 3.3 Adjust models/viewmodels to accommodate pagination metadata and separate benefit calls
  - [x] 3.4 Remove reliance on hardcoded mock card data in Android layer
  - [x] 3.5 Run only the new Android tests from 3.1

**Acceptance Criteria:**
- Android client fetches live data from `/api/cards`
- UI displays data without mock dependencies
- Tests from 3.1 succeed

### Quality Assurance

#### Task Group 4: Feature Test Consolidation
**Assigned implementer:** testing-engineer  
**Dependencies:** Task Groups 1-3

- [ ] 4.0 Validate coverage and add gap tests (max 6 new tests)
  - [ ] 4.1 Review tests introduced in Task Groups 1-3 for completeness
  - [ ] 4.2 Identify any critical gaps (e.g., error responses, pagination bounds, Android integration scenario)
  - [ ] 4.3 Add up to 6 integration or end-to-end tests that exercise the full API-to-client flow (prioritize SuperTest + Android instrumentation/mocked network setups)
  - [ ] 4.4 Run only the combined suite of tests added in 1.1, 2.1, 3.1, and 4.3 to confirm overall integrity

**Acceptance Criteria:**
- Critical workflows covered end-to-end, including error handling
- No more than 6 additional tests added in 4.3
- All selected tests pass together

## Execution Order
1. Task Group 1: Card Query Utilities
2. Task Group 2: Cards API Endpoints
3. Task Group 3: Android Data Integration
4. Task Group 4: Feature Test Consolidation
