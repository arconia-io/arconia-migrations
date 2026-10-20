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

class UpgradeSpringModulith_2_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.modulith2.UpgradeSpringModulith_2_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-modulith-observability-2.0", "micrometer-observation"));
    }

    @Test
    @DocumentExample
    void observabilityDependency() {
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
                            implementation "org.springframework.modulith:spring-modulith-observability"
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
                            implementation "org.springframework.modulith:spring-modulith-observability-core"
                        }
                        """
                )
        );
    }

    @Test
    void observabilityTypes() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.modulith.observability.support.ModulithContext;
                        import org.springframework.modulith.observability.support.ModulithObservationConvention;
                        import org.springframework.modulith.observability.support.ObservedModule;
                        import org.springframework.modulith.observability.support.ObservedModuleType;

                        class Demo {
                            ModulithContext context = null;
                            ModulithObservationConvention convention = null;
                            ObservedModule module = null;
                            ObservedModuleType type = null;
                        }
                        """,
                        """
                        import org.springframework.modulith.observability.ModulithContext;
                        import org.springframework.modulith.observability.ModulithObservationConvention;
                        import org.springframework.modulith.observability.ObservedModule;
                        import org.springframework.modulith.observability.ObservedModuleType;

                        class Demo {
                            ModulithContext context = null;
                            ModulithObservationConvention convention = null;
                            ObservedModule module = null;
                            ObservedModuleType type = null;
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
                            implementation "org.springframework.modulith:spring-modulith-starter-core:2.0.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-modulith dependency upgraded to 2.1.x")
                                    .containsPattern("org\\.springframework\\.modulith:spring-modulith-starter-core:2\\.1\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    void stalenessCheckIntervalTypo() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.modulith.events.staleness.check-intervall=30s
                        """,
                        """
                        spring.modulith.events.staleness.check-interval=30s
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
