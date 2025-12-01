# Spec Requirements: Card Recommendation Feature

## Initial Description
now i have to make the card recommendation feature to the app

## Requirements Discussion

### First Round Questions

**Q1:** I assume this feature includes both the recommendation logic (determining the best card) and the UI to display the result. Is that correct?
**Answer:** yes we need recommendation algorithm, and the ui. but we already have existing code for the recommended card ui, i want you to have that in mind

**Q2:** I'm thinking the recommendation logic should be based on matching the store's category to card benefits and sorting by the highest reward value (e.g., 5% > 1%). Is that the right approach, or should we handle it differently?
**Answer:** it kinda depends, the way benefits are stated at the data is obscure. the way it should be is weight based algorithm. first we should check if the selected store matches with "is_location_based" with the card data e.g. "롯데백화점", "롯데마트" etc and if so, we assign the cards that provides benefits for the place high weights. and then we move onto more general ones that shares same category with the selected store.

**Q3:** Since "User Credit Card Management" is a future roadmap item, how should we handle the user's portfolio for this feature? (A) Recommend from all cards in the database (discovery) (B) Implement a simple "select cards I own" mechanism now (C) Hardcode a test set?
**Answer:** we should calculate the weights among the ones that user has. and then if user clicks "discover" button at the bottom of the recommendation screen, we calculate weights of every card that shares same category and show user the potential candidates.

**Q4:** I'm thinking we should implement the recommendation logic in the local-backend (e.g., a new `/api/recommend` endpoint) to keep the client thin. Should we do that, or calculate it in the Android app?
**Answer:** yes, we should implement it at the local backend.

**Q5:** I assume the recommendation screen should appear immediately after the user confirms their store selection. Should this be a new full-screen page or a bottom sheet/modal (since we already use bottom sheets elsewhere)?
**Answer:** we already have the bottom sheet ui implemented(sort of). i'll provide you the picture later

### Existing Code to Reference
Based on the user's response:

**Similar Features Identified:**
- Feature: Recommendation Sheet UI (existing bottom sheet component) - Path: `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt` (`RecommendationSheetContent` composable) and `app/src/main/java/teamcherrypicker/com/ui/main/RecommendationScreen.kt`.
- Feature: Card Management feature (user portfolio selection) - Path: `app/src/main/java/teamcherrypicker/com/ui/cards/ManageCardsScreen.kt` (from provided screenshot reference) and related viewmodels; maintains list of cards user owns.

**Components to potentially reuse:**
- Existing `RecommendationCard` data models (`app/src/main/java/teamcherrypicker/com/data/RecommendedCard.kt`).
- Bottom sheet layout and chips/buttons used in recommendation screen.
- Card management list UI and storage to know which cards belong to the user.

**Backend logic to reference:**
- Local backend database schema loader `local-backend/src/ingest/loadData.js` and `local-backend/src/db.js` for extending schema to persist `is_location_based` flag.
- Upcoming `/api/cards` spec (2025-11-19) to align with API patterns and metadata envelopes.

### Follow-up Questions

**Follow-up 1:** I noticed the current database schema and ingestion script (`loadData.js`) do not store the `is_location_based` field from the CSV. To implement your weight-based algorithm, I'll need to update the database schema (`card_benefits` table) and ingestion logic to include this. Is that correct?
**Answer:** yes, you should update the db schema to include location based benefits

**Follow-up 2:** Since "User Credit Card Management" is not yet implemented, how should we determine "cards the user has" for this feature? Should I create a temporary table, hardcode IDs, or assume the user has all cards?
**Answer:** there are already card recommendation screen and card management feature. (Use the existing card management source of truth for owned cards.)

**Follow-up 3:** Is the "Discover" button a new element to be added to the existing `RecommendationSheetContent` bottom sheet? If so, should it be placed at the bottom of the list or fixed to the screen bottom?
**Answer:** the discover button should be located at the bottom of the list

**Follow-up 4:** Can we enhance the weighting by having an LLM evaluate card benefits for the selected store, and if so, which provider, what inputs, output format, fallback plan, and discover-mode behavior should we use?
**Answer:** use Gemini 2.5 Flash. Send store name, store category, and benefit_summary as prompt context. The model must respond with a numeric weight between 0 and 100. If the LLM call fails, fall back to simple category-based matching (show user-owned cards that share the store category). Discover mode should skip the LLM entirely for scalability and just reuse the fallback algorithm against all cards in the database.

## Visual Assets

No visual assets provided.

## Requirements Summary

### Functional Requirements
- Calculate recommendation weights in the backend using both `is_location_based` merchant matches and LLM-evaluated relevance (Gemini 2.5 Flash) for remaining benefits, normalizing outputs to a 0–100 scale.
- Always give exact `is_location_based` matches the highest deterministic weight before considering LLM-generated scores.
- Prioritize cards owned by the user (from existing card management data), returning ranked recommendations for the selected store.
- Provide a "Discover" action that fetches potential cards beyond the user's current portfolio by category overlap, using the fallback category-based algorithm only (no LLM calls) for scalability.
- Expose backend recommendation results via a new endpoint (e.g., `/api/recommendations`) consumed by the existing bottom sheet UI, including metadata about whether each score came from location, LLM, or fallback logic.
- Reuse the existing recommendation sheet UI: show ranked list, benefits summary, integrate the Discover button at the bottom of the list, and display fallback messaging when LLM scoring fails.

### Reusability Opportunities
- Reuse `RecommendationSheetContent`, `RecommendationList`, and card chip styles for consistency.
- Leverage card management data storage to determine user-owned cards when calculating weights.
- Align backend responses with the `/api/cards` response structure for metadata, pagination, and error envelopes.

### Scope Boundaries
**In Scope:**
- Backend schema changes (card benefits table, ingestion) to store `is_location_based` and any other fields needed for weighting.
- Weight-based recommendation algorithm covering location-specific matches first, then general category matches.
- API endpoint(s) and Android integration to display results in the existing bottom sheet, including Discover state.

**Out of Scope:**
- New full-screen recommendation layouts (bottom sheet enhancements only).
- Advanced filtering/sorting beyond owner vs discover scenarios.
- Persistent user analytics or recommendation history storage.

### Technical Considerations
- Database: Add `is_location_based` boolean column to `card_benefits`; update ingestion pipeline to populate it from CSV.
- Algorithm: Determine weights using merchant name matching (exact or fuzzy) for location-specific benefits; for remaining cards call Gemini 2.5 Flash with store name, store category, and benefit summary to obtain 0–100 scores; fall back to category-based weighting whenever the LLM request fails or when running Discover mode.
- LLM Integration: Configure backend support for Gemini 2.5 Flash (credential management, request batching, timeouts, retries, logging) and guardrails to limit usage.
- API: Implement new backend handler(s) in `local-backend` that accept store identifiers/category input, leverage user-owned card IDs, call the LLM where applicable, and return ranked lists with metadata describing the scoring method used.
- Client: Update viewmodels powering `RecommendationSheetContent` to call the new backend endpoint, show the Discover results, reuse existing card item components, and surface fallback messaging when LLM scoring is unavailable.
- UX: Ensure Discover button sits at the bottom of the recommendation list and triggers the full-card-set weighting flow (category-only scoring) without blocking on LLM calls.
