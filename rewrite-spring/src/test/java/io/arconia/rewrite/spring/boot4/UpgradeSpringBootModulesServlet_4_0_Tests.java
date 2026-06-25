package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesServlet_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateServletModule")
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
                        import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.web.servlet.ManagementServletContext;
                        import org.springframework.boot.actuate.web.mappings.servlet.FilterRegistrationMappingDescription;
                        import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
                        import org.springframework.boot.web.servlet.MultipartConfigFactory;
                        import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;

                        class Demo {
                            ServletEndpointManagementContextConfiguration servletEndpointManagementContextConfiguration = null;
                            ManagementServletContext managementServletContext = null;
                            FilterRegistrationMappingDescription filterRegistrationMappingDescription = null;
                            HttpEncodingAutoConfiguration httpEncodingAutoConfiguration = null;
                            MultipartConfigFactory multipartConfigFactory = null;
                            OrderedFormContentFilter orderedFormContentFilter = null;
                        }
                        """,
                        """
                        import org.springframework.boot.servlet.MultipartConfigFactory;
                        import org.springframework.boot.servlet.actuate.web.mappings.FilterRegistrationMappingDescription;
                        import org.springframework.boot.servlet.autoconfigure.HttpEncodingAutoConfiguration;
                        import org.springframework.boot.servlet.autoconfigure.actuate.web.ManagementServletContext;
                        import org.springframework.boot.servlet.autoconfigure.actuate.web.ServletEndpointManagementContextConfiguration;
                        import org.springframework.boot.servlet.filter.OrderedFormContentFilter;

                        class Demo {
                            ServletEndpointManagementContextConfiguration servletEndpointManagementContextConfiguration = null;
                            ManagementServletContext managementServletContext = null;
                            FilterRegistrationMappingDescription filterRegistrationMappingDescription = null;
                            HttpEncodingAutoConfiguration httpEncodingAutoConfiguration = null;
                            MultipartConfigFactory multipartConfigFactory = null;
                            OrderedFormContentFilter orderedFormContentFilter = null;
                        }
                        """
                )
        );
    }

}
