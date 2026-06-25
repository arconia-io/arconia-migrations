package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebClient_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebClientModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.metrics.web.reactive.client.ObservationWebClientCustomizer;
                        import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientCodecCustomizer;
                        import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
                        import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
                        import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;

                        class Demo {
                            ObservationWebClientCustomizer observationWebClientCustomizer = null;
                            WebClientAutoConfiguration webClientAutoConfiguration = null;
                            WebClientCodecCustomizer webClientCodecCustomizer = null;
                            WebClientSsl webClientSsl = null;
                            AutoConfigureWebClient autoConfigureWebClient = null;
                            WebClientCustomizer webClientCustomizer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.webclient.WebClientCustomizer;
                        import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
                        import org.springframework.boot.webclient.autoconfigure.WebClientCodecCustomizer;
                        import org.springframework.boot.webclient.autoconfigure.WebClientSsl;
                        import org.springframework.boot.webclient.observation.AutoConfigureWebClient;
                        import org.springframework.boot.webclient.observation.ObservationWebClientCustomizer;

                        class Demo {
                            ObservationWebClientCustomizer observationWebClientCustomizer = null;
                            WebClientAutoConfiguration webClientAutoConfiguration = null;
                            WebClientCodecCustomizer webClientCodecCustomizer = null;
                            WebClientSsl webClientSsl = null;
                            AutoConfigureWebClient autoConfigureWebClient = null;
                            WebClientCustomizer webClientCustomizer = null;
                        }
                        """
                )
        );
    }

}
