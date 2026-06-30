# 1. Recipe naming

Date: 2026-06-23

## Status

Accepted

## Context

Recipe identifiers, display names, and descriptions are public API. Users reference them in `rewrite.yml`, in build configuration, and through the Arconia CLI. Consistent naming keeps recipes discoverable and stable, and aligns the project with the wider OpenRewrite ecosystem it builds on.

## Decision

### Package and identifier

- Start every recipe package with `io.arconia.rewrite.<area>`, where `<area>` matches the Arconia Migration modules: `spring`, `testing`, `docling`, or `framework` (for Arconia Framework recipes).
- Where an area hosts several technologies that each maintain concurrent major-version trains, as `rewrite-spring` does, group recipes by a subarea that combines the technology and its major version in a single, collapsed segment: `spring.boot4`, `spring.ai1`, `spring.cloud2025`, `spring.framework7` (not the nested `spring.boot.boot4`).
- Where a single line evolves over time, keep a flat per-technology subarea and carry the version in the recipe name instead: `testing.junit`, `testing.testcontainers`, `docling`, `framework`.
- Group cross-version or functional helpers under a plain subarea: `spring.boot.properties`, `framework.opentelemetry`.
- Place an imperative recipe in the same package as the declarative recipes that compose it; its identifier is its fully qualified class name.
- Name classes in PascalCase. Capitalize only the first letter of an acronym within an identifier: `Ai`, `Mcp`, `Otlp`.
- Encode the target version with underscores, `_<major>` or `_<major>_<minor>`. Include the minor only when the recipe targets a specific minor, matching the upstream project's version granularity.

### Recipe forms

- Begin every recipe name with an imperative verb describing what it does. Aggregating, version-upgrade, and migration recipes use `Upgrade`, `Migrate`, `Add`, or `Remove`; a recipe that performs a single focused transformation may use another imperative verb, such as `Use`, `Find`, `Change`, or `Rename`.
- Name a version-upgrade recipe `Upgrade<Target>_<version>`, for example `UpgradeSpringBoot_4_0` or `UpgradeJUnit_6`.
- Name a recipe that migrates one part of a release `Migrate<Target><Component>_<version>`, for example `MigrateSpringBeans_6_2`.
- Name a migration between technologies `Migrate<Source>To<Target>`, for example `MigrateSpringBoot3OtlpToArconiaOpenTelemetry`.

### Display name and description

- Give every recipe a display name in the imperative mood, for example, "Migrate the Spring AI MCP module to Spring AI 1.1". Write acronyms in their conventional uppercase form: `AI`, `MCP`, `OTLP`.
- Give every recipe a description written as a complete sentence ending with a period.

## Consequences

Recipe identifiers are predictable and consistent with the wider OpenRewrite ecosystem. Renaming an identifier after a stable release is a breaking change and is handled through the deprecation process (see [2. Deprecating and removing recipes](0002-deprecating-and-removing-recipes.md)).
