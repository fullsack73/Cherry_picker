# Specification: Card Recommendation Feature

## Goal
Deliver backend-driven, weight-based credit card recommendations for a selected store, surfacing user-owned cards first and enabling a discover flow that highlights additional cards within the same category.

## User Stories
- As a shopper choosing a store, I want instantly ranked card suggestions so that I can pay with the card that maximizes rewards.
- As a user who already saved my cards, I want the recommendation list to respect the cards I own so that the advice stays realistic.
- As a curious user, I want a Discover action that shows promising cards I do not yet own so that I can decide whether to add them later.
- As a user on an unreliable network, I want a clear fallback message when LLM scoring is unavailable so that I still understand the recommendation rationale.

## Core Requirements
### Functional Requirements
- Compute recommendation weights in the local-backend by combining deterministic `is_location_based` matches, Gemini 2.5 Flash scoring, and category-only fallback logic.
- Accept store metadata (id, name, normalized category) and the user-owned card ids, returning a ranked list plus metadata identifying whether each score came from location, LLM, or fallback logic.
- Provide a Discover flag that triggers category-only scoring across all cards without calling the LLM.
- Expose results via a new `/api/recommendations` endpoint that matches the existing `/api/cards` response envelope and error handling patterns.
- Update the existing bottom sheet (`RecommendationSheetContent`) to render the ranked list, show score explanations, surface LLM-fallback messaging, and add a Discover button fixed at the list bottom.
- Persist the "cards I own" selection from `ManageCardsScreen` so the recommendation sheet and backend share the same source of truth.

### Non-Functional Requirements
- Backend response (excluding LLM latency) must stay under 1.5s for the 95th percentile; enforce a configurable LLM timeout (default 1s) and short exponential backoff (max 1 retry).
- All API inputs validated per global validation standards; return structured errors with clear `code` and `message` fields.
- Secure Gemini access via `GEMINI_API_KEY` env var; never log raw prompts or sensitive merchant data.
- Ensure the bottom sheet additions remain accessible: maintain tap targets ≥48dp, announce Discover button via TalkBack, and preserve existing focus order.
- Emit server logs noting score sources for observability while redacting card/user identifiers beyond hashed ids.

## Visual Design
- Mockup reference: none provided (keep referencing existing sheet in `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt`).
- Key UI elements to implement: Discover button anchored at list bottom, score badges/rationale text inline with `CardSummaryItem`, fallback banner for LLM issues, and accessibility semantics updates.
- Responsive breakpoints required: maintain current bottom sheet widths, verify readability at ≤360dp and tablet widths, and ensure button spans full width on phones while aligning to 640dp max width on large screens.

## Reusable Components
### Existing Code to Leverage
- **Components:** `RecommendationSheetContent`, `CardSummaryItem`, and `BenefitsList` (MainScreen) for rendering results; `ManageCardsScreen` for card ownership selection UI.
- **Services/ViewModels:** `CardsViewModel`, `CardsRepository`, and `StoreRepository` already orchestrate card/store loading and handle API envelopes.
- **Backend Modules:** `local-backend/src/db.js` schema initialization, `src/ingest/loadData.js` CSV ingestion, and `src/queries/cards.js` for pagination metadata patterns.
- **API Patterns:** `/api/cards` controller in `local-backend/index.js` demonstrates input validation, pagination defaults, and structured error responses to mirror.

### New Components Required
- **RecommendationEngine module (backend):** Encapsulate location matching, Gemini client calls, and fallback scoring because no existing service performs multi-signal weighting.
- **GeminiClient wrapper:** Handles prompt construction, timeouts, retries, and structured numeric output; required to keep LLM integration isolated and testable.
- **OwnedCardsStore (client):** Persist selected card ids (e.g., Proto DataStore) so `ManageCardsScreen` and `RecommendationSheetContent` share state; current UI-only `savedCardIds` list resets every session.
- **RecommendationRepository / use case (client):** Fetch `/api/recommendations`, map metadata to UI models, and expose discover + fallback state; present repositories do not cover recommendation payloads.

## Technical Approach
- **Database:** Add `is_location_based INTEGER NOT NULL DEFAULT 0` to `card_benefits`, index `(card_id, is_location_based)` and `(normalized_category, is_location_based)` for quick filtering, and update `loadCardsData` + `upsertCardsAndBenefits` to parse the CSV boolean. Extend refresh logs to capture recommendation schema version if needed.
- **API:** Implement `POST /api/recommendations` under `/api` with payload `{ storeId, storeName, storeCategory, ownedCardIds: number[], discover: boolean }`. Validate inputs (non-empty store metadata, ownedCardIds array of ints) and include optional `locationKeywords` when the UI passes merchant name overrides. Response: `{ data: [ { cardId, name, issuer, normalizedCategories, score, scoreSource: 'location'|'llm'|'fallback', rationale } ], meta: { limit, total, discover, latencyMs } }`. Add `X-RateLimit-*` headers aligned with standards.
- **Recommendation logic:**
  - Stage 1: Query `card_benefits` for `is_location_based` entries whose `keyword` matches store name/branch (case-insensitive) and assign fixed high weights (e.g., 100). Deduplicate per card.
  - Stage 2: For remaining owned cards (or all cards when `discover=true`), build prompt: `store_name`, `store_category`, `benefit_summary`. Gemini returns `{"score": 0-100, "explanation": "..."}`; clamp to range and tag as `llm`.
  - Stage 3: On timeout/error or `discover=true`, fall back to heuristic scoring: +30 for category match, +10 if keyword fuzzy-matches store, +5 per overlapping normalized category. Tag as `fallback`.
  - Normalize weights, sort descending, and include metadata about the contributing stages. Cache results for identical `(storeId, ownedCardIds hash, discover flag)` for ~5 minutes to reduce LLM usage.
- **Frontend:**
  - Create `RecommendationRepository` using Retrofit with `ApiService.getRecommendations` call (new DTOs). Update `CardsViewModel` (or a dedicated `RecommendationsViewModel`) to manage `RecommendationState` (loading/error/data, discover mode) and to read owned card ids from `OwnedCardsStore` shared with `ManageCardsScreen`.
  - Modify `RecommendationSheetContent` to show score badges, metadata (e.g., "Matched 롯데백화점 benefit" vs "LLM confidence"), fallback banner when `scoreSource=fallback`, and a sticky Discover button (Semantics role=Button, content description "Discover cards you don't own"). Ensure button triggers re-fetch with `discover=true` and disables while in-flight.
  - Surface error/snackbar when backend rejects payload (e.g., no owned cards) and instruct user to add cards.
- **Testing:**
  - Backend unit tests for weighting pipeline (location priority, fallback, metadata) and ingestion updates using sqlite memory DB.
  - Contract tests (supertest) for `/api/recommendations` covering valid request, validation errors, Discover branch, and LLM failure fallback (mock Gemini client).
  - Client instrumentation tests verifying `RecommendationSheetContent` shows Discover button and that fallback banner toggles when metadata indicates LLM failure. Snapshot tests optional per testing standard; prioritize behavior-focused tests only.

## Out of Scope
- Persisting full recommendation history or analytics dashboards.
- Multi-language recommendation copy or localization of rationale text.
- Full user account system or remote sync of owned cards (local persistence only for now).
- Additional map interactions or new store selection flows beyond invoking the existing bottom sheet.

## Success Criteria
- Location-based cards always appear above non-matching cards when such benefits exist, verified via automated test fixtures.
- `/api/recommendations` responds within 1.5s P95 (excluding LLM latency >1s), returns structured errors for invalid payloads, and includes score metadata.
- Android users can trigger Discover mode, see at least three candidate cards even when they own none, and receive clear fallback messaging when LLM scoring is skipped.
- Owned card selections persist across app restarts and immediately influence recommendation ordering without manual refresh.
