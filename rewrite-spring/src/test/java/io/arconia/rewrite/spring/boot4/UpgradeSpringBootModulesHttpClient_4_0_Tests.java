package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesHttpClient_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateHttpClientModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.observation.web.client.HttpClientObservationsAutoConfiguration;
                        import org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.http.client.HttpClientProperties;
                        import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;

                        class Demo {
                            ClientHttpRequestFactorySettings clientHttpRequestFactorySettings = null;
                            ClientHttpRequestFactoryBuilderCustomizer clientHttpRequestFactoryBuilderCustomizer = null;
                            HttpClientAutoConfiguration httpClientAutoConfiguration = null;
                            HttpClientProperties httpClientProperties = null;
                            HttpClientObservationsAutoConfiguration httpClientObservationsAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.http.client.HttpClientSettings;
                        import org.springframework.boot.http.client.autoconfigure.ClientHttpRequestFactoryBuilderCustomizer;
                        import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
                        import org.springframework.boot.http.client.autoconfigure.HttpClientProperties;
                        import org.springframework.boot.http.client.autoconfigure.metrics.HttpClientMetricsAutoConfiguration;

                        class Demo {
                            HttpClientSettings clientHttpRequestFactorySettings = null;
                            ClientHttpRequestFactoryBuilderCustomizer clientHttpRequestFactoryBuilderCustomizer = null;
                            HttpClientAutoConfiguration httpClientAutoConfiguration = null;
                            HttpClientProperties httpClientProperties = null;
                            HttpClientMetricsAutoConfiguration httpClientObservationsAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
