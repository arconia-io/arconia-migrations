package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJersey_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJerseyModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.jersey.JerseyServerMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.web.jersey.JerseyChildManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.web.jersey.JerseySameManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.web.jersey.ManagementContextResourceConfigCustomizer;
                        import org.springframework.boot.actuate.endpoint.web.jersey.JerseyEndpointResourceFactory;
                        import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jersey.JerseyProperties;
                        import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
                        import org.springframework.boot.autoconfigure.web.servlet.DefaultJerseyApplicationPath;
                        import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;

                        class Demo {
                            JerseyServletEndpointManagementContextConfiguration servletEndpointManagementContextConfiguration = null;
                            JerseyServerMetricsAutoConfiguration serverMetricsAutoConfiguration = null;
                            JerseyChildManagementContextConfiguration childManagementContextConfiguration = null;
                            JerseySameManagementContextConfiguration sameManagementContextConfiguration = null;
                            ManagementContextResourceConfigCustomizer managementContextResourceConfigCustomizer = null;
                            JerseyEndpointResourceFactory endpointResourceFactory = null;
                            JerseyAutoConfiguration autoConfiguration = null;
                            JerseyProperties properties = null;
                            ResourceConfigCustomizer resourceConfigCustomizer = null;
                            DefaultJerseyApplicationPath defaultApplicationPath = null;
                            JerseyApplicationPath applicationPath = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jersey.actuate.endpoint.web.JerseyEndpointResourceFactory;
                        import org.springframework.boot.jersey.autoconfigure.*;
                        import org.springframework.boot.jersey.autoconfigure.actuate.web.JerseyChildManagementContextConfiguration;
                        import org.springframework.boot.jersey.autoconfigure.actuate.web.JerseyEndpointManagementContextConfiguration;
                        import org.springframework.boot.jersey.autoconfigure.actuate.web.JerseySameManagementContextConfiguration;
                        import org.springframework.boot.jersey.autoconfigure.actuate.web.ManagementContextResourceConfigCustomizer;
                        import org.springframework.boot.jersey.autoconfigure.metrics.JerseyServerMetricsAutoConfiguration;

                        class Demo {
                            JerseyEndpointManagementContextConfiguration servletEndpointManagementContextConfiguration = null;
                            JerseyServerMetricsAutoConfiguration serverMetricsAutoConfiguration = null;
                            JerseyChildManagementContextConfiguration childManagementContextConfiguration = null;
                            JerseySameManagementContextConfiguration sameManagementContextConfiguration = null;
                            ManagementContextResourceConfigCustomizer managementContextResourceConfigCustomizer = null;
                            JerseyEndpointResourceFactory endpointResourceFactory = null;
                            JerseyAutoConfiguration autoConfiguration = null;
                            JerseyProperties properties = null;
                            ResourceConfigCustomizer resourceConfigCustomizer = null;
                            DefaultJerseyApplicationPath defaultApplicationPath = null;
                            JerseyApplicationPath applicationPath = null;
                        }
                        """
                )
        );
    }

}
