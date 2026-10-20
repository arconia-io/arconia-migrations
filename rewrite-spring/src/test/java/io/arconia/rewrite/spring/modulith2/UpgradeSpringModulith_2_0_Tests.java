package io.arconia.rewrite.spring.modulith2;

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
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeSpringModulith_2_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.modulith2.UpgradeSpringModulith_2_0")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-modulith-api-1.4"));
    }

    @Test
    void applicationModuleListener() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.modulith.ApplicationModuleListener;

                        class Demo {
                            @ApplicationModuleListener
                            void on(Object event) {
                            }
                        }
                        """,
                        """
                        import org.springframework.modulith.events.ApplicationModuleListener;

                        class Demo {
                            @ApplicationModuleListener
                            void on(Object event) {
                            }
                        }
                        """
                )
        );
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
                            implementation "org.springframework.modulith:spring-modulith-starter-core:1.4.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-modulith dependency upgraded to 2.0.x")
                                    .containsPattern("org\\.springframework\\.modulith:spring-modulith-starter-core:2\\.0\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    @DocumentExample
    void eventPublicationPropertiesInProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.modulith.republish-outstanding-events-on-restart=true
                        spring.modulith.events.staleness.check-intervall=30s
                        spring.modulith.events.staleness.resubmission=5m
                        """,
                        """
                        spring.modulith.events.republish-outstanding-events-on-restart=true
                        spring.modulith.events.staleness.check-interval=30s
                        spring.modulith.events.staleness.resubmitted=5m
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void eventPublicationPropertiesInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        spring:
                          modulith:
                            republish-outstanding-events-on-restart: true
                            events:
                              staleness:
                                check-intervall: 30s
                                resubmission: 5m
                        """,
                        """
                        spring:
                          modulith:
                            events.republish-outstanding-events-on-restart: true
                            events:
                              staleness:
                                check-interval: 30s
                                resubmitted: 5m
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
