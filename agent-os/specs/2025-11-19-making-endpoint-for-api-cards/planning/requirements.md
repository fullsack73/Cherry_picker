# Spec Requirements: Making Endpoint For Api Cards

## Initial Description
about making endpoint for /api/cards

## Requirements Discussion

### First Round Questions

**Q1:** I’m assuming the `/api/cards` endpoint should serve the card data we now ingest into SQLite (including issuer and benefit summaries) in JSON. Is that correct, or do you want to stick with the simpler mock payload for now?
**Answer:** yeah it should be JSON

**Q2:** I’m thinking the endpoint should support filtering by normalized category (e.g., `?category=DINING`) since our mapping table already provides that. Should we add this filter, and are there other filters you need (issuer, keyword, nearby merchants, etc.)?
**Answer:** yeah use that filter

**Q3:** I assume pagination will help keep payloads manageable—perhaps `limit`/`offset` with a sensible default like 25 items. Should we implement pagination, or is “return everything” acceptable for v1?
**Answer:** yes, implement pagination

**Q4:** I’m planning to include benefit details in the same response (so clients don’t make secondary requests). Should we embed each card’s benefits array, or do you prefer a lean card list with a separate endpoint for benefits?
**Answer:** seperate endpoint for benefits

**Q5:** I’m assuming the endpoint should be public like the current mock endpoints, with no authentication layer yet. Do you need any auth or rate limiting at this stage?
**Answer:** it should be public, no auth required

**Q6:** I’m considering returning metadata such as `lastRefreshedAt` or `dataSource` so the app can surface data freshness. Should we include those fields?
**Answer:** yes.

**Q7:** For errors, I’m leaning on standardized 4xx/5xx JSON responses (e.g., `{ "error": { "code": "...", "message": "..." } }`). Does that align with our conventions, or do you have an existing error envelope we should mirror?
**Answer:** yes, standard error responses will do

**Q8:** I’ll treat the old `/cards` route as deprecated once `/api/cards` is live. Should we remove the mock route entirely, or keep it temporarily for compatibility?
**Answer:** yeah remove the mock route entirely

**Q9:** Aside from the capabilities above, are there any specific exclusions or deferrals you want noted (e.g., multi-merchant aggregations, personalization in this endpoint, etc.)?
**Answer:** no.

### Existing Code to Reference
No similar existing features identified for reference.

### Follow-up Questions
No follow-up questions were required.

## Visual Assets

### Files Provided:
No visual assets provided.

## Requirements Summary

### Functional Requirements
- Provide a `/api/cards` endpoint returning card data in JSON sourced from the SQLite ingestion pipeline.
- Support filtering by normalized category via a `category` query parameter.
- Implement pagination controls (e.g., `limit` and `offset`) with reasonable defaults.
- Expose metadata such as last data refresh timestamp and source information in the response payload.
- Replace the existing `/cards` mock route with the new endpoint and remove the mock data.
- Deliver card benefit details via a separate dedicated endpoint rather than embedding them in `/api/cards`.

### Reusability Opportunities
- None explicitly identified; consider reviewing existing backend patterns when implementing.

### Scope Boundaries
**In Scope:**
- Building the production `/api/cards` endpoint with filtering, pagination, metadata, and standard error handling.
- Removing the mock `/cards` route.
- Preparing a separate benefits endpoint for clients to request benefit details.

**Out of Scope:**
- Additional filters beyond normalized category (e.g., issuer, keyword) for this iteration.
- Authentication, rate limiting, or personalization features.
- Advanced merchant aggregation logic or other future enhancements not mentioned by the user.

### Technical Considerations
- Endpoint remains public with no authentication requirements.
- Responses must conform to existing JSON conventions, including standardized error envelopes.
- Ensure metadata fields (such as `lastRefreshedAt`) are populated from refresh logs or other sources.
- Implement pagination logic aligned with database querying best practices.
