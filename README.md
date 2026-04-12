<p align="center">
  <img src="arconia-logo.png" alt="Arconia" width="200" />
</p>

<h1 align="center">Arconia Migrations</h1>

<p align="center">
  <a href="https://docs.openrewrite.org">OpenRewrite</a> recipes to automate refactoring, migrations, and upgrades for <a href="https://dev.java">Java</a>, <a href="https://spring.io/projects/spring-boot">Spring Boot</a>, and <a href="https://docs.arconia.io/arconia/latest/index.html">Arconia</a> projects.
</p>

<p align="center">
  <a href="https://github.com/arconia-io/arconia-migrations/actions/workflows/commit-stage.yml"><img src="https://github.com/arconia-io/arconia-migrations/actions/workflows/commit-stage.yml/badge.svg" alt="Build" /></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="Apache 2.0 License" /></a>
  <a href="https://bsky.app/profile/arconia.io"><img src="https://img.shields.io/static/v1?label=Bluesky&message=Follow&color=1DA1F2" alt="Follow us on Bluesky" /></a>
</p>

---

## ✨&nbsp; Features

- **Spring Boot.** Recipes for upgrading Spring Boot 3.x and migrating to Spring Boot 4.0.
- **Spring AI.** Recipes for upgrading Spring AI 1.x and migrating to Spring AI 2.0.
- **Spring Framework & Cloud.** Recipes for Spring Framework 6.x/7.x and Spring Cloud 2025.x.
- **Arconia.** Recipes for upgrading the Arconia Framework across versions.
- **Docling.** Recipes for upgrading the Docling Java library.
- **Testing.** Recipes for JUnit 6 and Testcontainers 2.

## 📦&nbsp; Modules

| Module | Description |
|---|---|
| `rewrite-arconia` | Recipes for [Arconia](https://docs.arconia.io) |
| `rewrite-docling` | Recipes for [Docling Java](https://github.com/docling-project/docling-java) |
| `rewrite-spring` | Recipes for [Spring AI](https://docs.spring.io/spring-ai/reference/), [Spring Boot](https://spring.io/projects/spring-boot), [Spring Cloud](https://spring.io/projects/spring-cloud), [Spring Framework](https://spring.io/projects/spring-framework) |
| `rewrite-test` | Recipes for [JUnit](https://junit.org) and [Testcontainers](https://testcontainers.com) |

## ⚡&nbsp; Quick Start

You can run Arconia Migrations recipes using the [Arconia CLI](https://github.com/arconia-io/arconia-cli):

```shell
arconia rewrite run --recipe-name io.arconia.rewrite.spring.boot.UpgradeSpringBoot_4_0
```

> [!NOTE]
> The Arconia Migrations project is currently in active development. We're working hard to improve it and appreciate your patience as we refine the tool. Feel free to try it out and share your feedback!

## 📙&nbsp; Documentation

The [Arconia documentation](https://arconia.io) covers all available recipes and usage details.

## 🤝&nbsp; Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md) before getting started.

## 🛡️&nbsp; Security

The security process for reporting vulnerabilities is described in [SECURITY.md](SECURITY.md).

## 🖊️&nbsp; License

This project is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) for more information.
