package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.SourceSpecs.text;

class UpgradeSpringBoot_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0");
    }

    @Test
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
                            implementation "org.springframework.boot:spring-boot:3.5.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-boot dependency upgraded to 4.0.x")
                                    .containsPattern("org\\.springframework\\.boot:spring-boot:4\\.0\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void logbook() {
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
                            implementation platform("org.zalando:logbook-bom:3.12.3")
                            implementation "org.zalando:logbook-core:3.12.3"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("logbook-bom dependency upgraded to 4.x")
                                    .containsPattern("org\\.zalando:logbook-bom:4\\.\\d+\\.\\d+");
                            assertThat(actual)
                                    .as("logbook-core dependency upgraded to 4.x")
                                    .containsPattern("org\\.zalando:logbook-core:4\\.\\d+\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void jobrunr() {
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
                            implementation "org.jobrunr:jobrunr:7.5.3"
                            implementation "org.jobrunr:jobrunr-spring-boot-3-starter:7.5.3"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("jobrunr dependency upgraded to 8.x")
                                    .containsPattern("org\\.jobrunr:jobrunr:8\\.\\d+\\.\\d+");
                            assertThat(actual)
                                    .as("jobrunr starter migrated to Spring Boot 4 and upgraded to 8.x")
                                    .containsPattern("org\\.jobrunr:jobrunr-spring-boot-4-starter:8\\.\\d+\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void springdoc() {
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
                            implementation platform("org.springdoc:springdoc-openapi-bom:2.8.17")
                            implementation "org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.17"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("springdoc-openapi-bom dependency upgraded to 3.0.x")
                                    .containsPattern("org\\.springdoc:springdoc-openapi-bom:3\\.0\\.\\d+");
                            assertThat(actual)
                                    .as("springdoc-openapi-starter-webmvc-api dependency upgraded to 3.0.x")
                                    .containsPattern("org\\.springdoc:springdoc-openapi-starter-webmvc-api:3\\.0\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void typeChangesData() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.boot.autoconfigure.domain.EntityScan;

                        class Demo {
                            EntityScan entityScan = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.boot.persistence.autoconfigure.EntityScan;

                        class Demo {
                            EntityScan entityScan = null;
                        }
                        """
                )
        );
    }

    @Test
    void typeChangesSecurity() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5")),
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

                        class Demo {
                            PathRequest pathRequest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;

                        class Demo {
                            PathRequest pathRequest = null;
                        }
                        """
                )
        );
    }

}
