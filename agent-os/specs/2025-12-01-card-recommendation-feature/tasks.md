# Task Breakdown: Card Recommendation Feature

## Overview
Total Tasks: 21
Assigned roles: database-engineer, api-engineer, android-engineer, testing-engineer

## Task List

### Database Layer

#### Task Group 1: Schema + Ingestion Upgrades
**Assigned implementer:** database-engineer  
**Dependencies:** None

- [ ] 1.0 Complete database layer
  - [ ] 1.1 Write 3-4 focused sqlite-based tests validating `is_location_based` ingestion and query helpers (cover migration default, CSV parsing, and filtering order).
  - [ ] 1.2 Create migration adding `is_location_based INTEGER NOT NULL DEFAULT 0` to `card_benefits`, plus indexes on `(card_id, is_location_based)` and `(normalized_category, is_location_based)` per standards/backend/migrations.md.
  - [ ] 1.3 Update `src/ingest/loadData.js` to parse the CSV field, normalize truthy values, and persist the flag while preserving existing category mapping.
  - [ ] 1.4 Extend `src/queries/cards.js` (or new helper) so recommendation consumers can fetch location-priority benefit rows without resorting to raw SQL.
  - [ ] 1.5 Run ONLY the tests from 1.1 to confirm migration + ingestion + query logic, do not execute the entire backend suite.

**Acceptance Criteria:**
- Tests from 1.1 pass on local sqlite DB.
- Migration applies/rolls back cleanly and keeps existing data intact.
- Ingestion writes accurate `is_location_based` values for every benefit row.
- Query helpers expose `is_location_based` without breaking current `/api/cards` responses.

### API Layer

#### Task Group 2: Recommendation Engine + Endpoint
**Assigned implementer:** api-engineer  
**Dependencies:** Task Group 1

- [ ] 2.0 Complete recommendation service
  - [ ] 2.1 Write 4-6 focused unit/integration tests covering location-priority weighting, Gemini success path (mocked), fallback logic, caching, and `/api/recommendations` validation errors.
  - [ ] 2.2 Implement a `RecommendationEngine` module that orchestrates location matches, Gemini scoring (weights 0-100), fallback heuristics, normalization, and 5-minute memoization keyed by store + owned cards + discover flag.
  - [ ] 2.3 Add a `GeminiClient` wrapper that reads `GEMINI_API_KEY`, enforces 1s timeout with single retry/backoff, redacts sensitive logs, and emits structured `{ score, rationale }` responses.
  - [ ] 2.4 Create `POST /api/recommendations` handler mirroring `/api/cards` envelope, including input validation, metadata (`discover`, `latencyMs`, `scoreSource`), and `X-RateLimit-*` headers per backend/api standards.
  - [ ] 2.5 Implement graceful error handling + fallback messaging so clients know when LLM scoring was skipped; ensure logs annotate score source without leaking PII.
  - [ ] 2.6 Run ONLY the tests from 2.1 plus existing linting needed for touched files.

**Acceptance Criteria:**
- Tests from 2.1 pass with Gemini client mocked.
- Endpoint returns ranked data with `score`, `scoreSource`, and rationale per spec, handling discover vs owned flows.
- Validation rejects malformed payloads with standardized error format.
- LLM failures automatically fall back without exceeding 1.5s P95 backend latency (excluding LLM timeouts).

### Android Client

#### Task Group 3: Recommendation UX & Persistence
**Assigned implementer:** android-engineer  
**Dependencies:** Task Group 2

- [ ] 3.0 Complete Android-side experience
  - [ ] 3.1 Write 3-5 Compose/UI tests covering Discover button enable/disable, fallback banner rendering, and persistence of owned cards influencing recommendations.
  - [ ] 3.2 Implement `OwnedCardsStore` (Proto DataStore or equivalent) so `ManageCardsScreen` selections persist and emit flow updates consumed by recommendation logic.
  - [ ] 3.3 Extend Retrofit `ApiService` and add `RecommendationRepository` to call `/api/recommendations`, map DTOs (score metadata), and expose discover vs owned modes.
  - [ ] 3.4 Update `CardsViewModel` (or new `RecommendationViewModel`) to combine selected store, owned cards, and discover flag, handling loading/error state + retry per global/error-handling.md.
  - [ ] 3.5 Refresh `RecommendationSheetContent` (and related composables) to show score badges, rationale text, fallback messaging, and a bottom-aligned Discover button with accessibility semantics + responsive spacing per frontend standards.
  - [ ] 3.6 Run ONLY the tests from 3.1 (plus necessary Compose previews) before handing off.

**Acceptance Criteria:**
- Tests from 3.1 pass locally (instrumented or Robolectric acceptable).
- Owned cards persist across app restarts and immediately influence recommendation ordering.
- UI reflects score source (location/LLM/fallback), exposes Discover button, and maintains accessibility contrast/tap targets.
- Errors/fallback states surface via snackbars or inline messaging without crashing the sheet.

### Testing & Quality

#### Task Group 4: Feature Test Audit
**Assigned implementer:** testing-engineer  
**Dependencies:** Task Groups 1-3

- [ ] 4.0 Consolidate feature-specific quality checks
  - [ ] 4.1 Review the tests introduced in 1.1, 2.1, and 3.1 to understand current coverage.
  - [ ] 4.2 Identify coverage gaps for key workflows (store selection → owned recommendation, Discover flow, LLM timeout fallback) without scanning unrelated modules.
  - [ ] 4.3 Add up to 8 targeted integration/E2E tests (API + Android instrumentation or backend contract) to cover the documented gaps only.
  - [ ] 4.4 Run ONLY the combined feature-specific suite (tests from 1.1, 2.1, 3.1, 4.3) and report results; do not execute the entire repository suite.

**Acceptance Criteria:**
- Added tests (max 8) ensure end-to-end coverage for owned vs discover flows and fallback behavior.
- Feature-specific suite passes and is documented for verifiers.
- No redundant or out-of-scope tests added; focus stays on this feature per standards/testing/test-writing.md.

## Execution Order
1. Task Group 1: Schema + Ingestion Upgrades (database-engineer)
2. Task Group 2: Recommendation Engine + Endpoint (api-engineer)
3. Task Group 3: Recommendation UX & Persistence (android-engineer)
4. Task Group 4: Feature Test Audit (testing-engineer)
