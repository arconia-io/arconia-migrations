package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataElasticsearch_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataElasticsearchModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5", "spring-boot-actuator-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.data.elasticsearch.ElasticsearchReactiveHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.data.elasticsearch.ElasticsearchReactiveHealthIndicator;
                        import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.elasticsearch.AutoConfigureDataElasticsearch;
                        import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;

                        class Demo {
                            ElasticsearchReactiveHealthContributorAutoConfiguration reactiveHealthContributorAutoConfiguration = null;
                            ElasticsearchReactiveHealthIndicator reactiveHealthIndicator = null;
                            ElasticsearchDataAutoConfiguration dataAutoConfiguration = null;
                            ElasticsearchRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            ReactiveElasticsearchRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            AutoConfigureDataElasticsearch autoConfigureDataElasticsearch = null;
                            DataElasticsearchTest dataElasticsearchTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration;
                        import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.elasticsearch.autoconfigure.health.DataElasticsearchReactiveHealthContributorAutoConfiguration;
                        import org.springframework.boot.data.elasticsearch.health.DataElasticsearchReactiveHealthIndicator;
                        import org.springframework.boot.data.elasticsearch.test.autoconfigure.AutoConfigureDataElasticsearch;
                        import org.springframework.boot.data.elasticsearch.test.autoconfigure.DataElasticsearchTest;

                        class Demo {
                            DataElasticsearchReactiveHealthContributorAutoConfiguration reactiveHealthContributorAutoConfiguration = null;
                            DataElasticsearchReactiveHealthIndicator reactiveHealthIndicator = null;
                            DataElasticsearchAutoConfiguration dataAutoConfiguration = null;
                            DataElasticsearchReactiveRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            DataElasticsearchRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            AutoConfigureDataElasticsearch autoConfigureDataElasticsearch = null;
                            DataElasticsearchTest dataElasticsearchTest = null;
                        }
                        """
                )
        );
    }

}
