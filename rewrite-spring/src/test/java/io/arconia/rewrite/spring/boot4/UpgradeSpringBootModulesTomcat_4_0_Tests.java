package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesTomcat_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateTomcatModule")
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
                        import org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.metrics.web.tomcat.TomcatMetricsBinder;
                        import org.springframework.boot.autoconfigure.web.embedded.TomcatVirtualThreadsWebServerFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.web.embedded.TomcatWebServerFactoryCustomizer;
                        import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
                        import org.springframework.boot.web.embedded.tomcat.ConnectorStartFailedException;
                        import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
                        import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
                        import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
                        import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
                        import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
                        import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
                        import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

                        class Demo {
                            TomcatMetricsAutoConfiguration tomcatMetricsAutoConfiguration = null;
                            TomcatMetricsBinder tomcatMetricsBinder = null;
                            TomcatVirtualThreadsWebServerFactoryCustomizer tomcatVirtualThreadsWebServerFactoryCustomizer = null;
                            TomcatWebServerFactoryCustomizer tomcatWebServerFactoryCustomizer = null;
                            ConfigurableTomcatWebServerFactory configurableTomcatWebServerFactory = null;
                            ConnectorStartFailedException connectorStartFailedException = null;
                            TomcatConnectorCustomizer tomcatConnectorCustomizer = null;
                            TomcatContextCustomizer tomcatContextCustomizer = null;
                            TomcatEmbeddedWebappClassLoader tomcatEmbeddedWebappClassLoader = null;
                            TomcatProtocolHandlerCustomizer tomcatProtocolHandlerCustomizer = null;
                            TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory = null;
                            TomcatServletWebServerFactory tomcatServletWebServerFactory = null;
                            TomcatWebServer tomcatWebServer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.tomcat.*;
                        import org.springframework.boot.tomcat.autoconfigure.TomcatVirtualThreadsWebServerFactoryCustomizer;
                        import org.springframework.boot.tomcat.autoconfigure.TomcatWebServerFactoryCustomizer;
                        import org.springframework.boot.tomcat.autoconfigure.metrics.TomcatMetricsAutoConfiguration;
                        import org.springframework.boot.tomcat.metrics.TomcatMetricsBinder;
                        import org.springframework.boot.tomcat.reactive.TomcatReactiveWebServerFactory;
                        import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;

                        class Demo {
                            TomcatMetricsAutoConfiguration tomcatMetricsAutoConfiguration = null;
                            TomcatMetricsBinder tomcatMetricsBinder = null;
                            TomcatVirtualThreadsWebServerFactoryCustomizer tomcatVirtualThreadsWebServerFactoryCustomizer = null;
                            TomcatWebServerFactoryCustomizer tomcatWebServerFactoryCustomizer = null;
                            ConfigurableTomcatWebServerFactory configurableTomcatWebServerFactory = null;
                            ConnectorStartFailedException connectorStartFailedException = null;
                            TomcatConnectorCustomizer tomcatConnectorCustomizer = null;
                            TomcatContextCustomizer tomcatContextCustomizer = null;
                            TomcatEmbeddedWebappClassLoader tomcatEmbeddedWebappClassLoader = null;
                            TomcatProtocolHandlerCustomizer tomcatProtocolHandlerCustomizer = null;
                            TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory = null;
                            TomcatServletWebServerFactory tomcatServletWebServerFactory = null;
                            TomcatWebServer tomcatWebServer = null;
                        }
                        """
                )
        );
    }

}
