package io.arconia.rewrite.spring.data2026;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

class UpgradeSpringData_2026_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.data2026.UpgradeSpringData_2026_0");
    }

    @Test
    @DocumentExample
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.data:spring-data-jpa:4.0.0"
                            implementation "org.springframework.data:spring-data-mongodb:5.0.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .containsPattern("spring-data-jpa:4\\.1\\.\\d+")
                                    .containsPattern("spring-data-mongodb:5\\.1\\.\\d+");
                            return actual;
                        })
                )
        );
    }

}
