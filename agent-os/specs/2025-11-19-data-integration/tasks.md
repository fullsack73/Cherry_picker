# Task Breakdown: Cards & Merchants Data Integration

## Overview
Total Tasks: 18
Assigned roles: database-engineer, api-engineer, android-engineer, testing-engineer

## Task List

### Data Ingestion & Persistence

#### Task Group 1: CSV Ingestion Pipeline & Storage
**Assigned implementer:** database-engineer  
**Dependencies:** None

- [ ] 1.0 Complete ingestion and persistence layer
  - [ ] 1.1 Write 3-5 focused ingestion tests (parsing edge cases, category normalization, idempotent seeding)
    - Limit to core success/failure paths only
  - [ ] 1.2 Design SQLite schema for cards, benefits, merchants, category mappings, and refresh logs
    - Include indexes on latitude/longitude and category columns
    - Follow naming/constraint standards in `standards/backend/models.md`
  - [ ] 1.3 Implement streaming CSV loader that normalizes UTF-8 text and trims whitespace
    - Map `카드혜택_분류` to internal benefit categories via configurable mapping file
  - [ ] 1.4 Seed database on boot and expose CLI `npm run load-data` to re-run ingestion without server restart
    - Ensure reruns upsert records and archive prior loads in refresh logs
  - [ ] 1.5 Validate inserts with lightweight sanity queries (counts, distinct categories)
  - [ ] 1.6 Run ONLY tests from 1.1 and confirm migrations/seed complete within 5 seconds

**Acceptance Criteria:**
- Ingestion tests from 1.1 pass
- Database schema matches spec requirements with required indexes and constraints
- CLI reloads both CSVs idempotently and logs refresh timestamps
- Data is UTF-8 clean, normalized, and ready for API consumption within 5 seconds of boot

### API Layer & Recommendation Logic

#### Task Group 2: REST Endpoints & Matching Engine
**Assigned implementer:** api-engineer  
**Dependencies:** Task Group 1

- [ ] 2.0 Complete API layer
  - [ ] 2.1 Write 4-6 targeted supertest suites (cards listing, merchants radius query, recommendations, error handling)
    - Cover happy paths plus malformed query scenario
  - [ ] 2.2 Replace `/stores` with `/api/merchants` supporting pagination, keyword search, and radius filtering using DB indices
  - [ ] 2.3 Replace `/cards` with `/api/cards` supporting pagination and benefit-category filters
  - [ ] 2.4 Implement `/api/recommendations` that ranks cards for a merchant via category weighting and returns benefit snippets
    - Reuse category mapping from Task Group 1 config
  - [ ] 2.5 Standardize error responses (JSON body, HTTP status codes, logging) per `standards/global/error-handling.md`
  - [ ] 2.6 Document query parameters and sample responses in README or OpenAPI stub
  - [ ] 2.7 Run ONLY tests from 2.1 and confirm responses stay under 300 ms on sample datasets

**Acceptance Criteria:**
- Supertest suites from 2.1 pass
- All endpoints return structured JSON, proper status codes, and leverage seeded data
- Recommendation endpoint surfaces at least three ranked cards when categories overlap
- Performance target (≤300 ms) met in local benchmarks with seeded data

### Android Integration

#### Task Group 3: Mobile Consumption & UI Updates
**Assigned implementer:** android-engineer  
**Dependencies:** Task Group 2

- [ ] 3.0 Complete Android data integration
  - [ ] 3.1 Write 3-5 focused unit/UI tests (repository mapping, map marker rendering, recommendation sheet population)
    - Use mock web service responses only for primary flows
  - [ ] 3.2 Extend Retrofit service definitions and DTOs for new `/api/cards`, `/api/merchants`, `/api/recommendations` endpoints
  - [ ] 3.3 Add repositories with caching/paging to avoid loading entire datasets into memory
  - [ ] 3.4 Connect map view to live merchant data, including pagination/infinite scroll markers and Unicode-safe labels
  - [ ] 3.5 Update recommendation bottom sheet to display real benefit text, match scores, and fallback states when no matches exist
  - [ ] 3.6 Refresh Manage Cards screen to list cards from API with category filters mapped to existing `CardCategory`
  - [ ] 3.7 Ensure dark mode, accessibility, and loading/error states follow `standards/frontend/*`
  - [ ] 3.8 Run ONLY tests from 3.1 plus targeted instrumentation smoke, verifying UI renders with mocked API data

**Acceptance Criteria:**
- Android tests from 3.1 pass
- Map markers and recommendation sheet populate from live endpoints with graceful empty/error states
- Manage Cards screen reflects remote catalog while preserving search/filter behavior
- App respects accessibility and dark mode requirements while keeping memory usage stable with large datasets

### Quality Review

#### Task Group 4: Test Gap Analysis & E2E Validation
**Assigned implementer:** testing-engineer  
**Dependencies:** Task Groups 1-3

- [ ] 4.0 Validate feature coverage
  - [ ] 4.1 Review tests produced in 1.1, 2.1, and 3.1 to map coverage against spec requirements
  - [ ] 4.2 Add up to 8 high-value integration/E2E tests (e.g., ingestion→API→Android flow, recommendation accuracy sanity)
    - Keep total new tests ≤10 as per standards
  - [ ] 4.3 Execute ONLY the combined feature-specific suites (1.1, 2.1, 3.1, 4.2) and document results
  - [ ] 4.4 Record lingering risks or follow-up items if gaps remain

**Acceptance Criteria:**
- Added tests from 4.2 pass alongside earlier suites
- End-to-end flow (CSV ingestion → API → Android UI) validated for at least one sample merchant
- Outstanding risks documented with suggested mitigations

## Execution Order
1. Task Group 1: CSV Ingestion Pipeline & Storage
2. Task Group 2: REST Endpoints & Matching Engine
3. Task Group 3: Mobile Consumption & UI Updates
4. Task Group 4: Test Gap Analysis & E2E Validation
