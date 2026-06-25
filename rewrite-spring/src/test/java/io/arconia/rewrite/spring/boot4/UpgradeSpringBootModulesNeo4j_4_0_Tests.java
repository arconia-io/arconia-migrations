package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesNeo4j_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateNeo4jModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-actuator-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.neo4j.Neo4jHealthIndicator;

                        class Demo {
                            Neo4jAutoConfiguration autoConfiguration = null;
                            Neo4jHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            Neo4jHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.neo4j.autoconfigure.Neo4jAutoConfiguration;
                        import org.springframework.boot.neo4j.autoconfigure.health.Neo4jHealthContributorAutoConfiguration;
                        import org.springframework.boot.neo4j.health.Neo4jHealthIndicator;

                        class Demo {
                            Neo4jAutoConfiguration autoConfiguration = null;
                            Neo4jHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            Neo4jHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
