# Spring Boot

## Upgrade to Spring Boot 4.0

> [!NOTE]
> The Arconia Migrations project is currently in active development and not GA yet. We're working hard to improve it and appreciate your patience as we refine the tool. Feel free to try it out and share your feedback!

The `io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0` recipe automates most of the changes introduced in Spring Boot 4.0. You can use it to update your project to the latest Spring Boot 4.0.x release.

The recipe will:

* Upgrade Spring Framework to 7.0 (Arconia Recipe)
* Upgrade Jackson to 3.0 ([OpenRewrite Recipe](https://docs.openrewrite.org/recipes/java/jackson/upgradejackson_2_3))
* Upgrade JUnit to 6.0 (Arconia Recipe)
* Upgrade Testcontainers to 2.0 (Arconia Recipe)
* Upgrade Spring Boot to 4.0 (Arconia Recipe).

If your project uses Gradle, the recipe will also:

* Upgrade Gradle to 9.x ([OpenRewrite Recipe](https://docs.openrewrite.org/recipes/gradle/updategradlewrapper))

The recipes use the foundational recipes from [OpenRewrite](https://docs.openrewrite.org), including recipes for working with Java, Gradle, and Maven.

There are multiple options for applying such a recipe to your project. In any case, you might need to update the version number of the Spring Boot BOM manually after running the recipe (e.g. `3.5.8` -> `4.0.0`) since it's not always updated automatically depending on how your project is configured.

### Using the Arconia CLI

Using the [Arconia CLI](https://arconia.io/docs/arconia-cli/latest/index.html), you can easily apply the recipe to your project as follows:

```shell
arconia update spring-boot --to-version 4.0
```

### Using the OpenRewrite Maven Plugin

Using the [OpenRewrite Maven Plugin](https://docs.openrewrite.org), you can apply the recipe to your project as follows:

```shell
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:LATEST \
  -Drewrite.activeRecipes=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0
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
    -DactiveRecipe=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0
```

Finally, you can remove the `init.gradle` file
