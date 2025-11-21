package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesCassandra_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateCassandraModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5.*", "spring-boot-autoconfigure-3.5.*",
                        "spring-boot-actuator-autoconfigure-3.5.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.cassandra.CassandraHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.cassandra.CassandraDriverHealthIndicator;

                        class Demo {
                            CassandraAutoConfiguration autoConfiguration = null;
                            CassandraHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            CassandraDriverHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration;
                        import org.springframework.boot.cassandra.autoconfigure.health.CassandraHealthContributorAutoConfiguration;
                        import org.springframework.boot.cassandra.health.CassandraDriverHealthIndicator;

                        class Demo {
                            CassandraAutoConfiguration autoConfiguration = null;
                            CassandraHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            CassandraDriverHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
