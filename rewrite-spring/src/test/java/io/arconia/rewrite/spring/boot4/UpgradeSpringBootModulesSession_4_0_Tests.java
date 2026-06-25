package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesSession_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSessionModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.session.SessionsEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.session.SessionsEndpoint;
                        import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
                        import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
                        import org.springframework.boot.autoconfigure.session.SessionProperties;

                        class Demo {
                            SessionsEndpoint sessionsEndpoint = null;
                            DefaultCookieSerializerCustomizer defaultCookieSerializerCustomizer = null;
                            SessionAutoConfiguration sessionAutoConfiguration = null;
                            SessionProperties sessionProperties = null;
                            SessionsEndpointAutoConfiguration sessionsEndpointAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.session.actuate.endpoint.SessionsEndpoint;
                        import org.springframework.boot.session.autoconfigure.DefaultCookieSerializerCustomizer;
                        import org.springframework.boot.session.autoconfigure.SessionAutoConfiguration;
                        import org.springframework.boot.session.autoconfigure.SessionProperties;
                        import org.springframework.boot.session.autoconfigure.SessionsEndpointAutoConfiguration;

                        class Demo {
                            SessionsEndpoint sessionsEndpoint = null;
                            DefaultCookieSerializerCustomizer defaultCookieSerializerCustomizer = null;
                            SessionAutoConfiguration sessionAutoConfiguration = null;
                            SessionProperties sessionProperties = null;
                            SessionsEndpointAutoConfiguration sessionsEndpointAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
