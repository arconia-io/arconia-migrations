# 2. Deprecating and removing recipes

Date: 2026-06-23

## Status

Accepted

## Context

Recipe identifiers are public API (see [1. Recipe naming](0001-recipe-naming.md)). Users pin them in committed configuration and CI pipelines, and recipes build on upstream recipes that may change between releases. Evolving identifiers needs a predictable, published process.

## Decision

Releases use a `MAJOR.MINOR.PATCH` version format that follows the spirit of Semantic Versioning while keeping pace with OpenRewrite, whose catalog evolves recipes outside major releases. A backward-incompatible change may arrive in a minor release, wherever possible only after a deprecation period that gives advance notice and a migration path. This delivers improvements promptly without holding them back for a rare major version. The user-facing rules are published in the Versioning and Support documentation; this record captures the decision.

- Deprecate a recipe identifier in one minor release and remove it in the next. While deprecated, keep the identifier as an alias that delegates to its replacement and note the deprecation in the recipe description and the release notes.
- Support the current release train only: the latest minor receives patch releases; earlier minors do not.
- When an upstream change requires renaming or removing a recipe, apply the same deprecation process where possible.

## Consequences

Users who pin recipe identifiers can upgrade predictably, one minor at a time. Each rename keeps an alias for one minor. Supporting a single release line keeps maintenance sustainable; users on an earlier line upgrade to receive fixes.
