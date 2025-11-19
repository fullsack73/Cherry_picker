# Specification: Cards & Merchants Data Integration

## Goal
Deliver end-to-end integration of the provided card benefit and merchant datasets so the Android app and local backend offer real recommendations and merchant discovery instead of mock data.

## User Stories
- As a cardholder, I want to browse real credit card benefits so that I can pick the best card for a purchase.
- As a user near a store, I want the map to surface merchants around me and show matching cards so that I can maximize rewards.
- As an operator, I want to refresh the card and merchant catalog from the CSV sources so that the experience stays accurate without code changes.

## Core Requirements
### Functional Requirements
- Ingest `cards_db.csv` and `merchants_db (1).csv`, normalize them, and persist the data for fast lookup.
- Replace mock `/stores` and `/cards` endpoints with RESTful endpoints that return paginated, filterable data from the ingested sources.
- Expose a recommendation endpoint that maps merchant benefit categories to card benefit categories and returns ranked cards with benefit details.
- Update the Android repositories, Retrofit services, and Compose screens to consume the new endpoints and render live data (map markers, bottom-sheet recommendations, manage-cards lists).
- Provide an ingestion refresh command or script so operators can reload updated CSVs without restarting the whole stack.
- Ensure search, category filtering, and favorite-card flows continue to function with the new data volume.

### Non-Functional Requirements
- Load and index the datasets within 5 seconds on server boot; subsequent API calls must respond within 300 ms for typical queries.
- Preserve Unicode text (Hangul) end-to-end and ensure fonts/rendering remain legible in the Android UI.
- Enforce consistent error handling with structured JSON errors and HTTP status codes across all new endpoints.
- Support at least 10k merchant records without exhausting mobile memory by paging and caching responses.

## Visual Design
- No new mockups provided; reuse existing UI layouts while feeding them real data.

## Reusable Components
### Existing Code to Leverage
- Android data models `CreditCard`, `RecommendedCard`, and Compose screens in `MainScreen.kt`, `ManageCardsScreen.kt`, `RecommendationScreen.kt`.
- Retrofit client setup in `ApiClient`/`ApiService` and location publishing logic in `MainActivity`.
- Express server skeleton and tests in `local-backend/index.js` and `local-backend/test`.

### New Components Required
- Data ingestion module (Node.js service + CLI) that streams CSVs, normalizes categories, and seeds the persistence layer.
- Persistence layer (SQLite via better-sqlite3 or similar) with tables for cards, benefits, merchants, and category mappings.
- Recommendation engine utility that joins merchant benefit categories to card benefit categories with configurable weightings.
- Android repository classes (e.g., `CardRepository`, `MerchantRepository`) encapsulating API access, caching, and mapping to UI models.
- Category-mapping configuration shared across backend and app to translate `카드혜택_분류` values into existing `CardCategory` enums.

## Technical Approach
- Database: Create SQLite schemas (`cards`, `card_benefits`, `merchants`, `merchant_categories`, `category_mappings`) and seed them during ingestion; index latitude/longitude and category columns for geospatial + category queries.
- API: Implement REST endpoints `/api/cards`, `/api/cards/{id}`, `/api/merchants`, `/api/merchants/{id}`, `/api/recommendations` with query parameters for pagination, category filters, keyword search, and location radius filtering; reuse `/api/location` for uploads.
- Frontend: Extend Retrofit interface to cover new endpoints, add DTOs and mappers to existing data classes, update Compose screens to display localized strings, and load map markers from `/api/merchants`; manage UI state with ViewModels and caching of paged lists.
- Testing: Expand supertest coverage for new routes and ingestion edge cases, add Jest unit tests for category mapping, create Android unit tests for repository mapping, and add an instrumentation smoke test that loads the map and verifies recommendations render.

## Out of Scope
- Real-time syncing or push notifications when data changes.
- Merchant or card CRUD interfaces beyond CSV ingestion.
- Personalization logic (user-specific rankings, spend tracking).
- Deploying the backend beyond the local development environment.

## Success Criteria
- Local backend boots, loads CSVs, and all new endpoints pass automated tests with real data.
- Android app displays merchants from the dataset on the map and populates recommendations and manage-card lists from live API responses.
- Category mapping produces sensible card suggestions for at least 90% of sampled merchant categories.
- Operators can rerun the ingestion command with updated CSVs and see changes reflected without code edits.
