package io.arconia.rewrite.framework.opentelemetry;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.mavenProject;

class AddArconiaOpenTelemetryTestStarterTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.opentelemetry.AddArconiaOpenTelemetryTestStarter")
                .beforeRecipe(withToolingApi());
    }

    @Test
    @DocumentExample
    void testStarterAdded() {
        rewriteRun(
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }

                                dependencies {
                                    implementation "io.arconia:arconia-opentelemetry-spring-boot-starter"
                                    testImplementation "org.springframework.boot:spring-boot-starter-test"
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
                                    implementation "io.arconia:arconia-opentelemetry-spring-boot-starter"
                                    testImplementation "io.arconia:arconia-opentelemetry-spring-boot-starter-test"
                                    testImplementation "org.springframework.boot:spring-boot-starter-test"
                                }
                                """
                        ))
        );
    }

    @Test
    void testStarterNotAddedTwice() {
        rewriteRun(
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }

                                dependencies {
                                    implementation "io.arconia:arconia-opentelemetry-spring-boot-starter"
                                    testImplementation "io.arconia:arconia-opentelemetry-spring-boot-starter-test"
                                }
                                """
                        ))
        );
    }

    @Test
    void testStarterNotAddedWithoutTheStarter() {
        rewriteRun(
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }

                                dependencies {
                                    implementation "io.arconia:arconia-spring-boot-starter"
                                    testImplementation "org.springframework.boot:spring-boot-starter-test"
                                }
                                """
                        ))
        );
    }

}
