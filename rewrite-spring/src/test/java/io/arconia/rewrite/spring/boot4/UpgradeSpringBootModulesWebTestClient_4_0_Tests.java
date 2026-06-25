package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebTestClient_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebTestClientModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-autoconfigure-3.5", "spring-boot-test-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
                        import org.springframework.boot.test.autoconfigure.web.reactive.SpringBootWebTestClientBuilderCustomizer;
                        import org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration;
                        import org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer;

                        class Demo {
                            AutoConfigureWebTestClient autoConfigureWebTestClient = null;
                            SpringBootWebTestClientBuilderCustomizer springBootWebTestClientBuilderCustomizer = null;
                            WebTestClientAutoConfiguration webTestClientAutoConfiguration = null;
                            WebTestClientBuilderCustomizer webTestClientBuilderCustomizer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
                        import org.springframework.boot.webtestclient.autoconfigure.SpringBootWebTestClientBuilderCustomizer;
                        import org.springframework.boot.webtestclient.autoconfigure.WebTestClientAutoConfiguration;
                        import org.springframework.boot.webtestclient.autoconfigure.WebTestClientBuilderCustomizer;

                        class Demo {
                            AutoConfigureWebTestClient autoConfigureWebTestClient = null;
                            SpringBootWebTestClientBuilderCustomizer springBootWebTestClientBuilderCustomizer = null;
                            WebTestClientAutoConfiguration webTestClientAutoConfiguration = null;
                            WebTestClientBuilderCustomizer webTestClientBuilderCustomizer = null;
                        }
                        """
                )
        );
    }

}
