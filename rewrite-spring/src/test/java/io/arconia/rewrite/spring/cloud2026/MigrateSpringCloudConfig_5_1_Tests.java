package io.arconia.rewrite.spring.cloud2026;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class MigrateSpringCloudConfig_5_1_Tests implements RewriteTest {

    //language=xml
    private static final String POM_WITH_SPRING_RETRY = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                        <version>5.0.6</version>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.retry</groupId>
                        <artifactId>spring-retry</artifactId>
                        <version>2.0.13</version>
                    </dependency>
                </dependencies>
            </project>
            """;

    //language=xml
    private static final String POM_WITHOUT_SPRING_RETRY = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                        <version>5.0.6</version>
                    </dependency>
                </dependencies>
            </project>
            """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2026.MigrateSpringCloudConfig_5_1");
    }

    @Test
    @DocumentExample
    void retryEnabledThroughSpringRetry() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=properties
                        properties(
                                """
                                spring.config.import=configserver:http://localhost:8888
                                spring.cloud.config.fail-fast=true
                                spring.cloud.config.retry.max-attempts=10
                                """,
                                """
                                spring.config.import=configserver:http://localhost:8888
                                spring.cloud.config.fail-fast=true
                                spring.cloud.config.retry.enabled=true
                                spring.cloud.config.retry.max-attempts=10
                                """,
                                s -> s.path("src/main/resources/application.properties")),
                        //language=yaml
                        yaml(
                                """
                                spring:
                                  cloud:
                                    config:
                                      fail-fast: true
                                      retry:
                                        max-attempts: 10
                                ---
                                spring:
                                  config:
                                    activate:
                                      on-profile: local
                                    import: "optional:configserver:"
                                """,
                                """
                                spring:
                                  cloud:
                                    config:
                                      fail-fast: true
                                      retry:
                                        max-attempts: 10
                                        enabled: true
                                ---
                                spring:
                                  config:
                                    activate:
                                      on-profile: local
                                    import: "optional:configserver:"
                                """,
                                s -> s.path("src/main/resources/application.yml"))
                )
        );
    }

    @Test
    void retryAddedWhenMissingInYaml() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=yaml
                        yaml(
                                """
                                spring:
                                  cloud:
                                    config:
                                      fail-fast: true
                                """,
                                """
                                spring:
                                  cloud:
                                    config:
                                      fail-fast: true
                                      retry:
                                        enabled: true
                                """,
                                s -> s.path("src/main/resources/application.yml"))
                )
        );
    }

    @Test
    void springRetryKeptWhenUsedInCode() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion()
                        .dependsOn("package org.springframework.retry.annotation; public @interface Retryable {}")),
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY),
                        //language=java
                        java(
                                """
                                import org.springframework.retry.annotation.Retryable;

                                class Demo {
                                    @Retryable
                                    void call() {
                                    }
                                }
                                """
                        ),
                        //language=properties
                        properties(
                                """
                                spring.cloud.config.fail-fast=true
                                """,
                                """
                                spring.cloud.config.fail-fast=true
                                spring.cloud.config.retry.enabled=true
                                """,
                                s -> s.path("src/main/resources/application.properties"))
                )
        );
    }

    @Test
    void explicitRetrySettingKept() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=properties
                        properties(
                                """
                                spring.cloud.config.fail-fast=true
                                spring.cloud.config.retry.enabled=false
                                """,
                                s -> s.path("src/main/resources/application.properties")),
                        //language=yaml
                        yaml(
                                """
                                spring:
                                  cloud:
                                    config:
                                      fail-fast: true
                                      retry:
                                        enabled: false
                                """,
                                s -> s.path("src/main/resources/application-prod.yml"))
                )
        );
    }

    @Test
    void retryNeverEnabledWithoutSpringRetry() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITHOUT_SPRING_RETRY),
                        //language=properties
                        properties(
                                """
                                spring.cloud.config.fail-fast=true
                                """,
                                s -> s.path("src/main/resources/application.properties"))
                )
        );
    }

    @Test
    void noFailFastLeavesConfigurationUnchanged() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=properties
                        properties(
                                """
                                spring.config.import=optional:configserver:http://localhost:8888
                                """,
                                s -> s.path("src/main/resources/application.properties"))
                )
        );
    }

}
