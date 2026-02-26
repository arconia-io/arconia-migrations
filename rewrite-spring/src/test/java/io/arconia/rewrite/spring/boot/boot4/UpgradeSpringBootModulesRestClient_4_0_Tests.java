package io.arconia.rewrite.spring.boot.boot4;

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

class UpgradeSpringBootModulesRestClient_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateRestClientModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5"));
    }

    @Test
    void addRestClientStarterDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                            //language=java
                            java("""
                                package com.yourorg;

                                import org.springframework.boot.web.client.RestTemplateBuilder;

                                class Demo {
                                  RestTemplateBuilder builder = null;
                                }
                                """,
                                """
                                package com.yourorg;

                                import org.springframework.boot.restclient.RestTemplateBuilder;

                                class Demo {
                                  RestTemplateBuilder builder = null;
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
                                      implementation "org.springframework.boot:spring-boot-starter-restclient"

                                      testImplementation "org.springframework.boot:spring-boot-starter-restclient-test"
                                  }
                                  """
                        )
                )
        );
    }


}
