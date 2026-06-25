package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesR2dbc_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateR2dbcModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5", "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.metrics.r2dbc.ConnectionPoolMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.r2dbc.ConnectionFactoryHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.r2dbc.R2dbcObservationAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.r2dbc.R2dbcObservationProperties;
                        import org.springframework.boot.actuate.metrics.r2dbc.ConnectionPoolMetrics;
                        import org.springframework.boot.actuate.r2dbc.ConnectionFactoryHealthIndicator;
                        import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

                        class Demo {
                            ConnectionPoolMetricsAutoConfiguration connectionPoolMetricsAutoConfiguration = null;
                            ConnectionFactoryHealthContributorAutoConfiguration connectionFactoryHealthContributorAutoConfiguration = null;
                            R2dbcObservationAutoConfiguration r2dbcObservationAutoConfiguration = null;
                            R2dbcObservationProperties r2dbcObservationProperties = null;
                            ConnectionPoolMetrics connectionPoolMetrics = null;
                            ConnectionFactoryHealthIndicator connectionFactoryHealthIndicator = null;
                            R2dbcAutoConfiguration r2dbcAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
                        import org.springframework.boot.r2dbc.autoconfigure.health.ConnectionFactoryHealthContributorAutoConfiguration;
                        import org.springframework.boot.r2dbc.autoconfigure.metrics.ConnectionPoolMetricsAutoConfiguration;
                        import org.springframework.boot.r2dbc.autoconfigure.observation.R2dbcObservationAutoConfiguration;
                        import org.springframework.boot.r2dbc.autoconfigure.observation.R2dbcObservationProperties;
                        import org.springframework.boot.r2dbc.health.ConnectionFactoryHealthIndicator;
                        import org.springframework.boot.r2dbc.metrics.ConnectionPoolMetrics;

                        class Demo {
                            ConnectionPoolMetricsAutoConfiguration connectionPoolMetricsAutoConfiguration = null;
                            ConnectionFactoryHealthContributorAutoConfiguration connectionFactoryHealthContributorAutoConfiguration = null;
                            R2dbcObservationAutoConfiguration r2dbcObservationAutoConfiguration = null;
                            R2dbcObservationProperties r2dbcObservationProperties = null;
                            ConnectionPoolMetrics connectionPoolMetrics = null;
                            ConnectionFactoryHealthIndicator connectionFactoryHealthIndicator = null;
                            R2dbcAutoConfiguration r2dbcAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
