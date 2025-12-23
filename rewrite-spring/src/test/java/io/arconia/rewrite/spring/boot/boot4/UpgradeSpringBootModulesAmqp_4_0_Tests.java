package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesAmqp_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateAmqpModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
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
                              testImplementation "org.springframework.amqp:spring-rabbit-test"
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
                              testImplementation "org.springframework.boot:spring-boot-starter-amqp-test"
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
                        import org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
                        import org.springframework.boot.actuate.metrics.amqp.RabbitMetrics;
                        import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

                        class Demo {
                            RabbitHealthContributorAutoConfiguration autoConfiguration = null;
                            RabbitMetricsAutoConfiguration metricsAutoConfiguration = null;
                            RabbitHealthIndicator healthIndicator = null;
                            RabbitMetrics metrics = null;
                            RabbitAutoConfiguration rabbitAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
                        import org.springframework.boot.amqp.autoconfigure.health.RabbitHealthContributorAutoConfiguration;
                        import org.springframework.boot.amqp.autoconfigure.metrics.RabbitMetricsAutoConfiguration;
                        import org.springframework.boot.amqp.health.RabbitHealthIndicator;
                        import org.springframework.boot.amqp.metrics.RabbitMetrics;

                        class Demo {
                            RabbitHealthContributorAutoConfiguration autoConfiguration = null;
                            RabbitMetricsAutoConfiguration metricsAutoConfiguration = null;
                            RabbitHealthIndicator healthIndicator = null;
                            RabbitMetrics metrics = null;
                            RabbitAutoConfiguration rabbitAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
