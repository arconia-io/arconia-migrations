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

class UpgradeSpringBootModulesKafka_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateKafkaModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration;

                        class Demo {
                            KafkaAutoConfiguration autoConfiguration = null;
                            KafkaMetricsAutoConfiguration metricsAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
                        import org.springframework.boot.kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration;

                        class Demo {
                            KafkaAutoConfiguration autoConfiguration = null;
                            KafkaMetricsAutoConfiguration metricsAutoConfiguration = null;
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
                            implementation "org.springframework.kafka:spring-kafka"
                            testImplementation "org.springframework.kafka:spring-kafka-test"
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
                            implementation "org.springframework.boot:spring-boot-starter-kafka"
                            testImplementation "org.springframework.boot:spring-boot-starter-kafka-test"
                        }
                        """
                )
        );
    }

}
