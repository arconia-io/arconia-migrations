package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.SourceSpecs.text;

class UpgradeSpringBootModulesCore_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateCoreModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5.*", "spring-boot-test-3.5.*", "spring-boot-test-autoconfigure-3.5.*"));
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
                              implementation "org.springframework.boot:spring-boot-starter"
                              testImplementation "org.springframework.boot:spring-boot-starter-test"
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
                          }
                          """
                )
        );
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.BootstrapRegistry;
                        import org.springframework.boot.env.EnvironmentPostProcessor;
                        import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;

                        class Demo {
                            BootstrapRegistry bootstrapRegistry = null;
                            EnvironmentPostProcessor environmentPostProcessor = null;
                            PropertyMapping propertyMapping = null;
                        }
                        """,
                        """
                        import org.springframework.boot.EnvironmentPostProcessor;
                        import org.springframework.boot.bootstrap.BootstrapRegistry;
                        import org.springframework.boot.test.context.PropertyMapping;

                        class Demo {
                            BootstrapRegistry bootstrapRegistry = null;
                            EnvironmentPostProcessor environmentPostProcessor = null;
                            PropertyMapping propertyMapping = null;
                        }
                        """
                ),
                //language=text
                text("""
                     org.springframework.boot.env.EnvironmentPostProcessor=\
                     io.arconia.openinference.observation.autoconfigure.ai.OpenInferenceEnvironmentPostProcessor
                     """,
                    """
                    org.springframework.boot.EnvironmentPostProcessor=\
                    io.arconia.openinference.observation.autoconfigure.ai.OpenInferenceEnvironmentPostProcessor
                    """,
                    s -> s.path("src/main/resources/META-INF/spring.factories"))
        );
    }

}
