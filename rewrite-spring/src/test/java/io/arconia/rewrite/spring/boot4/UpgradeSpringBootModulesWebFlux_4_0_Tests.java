package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebFlux_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebFluxModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.observation.web.reactive.WebFluxObservationAutoConfiguration;
                        import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
                        import org.springframework.boot.actuate.web.exchanges.reactive.HttpExchangesWebFilter;
                        import org.springframework.boot.actuate.web.mappings.reactive.HandlerFunctionDescription;
                        import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartProperties;
                        import org.springframework.boot.autoconfigure.web.reactive.ResourceHandlerRegistrationCustomizer;
                        import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
                        import org.springframework.boot.autoconfigure.web.reactive.WebFluxRegistrations;
                        import org.springframework.boot.autoconfigure.web.reactive.WebHttpHandlerBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration;
                        import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
                        import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
                        import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
                        import org.springframework.boot.web.reactive.error.ErrorAttributes;
                        import org.springframework.boot.web.reactive.filter.OrderedWebFilter;

                        class Demo {
                            WebFluxEndpointManagementContextConfiguration webFluxEndpointManagementContextConfiguration = null;
                            WebFluxObservationAutoConfiguration webFluxObservationAutoConfiguration = null;
                            ControllerEndpointHandlerMapping controllerEndpointHandlerMapping = null;
                            HttpExchangesWebFilter httpExchangesWebFilter = null;
                            HandlerFunctionDescription handlerFunctionDescription = null;
                            HttpHandlerAutoConfiguration httpHandlerAutoConfiguration = null;
                            ReactiveMultipartAutoConfiguration reactiveMultipartAutoConfiguration = null;
                            ReactiveMultipartProperties reactiveMultipartProperties = null;
                            ResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer = null;
                            WebFluxAutoConfiguration webFluxAutoConfiguration = null;
                            WebFluxProperties webFluxProperties = null;
                            WebFluxRegistrations webFluxRegistrations = null;
                            WebHttpHandlerBuilderCustomizer webHttpHandlerBuilderCustomizer = null;
                            WebSessionIdResolverAutoConfiguration webSessionIdResolverAutoConfiguration = null;
                            DefaultErrorWebExceptionHandler defaultErrorWebExceptionHandler = null;
                            AutoConfigureWebFlux autoConfigureWebFlux = null;
                            WebFluxTest webFluxTest = null;
                            ErrorAttributes errorAttributes = null;
                            OrderedWebFilter orderedWebFilter = null;
                        }
                        """,
                        """
                        import org.springframework.boot.webflux.actuate.endpoint.web.ControllerEndpointHandlerMapping;
                        import org.springframework.boot.webflux.actuate.web.exchanges.HttpExchangesWebFilter;
                        import org.springframework.boot.webflux.actuate.web.mappings.HandlerFunctionDescription;
                        import org.springframework.boot.webflux.autoconfigure.*;
                        import org.springframework.boot.webflux.autoconfigure.actuate.web.WebFluxEndpointManagementContextConfiguration;
                        import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler;
                        import org.springframework.boot.webflux.error.ErrorAttributes;
                        import org.springframework.boot.webflux.filter.OrderedWebFilter;
                        import org.springframework.boot.webflux.test.autoconfigure.AutoConfigureWebFlux;
                        import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;

                        class Demo {
                            WebFluxEndpointManagementContextConfiguration webFluxEndpointManagementContextConfiguration = null;
                            WebFluxObservationAutoConfiguration webFluxObservationAutoConfiguration = null;
                            ControllerEndpointHandlerMapping controllerEndpointHandlerMapping = null;
                            HttpExchangesWebFilter httpExchangesWebFilter = null;
                            HandlerFunctionDescription handlerFunctionDescription = null;
                            HttpHandlerAutoConfiguration httpHandlerAutoConfiguration = null;
                            ReactiveMultipartAutoConfiguration reactiveMultipartAutoConfiguration = null;
                            ReactiveMultipartProperties reactiveMultipartProperties = null;
                            ResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer = null;
                            WebFluxAutoConfiguration webFluxAutoConfiguration = null;
                            WebFluxProperties webFluxProperties = null;
                            WebFluxRegistrations webFluxRegistrations = null;
                            WebHttpHandlerBuilderCustomizer webHttpHandlerBuilderCustomizer = null;
                            WebSessionIdResolverAutoConfiguration webSessionIdResolverAutoConfiguration = null;
                            DefaultErrorWebExceptionHandler defaultErrorWebExceptionHandler = null;
                            AutoConfigureWebFlux autoConfigureWebFlux = null;
                            WebFluxTest webFluxTest = null;
                            ErrorAttributes errorAttributes = null;
                            OrderedWebFilter orderedWebFilter = null;
                        }
                        """
                )
        );
    }

}
