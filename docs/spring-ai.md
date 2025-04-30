# Spring AI

## Upgrade to Spring AI 1.0

When upgrading Spring AI to a newer version, refer to the [Upgrade Notes](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) to find out about newly introduced APIs, deprecated APIs, and information about breaking changes, if any.

> [!NOTE]
> The Arconia Migrations project is currently in active development and not GA yet. We're working hard to improve it and appreciate your patience as we refine the tool. Feel free to try it out and share your feedback!

The `io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0` recipe automates most of the breaking changes introduced in Spring AI M7 and M8. And it will be kept up to date with future releases of Spring AI until the release of 1.0.0 GA.

There are multiple options for applying such a recipe to your project. In any case, you might need to update the version number of the Spring AI BOM manually after running the recipe (e.g. `1.0.0-M6` -> `1.0.0-M8`) since it's not always updated automatically depending on how your project is configured.

### Using the Arconia CLI

Using the [Arconia CLI](https://arconia.io/docs/arconia-cli/latest/index.html), you can easily apply the recipe to your project as follows:

```shell
arconia update spring-ai
```

### Using the OpenRewrite Maven Plugin

Using the [OpenRewrite Maven Plugin](https://docs.openrewrite.org), you can apply the recipe to your project as follows:

```shell
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:LATEST \
  -Drewrite.activeRecipes=io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0
```

### Using the OpenRewrite Gradle Plugin

Using the [OpenRewrite Gradle Plugin](https://docs.openrewrite.org), you can apply the recipe to your project as follows.

First, create an `init.gradle` file in your Spring Boot project (root folder) with the following content:

```groovy
initscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2" }
    }
    dependencies {
        classpath("org.openrewrite:plugin:latest.release")
    }
}

rootProject {
    plugins.apply(org.openrewrite.gradle.RewritePlugin)
    dependencies {
        rewrite("io.arconia.migrations:rewrite-spring:latest.release")
    }

    afterEvaluate {
        if (repositories.isEmpty()) {
            repositories {
                mavenCentral()
            }
        }
    }
}
```

Then, run the following command.

```shell
./gradlew rewriteRun \
    --init-script init.gradle \
    -DactiveRecipe=io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0
```

Finally, you can remove the `init.gradle` file
