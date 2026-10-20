package io.arconia.rewrite.spring.security7;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

class UpgradeSpringSecurity_7_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.security7.UpgradeSpringSecurity_7_1");
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
                            implementation "org.springframework.security:spring-security-core:7.0.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-security dependency upgraded to 7.1.x")
                                    .containsPattern("org\\.springframework\\.security:spring-security-core:7\\.1\\.\\d+");
                            return actual;
                        })
                )
        );
    }

}
