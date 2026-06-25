package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJdbc_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJdbcModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthIndicatorProperties;
                        import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
                        import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
                        import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
                        import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
                        import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
                        import org.springframework.boot.actuate.metrics.jdbc.DataSourcePoolMetrics;
                        import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

                        class Demo {
                            DataSourceHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            DataSourceHealthIndicatorProperties healthIndicatorProperties = null;
                            DataSourcePoolMetricsAutoConfiguration poolMetricsAutoConfiguration = null;
                            DataSourcePoolMetadataProvidersConfiguration poolMetadataProvidersConfiguration = null;
                            DataSourceAutoConfiguration dataSourceAutoConfiguration = null;
                            DataSourceProperties dataSourceProperties = null;
                            DataSourceTransactionManagerAutoConfiguration transactionManagerAutoConfiguration = null;
                            EmbeddedDataSourceConfiguration embeddedDataSourceConfiguration = null;
                            JdbcClientAutoConfiguration jdbcClientAutoConfiguration = null;
                            JdbcConnectionDetails jdbcConnectionDetails = null;
                            JdbcProperties jdbcProperties = null;
                            JdbcTemplateAutoConfiguration jdbcTemplateAutoConfiguration = null;
                            JndiDataSourceAutoConfiguration jndiDataSourceAutoConfiguration = null;
                            XADataSourceAutoConfiguration xaDataSourceAutoConfiguration = null;
                            DataSourceHealthIndicator healthIndicator = null;
                            DataSourcePoolMetrics poolMetrics = null;
                            JdbcTest jdbcTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jdbc.autoconfigure.*;
                        import org.springframework.boot.jdbc.autoconfigure.health.DataSourceHealthContributorAutoConfiguration;
                        import org.springframework.boot.jdbc.autoconfigure.health.DataSourceHealthIndicatorProperties;
                        import org.springframework.boot.jdbc.autoconfigure.metrics.DataSourcePoolMetricsAutoConfiguration;
                        import org.springframework.boot.jdbc.health.DataSourceHealthIndicator;
                        import org.springframework.boot.jdbc.metrics.DataSourcePoolMetrics;
                        import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;

                        class Demo {
                            DataSourceHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            DataSourceHealthIndicatorProperties healthIndicatorProperties = null;
                            DataSourcePoolMetricsAutoConfiguration poolMetricsAutoConfiguration = null;
                            DataSourcePoolMetadataProvidersConfiguration poolMetadataProvidersConfiguration = null;
                            DataSourceAutoConfiguration dataSourceAutoConfiguration = null;
                            DataSourceProperties dataSourceProperties = null;
                            DataSourceTransactionManagerAutoConfiguration transactionManagerAutoConfiguration = null;
                            EmbeddedDataSourceConfiguration embeddedDataSourceConfiguration = null;
                            JdbcClientAutoConfiguration jdbcClientAutoConfiguration = null;
                            JdbcConnectionDetails jdbcConnectionDetails = null;
                            JdbcProperties jdbcProperties = null;
                            JdbcTemplateAutoConfiguration jdbcTemplateAutoConfiguration = null;
                            JndiDataSourceAutoConfiguration jndiDataSourceAutoConfiguration = null;
                            XADataSourceAutoConfiguration xaDataSourceAutoConfiguration = null;
                            DataSourceHealthIndicator healthIndicator = null;
                            DataSourcePoolMetrics poolMetrics = null;
                            JdbcTest jdbcTest = null;
                        }
                        """
                )
        );
    }

}
