package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcTestJava;

class UpgradeSpringBootModulesRestTestClient_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateRestTestClientModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-3.5", "spring-boot-resttestclient-4.0"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.test.web.client.TestRestTemplate;

                        class Demo {
                            TestRestTemplate testRestTemplate = null;
                        }
                        """,
                        """
                        import org.springframework.boot.resttestclient.TestRestTemplate;

                        class Demo {
                            TestRestTemplate testRestTemplate = null;
                        }
                        """
                )
        );
    }

    @Test
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()).expectedCyclesThatMakeChanges(2),
                mavenProject("project",
                        srcTestJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import org.springframework.boot.test.web.client.TestRestTemplate;

                                        class Demo {
                                            TestRestTemplate testRestTemplate = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import org.springframework.boot.resttestclient.TestRestTemplate;

                                        class Demo {
                                            TestRestTemplate testRestTemplate = null;
                                        }
                                        """
                                )
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
                                    testImplementation "org.springframework.boot:spring-boot-restclient"
                                    testImplementation "org.springframework.boot:spring-boot-resttestclient"
                                }
                                """
                        )
                )
        );
    }

}
