# Codexa API Versioning Policy & Lifecycle

## Versioning Model
Codexa adheres to URI-based semantic versioning:
`/api/v{MAJOR}/{resource}`

The current stable API version is **`v1`**.

## Backwards Compatibility Guarantees
Within major versions (`v1`), the following changes are guaranteed non-breaking:
- Adding new optional request query parameters or request body fields.
- Adding new response attributes or headers.
- Introducing new enum values in rule catalogs or severity taxonomies.
- Adding new endpoints.

## Breaking Changes Policy
A change is considered breaking if:
- A field or endpoint is removed or renamed.
- A previously optional request field becomes mandatory.
- The data type of an existing response field changes.

Breaking changes will only occur in new major versions (e.g. `v2`).

## Deprecation Schedule
1. **Notice Period**: Deprecated endpoints are announced at least 6 months prior to removal.
2. **HTTP Deprecation Headers**:
   ```http
   Deprecation: @1726058400
   Sunset: Fri, 15 Jan 2027 00:00:00 GMT
   Link: </docs/api/v2-migration>; rel="sunset"
   ```
3. **Graceful Fallback**: Deprecated routes return clear guidance warnings in response metadata.
