package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesCouchbase_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateCouchbaseModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.couchbase.CouchbaseHealthIndicator;

                        class Demo {
                            CouchbaseAutoConfiguration autoConfiguration = null;
                            CouchbaseHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            CouchbaseHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.couchbase.autoconfigure.CouchbaseAutoConfiguration;
                        import org.springframework.boot.couchbase.autoconfigure.health.CouchbaseHealthContributorAutoConfiguration;
                        import org.springframework.boot.couchbase.health.CouchbaseHealthIndicator;

                        class Demo {
                            CouchbaseAutoConfiguration autoConfiguration = null;
                            CouchbaseHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            CouchbaseHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
