package io.arconia.rewrite.test.junit.junit6;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

/**
 * Unit tests for "io.arconia.rewrite.test.junit.UpgradeJUnit_6".
 */
class UpgradeJUnit6_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.test.junit.UpgradeJUnit_6");
    }

    @Test
    void dependencyChanges() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {

                            testImplementation "org.junit.platform:junit-platform-jfr"
                            testImplementation "org.junit.platform:junit-platform-runner"
                            testImplementation "org.junit.platform:junit-platform-suite-commons"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {

                            testRuntimeOnly "org.junit.platform:junit-platform-launcher"
                        }
                        """
                )
        );
    }

}
