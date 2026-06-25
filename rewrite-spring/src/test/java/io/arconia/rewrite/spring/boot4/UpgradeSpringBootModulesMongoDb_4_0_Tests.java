package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMongoDb_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMongoDbModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5", "spring-boot-actuator-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.data.mongo.MongoHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.mongo.MongoMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.data.mongo.MongoHealthIndicator;
                        import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

                        class Demo {
                            MongoAutoConfiguration autoConfiguration = null;
                            MongoHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            MongoMetricsAutoConfiguration metricsAutoConfiguration = null;
                            MongoHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.mongodb.autoconfigure.health.MongoHealthContributorAutoConfiguration;
                        import org.springframework.boot.mongodb.autoconfigure.metrics.MongoMetricsAutoConfiguration;
                        import org.springframework.boot.mongodb.health.MongoHealthIndicator;
                        import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;

                        class Demo {
                            MongoAutoConfiguration autoConfiguration = null;
                            MongoHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            MongoMetricsAutoConfiguration metricsAutoConfiguration = null;
                            MongoHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
