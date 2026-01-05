# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4] - 2026-01-04

### Fixed
- **supabase-db**: Fixed DELETE query filter application. The `FilterBuilder.applyTo()` method now correctly applies filters to DELETE queries by iterating through operations instead of nesting filter blocks, resolving the "DELETE requires a WHERE clause" error.

### Technical Details
- Changed `FilterBuilder.applyTo()` to apply each filter operation individually via `builder.filter { }` calls
- This ensures filters work correctly in both direct contexts (DELETE, UPDATE) and nested contexts (SELECT)
- DELETE queries with `.filter { eq("column", value) }` now properly include WHERE clauses in the SQL request

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
