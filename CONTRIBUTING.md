# Contributing to Arconia Migrations

Thank you for your interest in contributing! Whether it's reporting bugs, suggesting features, improving documentation, or submitting code, your input is welcome and valued.

## Ground Rules

* **Be Respectful.** Adhere to the [Code of Conduct](CODE_OF_CONDUCT.md).
* **Discuss First.** For non-trivial changes, [open an issue](https://github.com/arconia-io/arconia-migrations/issues/new/choose) before starting work. PRs without a prior discussion may be rejected.
* **Use Discussions for Questions.** The issue tracker is not for support questions. Use [GitHub Discussions](https://github.com/arconia-io/arconia-migrations/discussions) instead.
* **Focused PRs.** Each pull request should address a single issue or feature, linked to the corresponding issue (e.g., `Fixes #123`).
* **Security Vulnerabilities.** Report them responsibly via the [Security Policy](SECURITY.md). Do **not** open a public issue.

## Prerequisites

* Java 21+.
* A container runtime compatible with Testcontainers (e.g., [Podman Desktop](https://podman-desktop.io), [Docker Desktop](https://www.docker.com/products/docker-desktop/)).

## Contribution Workflow

1. **Find or create an issue.** Ensure a [GitHub Issue](https://github.com/arconia-io/arconia-migrations/issues) exists for the change you want to make.
2. **Fork and clone** the repository:
    ```shell
    git clone https://github.com/<your-username>/arconia-migrations.git
    cd arconia-migrations
    ```
3. **Create a branch** off `main`:
    ```shell
    git checkout -b my-feature-branch main
    ```
4. **Implement your changes.** Follow the [code style](#code-style) guidelines, add tests, and update documentation in `docs/` if needed.
5. **Build and test locally**:
    ```shell
    ./gradlew build
    ```
6. **Commit** using [Conventional Commits](#commit-messages) format with a [DCO sign-off](#dco-sign-off):
    ```shell
    git commit -s -m "feat(spring): Add recipe for Spring Boot 4.1"
    ```
7. **Keep your branch updated** via rebase (never merge). Your branch must be up to date with `main` before it can be merged:
    ```shell
    git fetch upstream
    git rebase upstream/main
    ```
8. **Push and open a PR** targeting `main`. Ensure the PR title follows [Conventional Commits](#commit-messages) format and fill out the PR template.
9. **Address review feedback.** Maintainers will review your PR. Push additional signed-off commits as needed, and resolve all review conversations before merge.

### How Pull Requests Are Merged

`main` is a protected branch with a linear history; direct pushes and force-pushes are not allowed. Maintainers merge an approved PR using rebase and merge, which preserves your commits (and their DCO sign-offs) on top of `main`. PRs made up of many small commits may be squashed into one at the maintainer's discretion.

Before a PR can merge, it must:

* Pass all required checks: build and tests, CodeQL analysis, and a [Conventional Commits](#commit-messages)-compliant PR title and commit messages.
* Be up to date with `main` (rebase if it has fallen behind).
* Have an approving review and all conversations resolved.

## Reporting Bugs and Suggesting Features

Use [GitHub Issues](https://github.com/arconia-io/arconia-migrations/issues/new/choose) with the appropriate template. Before submitting, search existing issues to avoid duplicates and ensure bugs are reproducible with the latest version.

## Development Guidelines

### Code Style

* The project uses [.editorconfig](/.editorconfig) for formatting. Ensure your editor respects it.
* Use explicit imports (no wildcards).
* Follow existing alphabetical sorting conventions.

### Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) for both commit messages and PR titles.

```
<type>[optional scope]: <description>
```

* **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `build`, `ci`, `chore`, `revert`, `deps`.
* **Style:** Present tense, imperative mood (e.g., "Add feature", not "Added feature").
* **Breaking changes:** Append `!` after the type/scope (e.g., `feat(spring)!:`) or add `BREAKING CHANGE:` in the footer.

Example: `feat(spring): Add recipe for Spring AI 2.0 upgrade`

### Authoring a Recipe

The [Recipe Authoring Guide](RECIPE_AUTHORING.md) walks through the module and resource layout, generating Spring property-migration recipes and wrapping them, the parser classpath and TypeTable setup, and writing `RewriteTest` tests. Start there when adding or updating a recipe.

### Naming a Recipe

Recipe identifiers follow the conventions in [ADR 1: Recipe naming](adr/0001-recipe-naming.md): package namespace, the `Upgrade`/`Migrate` patterns, version encoding, acronym capitalization, and the required `displayName` and `description`. Read it before adding a recipe.

### Deprecating a Recipe

Recipe identifiers are part of the project's public API, so they cannot be renamed or removed without notice. [ADR 2: Deprecating and removing recipes](adr/0002-deprecating-and-removing-recipes.md) and the [Versioning and Support](https://docs.arconia.io/arconia-migrations/latest/versioning.html) policy define the deprecation cycle: keeping the old identifier as an alias in one minor release, removing it in the next, and routing upstream OpenRewrite renames or removals through the same process. Read them before renaming or removing a recipe.

### DCO Sign-off

All commits must include a [Developer Certificate of Origin](dco.txt) sign-off. Commits without it will block merging.

```shell
# Sign-off a new commit
git commit -s -m "Your commit message"

# Add sign-off to the last commit
git commit --amend -s --no-edit
```
