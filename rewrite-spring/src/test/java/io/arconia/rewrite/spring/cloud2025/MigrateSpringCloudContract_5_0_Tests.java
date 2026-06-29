package io.arconia.rewrite.spring.cloud2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcTestJava;

class MigrateSpringCloudContract_5_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2025.MigrateSpringCloudContract_5_0");
    }

    @Test
    @DocumentExample
    void autoConfigureWireMockMigratedToEnableWireMock() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-cloud-contract-wiremock-4.3")),
                mavenProject("project",
                        srcTestJava(
                                //language=java
                                java(
                                        """
                                        package com.example;

                                        import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

                                        @AutoConfigureWireMock(port = 0)
                                        class DemoTests {
                                        }
                                        """,
                                        """
                                        package com.example;

                                        import org.wiremock.spring.EnableWireMock;

                                        @EnableWireMock
                                        class DemoTests {
                                        }
                                        """
                                )
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id 'java-library'
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                spec -> spec.after(actual -> {
                                    assertThat(actual)
                                            .as("wiremock-spring-boot test dependency added")
                                            .containsPattern("testImplementation \"org\\.wiremock\\.integrations:wiremock-spring-boot:3\\.\\d+\\.\\d+\"");
                                    return actual;
                                })
                        )
                )
        );
    }

    @Test
    void wiremockStandaloneWithHardcodedVersion() {
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
                            testImplementation "com.github.tomakehurst:wiremock-jre8-standalone:2.35.2"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("wiremock-jre8-standalone migrated to org.wiremock:wiremock-standalone 3.x")
                                    .containsPattern("org\\.wiremock:wiremock-standalone:3\\.\\d+\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void wiremockStandaloneWithManagedVersion() {
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
                            testImplementation "com.github.tomakehurst:wiremock-jre8-standalone"
                        }
                        """,
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            testImplementation "org.wiremock:wiremock-standalone"
                        }
                        """
                )
        );
    }

}
