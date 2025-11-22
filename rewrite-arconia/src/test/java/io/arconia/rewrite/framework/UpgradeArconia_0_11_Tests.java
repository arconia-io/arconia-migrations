package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_11_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_11")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "arconia-core-0.10.*"));
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
                    implementation 'io.arconia:arconia-spring-boot-starter:0.10.3'
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
                    implementation 'io.arconia:arconia-spring-boot-starter:0.11.0'
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
                        <version>0.10.3</version>
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
                        <version>0.11.0</version>
                      </dependency>
                    </dependencies>
                  </project>
                  """
            )
        );
    }

    // Dependency Changes

    @Test
    void changeLgtmDependencyGradle() {
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
                      implementation 'io.arconia:arconia-dev-services-opentelemetry-lgtm'
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
                      implementation 'io.arconia:arconia-dev-services-lgtm'
                  }
                  """
            )
        );
    }

    // Property Key Changes

    @Test
    void changePropertyKeys() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        reusable: true
                      ollama:
                        reusable: false
                      postgresql:
                        reusable: true
                      redis:
                        community:
                          environment:
                            MY_VAR: value
                          image-name: custom-redis:latest
                          reusable: true
                """,
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        shared: dev-mode
                      ollama:
                        shared: never
                      postgresql:
                        shared: dev-mode
                      redis:
                          environment:
                            MY_VAR: value
                          image-name: custom-redis:latest
                          shared: dev-mode
                """,
                s -> s.path("src/main/resources/application.yml")
            )
        );
    }

    // Property Value Changes

    @Test
    void changePropertyValuesLgtm() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        shared: true
                """,
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        shared: dev-mode
                """,
                s -> s.path("src/main/resources/application.yml")
            ),
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        shared: false
                """,
                """
                arconia:
                  dev:
                    services:
                      lgtm:
                        shared: never
                """,
                s -> s.path("src/main/resources/application-another.yml")
            )
        );
    }

    @Test
    void changePropertyValuesOllama() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      ollama:
                        shared: true
                """,
                """
                arconia:
                  dev:
                    services:
                      ollama:
                        shared: dev-mode
                """,
                s -> s.path("src/main/resources/application.yml")
            )
        );
    }

    @Test
    void changePropertyValuesPostgresql() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      postgresql:
                        shared: false
                """,
                """
                arconia:
                  dev:
                    services:
                      postgresql:
                        shared: never
                """,
                s -> s.path("src/main/resources/application.yml")
            )
        );
    }

    @Test
    void changePropertyValuesRedis() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      redis:
                        shared: true
                """,
                """
                arconia:
                  dev:
                    services:
                      redis:
                        shared: dev-mode
                """,
                s -> s.path("src/main/resources/application.yml")
            )
        );
    }

    // Delete Deprecated Properties

    @Test
    void deleteDeprecatedRedisStackProperties() {
        rewriteRun(
            //language=yaml
            yaml(
                """
                arconia:
                  dev:
                    services:
                      redis:
                        stack:
                          environment:
                            VAR1: val1
                          image-name: redis/redis-stack:latest
                          reusable: true
                        community:
                          image-name: redis:alpine
                """,
                """
                arconia:
                  dev:
                    services:
                      redis:
                          image-name: redis:alpine
                """,
                s -> s.path("src/main/resources/application.yml")
            )
        );
    }

}
