package io.arconia.rewrite.frameworks;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Unit tests for "io.arconia.rewrite.UpgradeArconia_0_12".
 */
class UpgradeArconia_0_12_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_12")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "arconia-opentelemetry-sdk-spring-boot-autoconfigure-0.11.0"));
    }

    // Dependency Version Changes

    @Test
    void upgradeArconiaDependencyVersionGradle() {
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
                    implementation 'io.arconia:arconia-spring-boot-starter:0.11.0'
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
                    implementation 'io.arconia:arconia-spring-boot-starter:0.12.0'
                }
                """
            )
        );
    }

    @Test
    void upgradeArconiaDependencyVersionMaven() {
        rewriteRun(
            //language=xml
            pomXml(
                """
                  <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <dependencies>
                      <dependency>
                        <groupId>io.arconia</groupId>
                        <artifactId>arconia-spring-boot-starter</artifactId>
                        <version>0.11.0</version>
                      </dependency>
                    </dependencies>
                  </project>
                  """,
                """
                  <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <dependencies>
                      <dependency>
                        <groupId>io.arconia</groupId>
                        <artifactId>arconia-spring-boot-starter</artifactId>
                        <version>0.12.0</version>
                      </dependency>
                    </dependencies>
                  </project>
                  """
            )
        );
    }

    // Type Changes

    @Test
    void openTelemetryResourceBuilderCustomizer() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.opentelemetry.autoconfigure.sdk.resource.SdkResourceBuilderCustomizer;

                        class Demo {
                            SdkResourceBuilderCustomizer customizer = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.opentelemetry.autoconfigure.sdk.resource.OpenTelemetryResourceBuilderCustomizer;

                        class Demo {
                            OpenTelemetryResourceBuilderCustomizer customizer = null;
                        }
                        """
                )
        );
    }

}
