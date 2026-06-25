package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebServer_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebServerModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.web.ServerProperties;
                        import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
                        import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
                        import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
                        import org.springframework.boot.web.reactive.context.ReactiveWebServerInitializedEvent;
                        import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactory;
                        import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory;
                        import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
                        import org.springframework.boot.web.servlet.ServletComponentScan;
                        import org.springframework.boot.web.servlet.WebListenerRegistrar;
                        import org.springframework.boot.web.servlet.WebListenerRegistry;
                        import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
                        import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
                        import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
                        import org.springframework.boot.web.servlet.context.WebApplicationContextServletContextAwareProcessor;
                        import org.springframework.boot.web.servlet.context.XmlServletWebServerApplicationContext;
                        import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
                        import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
                        import org.springframework.boot.web.servlet.server.Jsp;
                        import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
                        import org.springframework.boot.web.servlet.server.Session;

                        class Demo {
                            ServerProperties serverProperties = null;
                            ReactiveWebServerFactoryCustomizer reactiveWebServerFactoryCustomizer = null;
                            ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer = null;
                            AbstractReactiveWebServerFactory abstractReactiveWebServerFactory = null;
                            ConfigurableReactiveWebServerFactory configurableReactiveWebServerFactory = null;
                            ReactiveWebServerFactory reactiveWebServerFactory = null;
                            AnnotationConfigReactiveWebServerApplicationContext annotationConfigReactiveWebServerApplicationContext = null;
                            ReactiveWebServerApplicationContext reactiveWebServerApplicationContext = null;
                            ReactiveWebServerInitializedEvent reactiveWebServerInitializedEvent = null;
                            ConfigurableServletWebServerFactory configurableServletWebServerFactory = null;
                            CookieSameSiteSupplier cookieSameSiteSupplier = null;
                            Jsp jsp = null;
                            ServletWebServerFactory servletWebServerFactory = null;
                            Session session = null;
                            WebListenerRegistrar webListenerRegistrar = null;
                            WebListenerRegistry webListenerRegistry = null;
                            AnnotationConfigServletWebServerApplicationContext annotationConfigServletWebServerApplicationContext = null;
                            ServletComponentScan servletComponentScan = null;
                            ServletWebServerApplicationContext servletWebServerApplicationContext = null;
                            ServletWebServerInitializedEvent servletWebServerInitializedEvent = null;
                            WebApplicationContextServletContextAwareProcessor webApplicationContextServletContextAwareProcessor = null;
                            XmlServletWebServerApplicationContext xmlServletWebServerApplicationContext = null;
                        }
                        """,
                        """
                        import org.springframework.boot.web.server.autoconfigure.ServerProperties;
                        import org.springframework.boot.web.server.autoconfigure.reactive.ReactiveWebServerFactoryCustomizer;
                        import org.springframework.boot.web.server.autoconfigure.servlet.ServletWebServerFactoryCustomizer;
                        import org.springframework.boot.web.server.reactive.AbstractReactiveWebServerFactory;
                        import org.springframework.boot.web.server.reactive.ConfigurableReactiveWebServerFactory;
                        import org.springframework.boot.web.server.reactive.ReactiveWebServerFactory;
                        import org.springframework.boot.web.server.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
                        import org.springframework.boot.web.server.reactive.context.ReactiveWebServerApplicationContext;
                        import org.springframework.boot.web.server.reactive.context.ReactiveWebServerInitializedEvent;
                        import org.springframework.boot.web.server.servlet.*;
                        import org.springframework.boot.web.server.servlet.context.*;

                        class Demo {
                            ServerProperties serverProperties = null;
                            ReactiveWebServerFactoryCustomizer reactiveWebServerFactoryCustomizer = null;
                            ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer = null;
                            AbstractReactiveWebServerFactory abstractReactiveWebServerFactory = null;
                            ConfigurableReactiveWebServerFactory configurableReactiveWebServerFactory = null;
                            ReactiveWebServerFactory reactiveWebServerFactory = null;
                            AnnotationConfigReactiveWebServerApplicationContext annotationConfigReactiveWebServerApplicationContext = null;
                            ReactiveWebServerApplicationContext reactiveWebServerApplicationContext = null;
                            ReactiveWebServerInitializedEvent reactiveWebServerInitializedEvent = null;
                            ConfigurableServletWebServerFactory configurableServletWebServerFactory = null;
                            CookieSameSiteSupplier cookieSameSiteSupplier = null;
                            Jsp jsp = null;
                            ServletWebServerFactory servletWebServerFactory = null;
                            Session session = null;
                            WebListenerRegistrar webListenerRegistrar = null;
                            WebListenerRegistry webListenerRegistry = null;
                            AnnotationConfigServletWebServerApplicationContext annotationConfigServletWebServerApplicationContext = null;
                            ServletComponentScan servletComponentScan = null;
                            ServletWebServerApplicationContext servletWebServerApplicationContext = null;
                            ServletWebServerInitializedEvent servletWebServerInitializedEvent = null;
                            WebApplicationContextServletContextAwareProcessor webApplicationContextServletContextAwareProcessor = null;
                            XmlServletWebServerApplicationContext xmlServletWebServerApplicationContext = null;
                        }
                        """
                )
        );
    }

}
