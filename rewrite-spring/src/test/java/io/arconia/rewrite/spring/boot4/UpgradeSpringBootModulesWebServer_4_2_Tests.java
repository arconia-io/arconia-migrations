package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebServer_4_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSpringBootWebServer_4_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-4.1", "spring-boot-jetty-4.1", "spring-boot-reactor-netty-4.1", "spring-boot-web-server-4.1"));
    }

    @DocumentExample
    @Test
    void forwardedRequestHeaders() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.jetty.ConfigurableJettyWebServerFactory;
                        import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;

                        class Demo {
                            void jetty(ConfigurableJettyWebServerFactory factory) {
                                factory.setUseForwardHeaders(true);
                            }

                            void jettyDisabled(ConfigurableJettyWebServerFactory factory) {
                                factory.setUseForwardHeaders(false);
                            }

                            void netty(NettyReactiveWebServerFactory factory) {
                                factory.setUseForwardHeaders(true);
                            }
                        }
                        """,
                        """
                        import org.springframework.boot.jetty.ConfigurableJettyWebServerFactory;
                        import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
                        import org.springframework.boot.web.server.ForwardedRequestHeaders;

                        class Demo {
                            void jetty(ConfigurableJettyWebServerFactory factory) {
                                factory.setForwardedRequestHeaders(ForwardedRequestHeaders.X_FORWARDED_WITHOUT_PREFIX);
                            }

                            void jettyDisabled(ConfigurableJettyWebServerFactory factory) {
                                factory.setForwardedRequestHeaders(null);
                            }

                            void netty(NettyReactiveWebServerFactory factory) {
                                factory.setForwardedRequestHeaders(ForwardedRequestHeaders.X_FORWARDED_WITHOUT_PREFIX);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void forwardedRequestHeadersUnchangedWhenNotLiteral() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;

                        class Demo {
                            void netty(NettyReactiveWebServerFactory factory, boolean behindProxy) {
                                factory.setUseForwardHeaders(behindProxy);
                            }
                        }
                        """
                )
        );
    }

}
