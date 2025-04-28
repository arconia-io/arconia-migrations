package io.arconia.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_10_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_10")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "arconia-core-0.9.2"));
    }

    // Dependency Changes

    @Test
    void doesNotChangeDevToolsDependency() {
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
                              implementation 'org.springframework.boot:spring-boot-devtools'
                          }
                          """
                )
        );
    }

    @Test
    void changeDevToolsDependency() {
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
                              implementation 'io.arconia:arconia-dev-tools'
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
                              implementation 'org.springframework.boot:spring-boot-devtools'
                          }
                          """
                )
        );
    }

    @Test
    void doesNotAddStarterDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    class Demo {
                                      String hostInfo = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """
                        )
                )
        );
    }

    @Test
    void addsStarterDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import io.arconia.core.info.HostInfo;

                                    class Demo {
                                      HostInfo hostInfo = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
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
                                    implementation "io.arconia:arconia-spring-boot-starter:0.10.1"
                                }
                                """
                        )
                )
        );
    }

    // Property Changes

    @Test
    void doesNotChangeProperties() {
        rewriteRun(
                //language=yaml
                yaml("""
                        arconia:
                          config:
                            profiles:
                              development: dev
                              test: local
                              production: prof
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void changeProperties() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            profiles:
                              development: dev
                              test: local
                        """,
                        """
                        arconia:
                          config.profiles.development: dev
                          config.profiles.test: local
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
