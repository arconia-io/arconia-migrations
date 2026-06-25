package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesElasticsearch_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateElasticsearchModule")
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
                        import org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticsearchRestHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestClientHealthIndicator;
                        import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchConnectionDetails;
                        import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
                        import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;

                        class Demo {
                            ReactiveElasticsearchClientAutoConfiguration reactiveClientAutoConfiguration = null;
                            ElasticsearchConnectionDetails connectionDetails = null;
                            ElasticsearchProperties properties = null;
                            ElasticsearchRestClientAutoConfiguration restClientAutoConfiguration = null;
                            RestClientBuilderCustomizer restClientBuilderCustomizer = null;
                            ElasticsearchRestHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            ElasticsearchRestClientHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.elasticsearch.autoconfigure.*;
                        import org.springframework.boot.elasticsearch.autoconfigure.health.ElasticsearchRestHealthContributorAutoConfiguration;
                        import org.springframework.boot.elasticsearch.health.ElasticsearchRestClientHealthIndicator;

                        class Demo {
                            ElasticsearchClientAutoConfiguration reactiveClientAutoConfiguration = null;
                            ElasticsearchConnectionDetails connectionDetails = null;
                            ElasticsearchProperties properties = null;
                            ElasticsearchRestClientAutoConfiguration restClientAutoConfiguration = null;
                            Rest5ClientBuilderCustomizer restClientBuilderCustomizer = null;
                            ElasticsearchRestHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            ElasticsearchRestClientHealthIndicator healthIndicator = null;
                        }
                        """
                )
        );
    }

}
