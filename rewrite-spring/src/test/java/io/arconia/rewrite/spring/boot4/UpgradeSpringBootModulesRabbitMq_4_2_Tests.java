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

class UpgradeSpringBootModulesRabbitMq_4_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSpringBootRabbitMq_4_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-amqp-4.1", "spring-boot-health-4.1"));
    }

    @DocumentExample
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
                            implementation "org.springframework.boot:spring-boot-starter-amqp"
                            implementation "org.springframework.boot:spring-boot-amqp"
                            testImplementation "org.springframework.boot:spring-boot-starter-amqp-test"
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
                            implementation "org.springframework.boot:spring-boot-starter-rabbitmq"
                            implementation "org.springframework.boot:spring-boot-rabbitmq"
                            testImplementation "org.springframework.boot:spring-boot-starter-rabbitmq-test"
                        }
                        """
                )
        );
    }

    @Test
    void packageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
                        import org.springframework.boot.amqp.autoconfigure.RabbitConnectionDetails;
                        import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
                        import org.springframework.boot.amqp.autoconfigure.health.RabbitHealthContributorAutoConfiguration;
                        import org.springframework.boot.amqp.autoconfigure.metrics.RabbitMetricsAutoConfiguration;
                        import org.springframework.boot.amqp.health.RabbitHealthIndicator;

                        class Demo {
                            RabbitAutoConfiguration autoConfiguration = null;
                            RabbitConnectionDetails connectionDetails = null;
                            RabbitProperties properties = null;
                            RabbitHealthContributorAutoConfiguration healthAutoConfiguration = null;
                            RabbitMetricsAutoConfiguration metricsAutoConfiguration = null;
                            RabbitHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.rabbitmq.autoconfigure.RabbitAutoConfiguration;
                        import org.springframework.boot.rabbitmq.autoconfigure.RabbitConnectionDetails;
                        import org.springframework.boot.rabbitmq.autoconfigure.RabbitProperties;
                        import org.springframework.boot.rabbitmq.autoconfigure.health.RabbitHealthContributorAutoConfiguration;
                        import org.springframework.boot.rabbitmq.autoconfigure.metrics.RabbitMetricsAutoConfiguration;
                        import org.springframework.boot.rabbitmq.health.RabbitHealthIndicator;

                        class Demo {
                            RabbitAutoConfiguration autoConfiguration = null;
                            RabbitConnectionDetails connectionDetails = null;
                            RabbitProperties properties = null;
                            RabbitHealthContributorAutoConfiguration healthAutoConfiguration = null;
                            RabbitMetricsAutoConfiguration metricsAutoConfiguration = null;
                            RabbitHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
