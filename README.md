<p align="center">
  <img src="arconia-logo.png" alt="Arconia" width="200" />
</p>

<h1 align="center">Arconia Migrations</h1>

<p align="center">
  100% open-source (Apache 2.0) <a href="https://docs.openrewrite.org">OpenRewrite</a> recipes to automate refactoring, migrations, and upgrades for <a href="https://dev.java">Java</a>, <a href="https://spring.io/projects/spring-boot">Spring Boot</a>, and <a href="https://docs.arconia.io/arconia/latest/index.html">Arconia</a> projects.
</p>

<p align="center">
  <a href="https://github.com/arconia-io/arconia-migrations/actions/workflows/commit-stage.yml?query=branch%3Amain"><img src="https://img.shields.io/github/actions/workflow/status/arconia-io/arconia-migrations/commit-stage.yml?branch=main&logo=GitHub&label=Build" alt="Build" /></a>
  <a href="https://central.sonatype.com/namespace/io.arconia.migrations"><img src="https://img.shields.io/maven-central/v/io.arconia.migrations/rewrite-bom?logo=apache%20maven&label=Maven%20Central&color=blue" alt="Maven Central" /></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="Apache 2.0 License" /></a>
  <a href="https://bsky.app/profile/arconia.io"><img src="https://img.shields.io/static/v1?label=Bluesky&message=Follow&color=1DA1F2" alt="Follow us on Bluesky" /></a>
</p>

---

## 🌱&nbsp; Why Arconia Migrations?

Arconia Migrations provides **Apache 2.0 licensed** [OpenRewrite](https://docs.openrewrite.org) recipes for Java, Spring Boot, and Arconia upgrades. It builds on the Apache 2.0 recipes in the OpenRewrite catalog and contributes back to them where it can, as with `rewrite-jackson`. To cover Spring and testing upgrades for which no Apache 2.0 module was available, it introduces its own `rewrite-spring` and `rewrite-test` modules — independent of the similarly-named upstream modules distributed under the Moderne Source License.

Read more in the [Vision](https://docs.arconia.io/arconia-migrations/latest/vision.html).

## ✨&nbsp; Features

- **Spring Boot.** Recipes for upgrading to Spring Boot 3.x and 4.x.
- **Spring AI.** Recipes for upgrading to Spring AI 1.x and 2.x.
- **Spring Framework.** Recipes for upgrading to Spring Framework 6.x and 7.x.
- **Spring Cloud.** Recipes for upgrading to Spring Cloud 2025.x.
- **Arconia.** Recipes for upgrading the Arconia Framework across versions.
- **Docling.** Recipes for upgrading the Docling Java library.
- **Testing.** Recipes for JUnit 6 and Testcontainers 2.

> [!NOTE]
> Arconia Migrations is currently under active development. We're working hard to improve it and appreciate your patience as we continue to refine the project. Feel free to try it out and share your feedback!

## 📙&nbsp; Documentation

The [Arconia Migrations documentation](https://docs.arconia.io/arconia-migrations/latest/index.html) covers all available recipes, supported migrations, and usage details.

## 🤝&nbsp; Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md) before getting started.

## 🛡️&nbsp; Security

The security process for reporting vulnerabilities is described in [SECURITY.md](SECURITY.md).

## 🖊️&nbsp; License

This project is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) for more information.
