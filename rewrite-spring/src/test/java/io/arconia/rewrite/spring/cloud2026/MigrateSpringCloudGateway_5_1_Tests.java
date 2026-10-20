package io.arconia.rewrite.spring.cloud2026;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateSpringCloudGateway_5_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2026.MigrateSpringCloudGateway_5_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-cloud-gateway-server-webflux-5.0"));
    }

    @Test
    @DocumentExample
    void retryBackoffWithoutBasedOnPreviousValue() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.time.Duration;

                        import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;

                        class Demo {
                            RetryGatewayFilterFactory.RetryConfig config = new RetryGatewayFilterFactory.RetryConfig()
                                    .setBackoff(Duration.ofMillis(10), Duration.ofMillis(50), 2, false);

                            RetryGatewayFilterFactory.BackoffConfig backoff =
                                    new RetryGatewayFilterFactory.BackoffConfig(Duration.ofMillis(10), Duration.ofMillis(50), 2, true);
                        }
                        """,
                        """
                        import java.time.Duration;

                        import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;

                        class Demo {
                            RetryGatewayFilterFactory.RetryConfig config = new RetryGatewayFilterFactory.RetryConfig()
                                    .setBackoff(Duration.ofMillis(10), Duration.ofMillis(50), 2);

                            RetryGatewayFilterFactory.BackoffConfig backoff =
                                    new RetryGatewayFilterFactory.BackoffConfig(Duration.ofMillis(10), Duration.ofMillis(50), 2);
                        }
                        """
                )
        );
    }

}
