package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJetty_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJettyModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
                        import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
                        import org.springframework.boot.web.embedded.jetty.JettyWebServer;
                        import org.springframework.boot.autoconfigure.web.embedded.JettyVirtualThreadsWebServerFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.metrics.web.jetty.JettyMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.metrics.web.jetty.JettyConnectionMetricsBinder;
                        import org.springframework.boot.web.embedded.jetty.JettyReactiveWebServerFactory;
                        import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

                        class Demo {
                            ConfigurableJettyWebServerFactory configurableJettyWebServerFactory = null;
                            JettyServerCustomizer jettyServerCustomizer = null;
                            JettyWebServer jettyWebServer = null;
                            JettyVirtualThreadsWebServerFactoryCustomizer jettyVirtualThreadsWebServerFactoryCustomizer = null;
                            JettyWebServerFactoryCustomizer jettyWebServerFactoryCustomizer = null;
                            JettyMetricsAutoConfiguration jettyMetricsAutoConfiguration = null;
                            JettyConnectionMetricsBinder jettyConnectionMetricsBinder = null;
                            JettyReactiveWebServerFactory jettyReactiveWebServerFactory = null;
                            JettyServletWebServerFactory jettyServletWebServerFactory = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jetty.ConfigurableJettyWebServerFactory;
                        import org.springframework.boot.jetty.JettyServerCustomizer;
                        import org.springframework.boot.jetty.JettyWebServer;
                        import org.springframework.boot.jetty.autoconfigure.JettyVirtualThreadsWebServerFactoryCustomizer;
                        import org.springframework.boot.jetty.autoconfigure.JettyWebServerFactoryCustomizer;
                        import org.springframework.boot.jetty.autoconfigure.metrics.JettyMetricsAutoConfiguration;
                        import org.springframework.boot.jetty.metrics.JettyConnectionMetricsBinder;
                        import org.springframework.boot.jetty.reactive.JettyReactiveWebServerFactory;
                        import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;

                        class Demo {
                            ConfigurableJettyWebServerFactory configurableJettyWebServerFactory = null;
                            JettyServerCustomizer jettyServerCustomizer = null;
                            JettyWebServer jettyWebServer = null;
                            JettyVirtualThreadsWebServerFactoryCustomizer jettyVirtualThreadsWebServerFactoryCustomizer = null;
                            JettyWebServerFactoryCustomizer jettyWebServerFactoryCustomizer = null;
                            JettyMetricsAutoConfiguration jettyMetricsAutoConfiguration = null;
                            JettyConnectionMetricsBinder jettyConnectionMetricsBinder = null;
                            JettyReactiveWebServerFactory jettyReactiveWebServerFactory = null;
                            JettyServletWebServerFactory jettyServletWebServerFactory = null;
                        }
                        """
                )
        );
    }

}
