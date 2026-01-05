# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4] - 2026-01-04

### Added
- **supabase-db**: Added `single()` method to `SelectQuery` - returns exactly one row (type `T`), fails if 0 or multiple rows found
- **supabase-db**: Added `maybeSingle()` method to `SelectQuery` - returns one row or null (type `T?`), fails if multiple rows found
- **supabase-db**: Added `range(from, to)` pagination helper to `SelectQuery` for cleaner page-based queries

### Fixed
- **supabase-db**: Fixed DELETE query filter application. The `FilterBuilder.applyTo()` method now correctly applies filters to DELETE queries by iterating through operations instead of nesting filter blocks, resolving the "DELETE requires a WHERE clause" error.

### Changed
- **supabase-db**: Deprecated `executeSingle()` in favor of `single()` or `maybeSingle()` for clearer semantics

### Technical Details
- Changed `FilterBuilder.applyTo()` to apply each filter operation individually via `builder.filter { }` calls
- This ensures filters work correctly in both direct contexts (DELETE, UPDATE) and nested contexts (SELECT)
- DELETE queries with `.filter { eq("column", value) }` now properly include WHERE clauses in the SQL request

### Testing
- Added `FilterBuilderTest.kt` with unit tests validating filter operation storage
- Tests verify correct handling of: `eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `like`, `ilike`, `exact`, `inList`, and null values
- All FilterBuilder unit tests pass ✅
- Added `kotlinx-coroutines-test` dependency for test infrastructure

### Verified SDK Operations
The SDK provides all database operations required by consumer apps:
- ✅ SELECT with filters and count
- ✅ INSERT (single and batch)
- ✅ UPDATE (full item or partial values) with filters
- ✅ DELETE with filters (fixed in this release)
- ✅ UPSERT (single and batch) with conflict handling

## [1.0.3] - 2026-01-03

### Added
- Initial GitHub Packages release

## [1.0.2] - 2026-01-03

### Changed
- Version bump for package publication

## [1.0.1] - 2026-01-03

### Changed
- Version bump for package publication

## [1.0.0] - 2026-01-03

### Added
- Initial release of Supabase SDK
- Core module with client setup
- Database module with FilterBuilder DSL
- Authentication module
- Koin dependency injection module
- Auth UI Compose Multiplatform screens
