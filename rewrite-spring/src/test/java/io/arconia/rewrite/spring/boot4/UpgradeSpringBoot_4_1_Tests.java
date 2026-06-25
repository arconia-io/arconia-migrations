package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

class UpgradeSpringBoot_4_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1");
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
                            implementation "org.springframework.boot:spring-boot:4.0.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-boot dependency upgraded to 4.1.x")
                                    .containsPattern("org\\.springframework\\.boot:spring-boot:4\\.1\\.\\d+");
                            return actual;
                        })
                )
        );
    }

}
