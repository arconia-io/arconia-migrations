package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebMvc_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebMvcModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5", "spring-boot-actuator-3.5",
                        "spring-boot-actuator-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.observation.web.servlet.WebMvcObservationAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.web.servlet.ManagementErrorEndpoint;
                        import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
                        import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDescription;
                        import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDetails;
                        import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletsMappingDescriptionProvider;
                        import org.springframework.boot.actuate.web.mappings.servlet.HandlerFunctionDescription;
                        import org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription;
                        import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
                        import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
                        import org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider;
                        import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
                        import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
                        import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
                        import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
                        import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
                        import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
                        import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
                        import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebDriverAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.web.servlet.SpringBootMockMvcBuilderCustomizer;
                        import org.springframework.boot.test.autoconfigure.web.servlet.WebDriverScope;
                        import org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener;
                        import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
                        import org.springframework.boot.web.servlet.error.ErrorAttributes;

                        class Demo {
                            WebMvcEndpointManagementContextConfiguration webMvcEndpointManagementContextConfiguration = null;
                            WebMvcObservationAutoConfiguration webMvcObservationAutoConfiguration = null;
                            ManagementErrorEndpoint managementErrorEndpoint = null;
                            WebMvcEndpointHandlerMapping webMvcEndpointHandlerMapping = null;
                            DispatcherServletMappingDescription dispatcherServletMappingDescription = null;
                            DispatcherServletMappingDetails dispatcherServletMappingDetails = null;
                            DispatcherServletsMappingDescriptionProvider dispatcherServletsMappingDescriptionProvider = null;
                            HandlerFunctionDescription handlerFunctionDescription = null;
                            RequestMappingConditionsDescription requestMappingConditionsDescription = null;
                            DispatcherServletAutoConfiguration dispatcherServletAutoConfiguration = null;
                            DispatcherServletPath dispatcherServletPath = null;
                            DispatcherServletRegistrationBean dispatcherServletRegistrationBean = null;
                            JspTemplateAvailabilityProvider jspTemplateAvailabilityProvider = null;
                            WebMvcAutoConfiguration webMvcAutoConfiguration = null;
                            WebMvcProperties webMvcProperties = null;
                            WebMvcRegistrations webMvcRegistrations = null;
                            BasicErrorController basicErrorController = null;
                            AutoConfigureMockMvc autoConfigureMockMvc = null;
                            AutoConfigureWebMvc autoConfigureWebMvc = null;
                            MockMvcAutoConfiguration mockMvcAutoConfiguration = null;
                            MockMvcBuilderCustomizer mockMvcBuilderCustomizer = null;
                            MockMvcPrint mockMvcPrint = null;
                            MockMvcWebClientAutoConfiguration mockMvcWebClientAutoConfiguration = null;
                            MockMvcWebDriverAutoConfiguration mockMvcWebDriverAutoConfiguration = null;
                            SpringBootMockMvcBuilderCustomizer springBootMockMvcBuilderCustomizer = null;
                            WebDriverScope webDriverScope = null;
                            WebDriverTestExecutionListener webDriverTestExecutionListener = null;
                            WebMvcTest webMvcTest = null;
                            ErrorAttributes errorAttributes = null;
                        }
                        """,
                        """
                        import org.springframework.boot.webmvc.actuate.endpoint.web.WebMvcEndpointHandlerMapping;
                        import org.springframework.boot.webmvc.actuate.web.mappings.*;
                        import org.springframework.boot.webmvc.autoconfigure.*;
                        import org.springframework.boot.webmvc.autoconfigure.actuate.web.ManagementErrorEndpoint;
                        import org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration;
                        import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
                        import org.springframework.boot.webmvc.error.ErrorAttributes;
                        import org.springframework.boot.webmvc.test.autoconfigure.*;

                        class Demo {
                            WebMvcEndpointManagementContextConfiguration webMvcEndpointManagementContextConfiguration = null;
                            WebMvcObservationAutoConfiguration webMvcObservationAutoConfiguration = null;
                            ManagementErrorEndpoint managementErrorEndpoint = null;
                            WebMvcEndpointHandlerMapping webMvcEndpointHandlerMapping = null;
                            DispatcherServletMappingDescription dispatcherServletMappingDescription = null;
                            DispatcherServletMappingDetails dispatcherServletMappingDetails = null;
                            DispatcherServletsMappingDescriptionProvider dispatcherServletsMappingDescriptionProvider = null;
                            HandlerFunctionDescription handlerFunctionDescription = null;
                            RequestMappingConditionsDescription requestMappingConditionsDescription = null;
                            DispatcherServletAutoConfiguration dispatcherServletAutoConfiguration = null;
                            DispatcherServletPath dispatcherServletPath = null;
                            DispatcherServletRegistrationBean dispatcherServletRegistrationBean = null;
                            JspTemplateAvailabilityProvider jspTemplateAvailabilityProvider = null;
                            WebMvcAutoConfiguration webMvcAutoConfiguration = null;
                            WebMvcProperties webMvcProperties = null;
                            WebMvcRegistrations webMvcRegistrations = null;
                            BasicErrorController basicErrorController = null;
                            AutoConfigureMockMvc autoConfigureMockMvc = null;
                            AutoConfigureWebMvc autoConfigureWebMvc = null;
                            MockMvcAutoConfiguration mockMvcAutoConfiguration = null;
                            MockMvcBuilderCustomizer mockMvcBuilderCustomizer = null;
                            MockMvcPrint mockMvcPrint = null;
                            MockMvcWebClientAutoConfiguration mockMvcWebClientAutoConfiguration = null;
                            MockMvcWebDriverAutoConfiguration mockMvcWebDriverAutoConfiguration = null;
                            SpringBootMockMvcBuilderCustomizer springBootMockMvcBuilderCustomizer = null;
                            WebDriverScope webDriverScope = null;
                            WebDriverTestExecutionListener webDriverTestExecutionListener = null;
                            WebMvcTest webMvcTest = null;
                            ErrorAttributes errorAttributes = null;
                        }
                        """
                )
        );
    }

    @Test
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.boot:spring-boot-starter-web"
                        }
                        """,
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.boot:spring-boot-starter-webmvc"

                            testImplementation "org.springframework.boot:spring-boot-starter-webmvc-test"
                        }
                        """
                )
        );
    }

}
