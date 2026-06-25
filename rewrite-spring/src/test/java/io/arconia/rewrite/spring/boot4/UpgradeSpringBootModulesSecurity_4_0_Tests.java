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

class UpgradeSpringBootModulesSecurity_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSecurityModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
                        import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.security.servlet.RequestMatcherProvider;
                        import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
                        import org.springframework.boot.autoconfigure.security.SecurityProperties;
                        import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
                        import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
                        import org.springframework.boot.security.reactive.ApplicationContextServerWebExchangeMatcher;
                        import org.springframework.boot.security.servlet.ApplicationContextRequestMatcher;

                        class Demo {
                            EndpointRequest reactiveEndpointRequest = null;
                            ReactiveManagementWebSecurityAutoConfiguration reactiveManagementWebSecurityAutoConfiguration = null;
                            ManagementWebSecurityAutoConfiguration managementWebSecurityAutoConfiguration = null;
                            RequestMatcherProvider requestMatcherProvider = null;
                            ConditionalOnDefaultWebSecurity conditionalOnDefaultWebSecurity = null;
                            SecurityProperties securityProperties = null;
                            StaticResourceLocation staticResourceLocation = null;
                            ReactiveSecurityAutoConfiguration reactiveSecurityAutoConfiguration = null;
                            ReactiveUserDetailsServiceAutoConfiguration reactiveUserDetailsServiceAutoConfiguration = null;
                            RSocketSecurityAutoConfiguration rSocketSecurityAutoConfiguration = null;
                            SecurityAutoConfiguration securityAutoConfiguration = null;
                            SecurityFilterAutoConfiguration securityFilterAutoConfiguration = null;
                            UserDetailsServiceAutoConfiguration userDetailsServiceAutoConfiguration = null;
                            ApplicationContextServerWebExchangeMatcher applicationContextServerWebExchangeMatcher = null;
                            ApplicationContextRequestMatcher applicationContextRequestMatcher = null;
                            org.springframework.boot.autoconfigure.security.reactive.PathRequest reactivePathRequest = null;
                            org.springframework.boot.autoconfigure.security.servlet.PathRequest servletPathRequest = null;
                            org.springframework.boot.autoconfigure.security.reactive.StaticResourceRequest reactiveStaticResourceRequest = null;
                            org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest servletStaticResourceRequest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.SecurityProperties;
                        import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.actuate.web.reactive.EndpointRequest;
                        import org.springframework.boot.security.autoconfigure.actuate.web.reactive.ReactiveManagementWebSecurityAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.actuate.web.servlet.SecurityRequestMatchersManagementContextConfiguration;
                        import org.springframework.boot.security.autoconfigure.rsocket.RSocketSecurityAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.web.StaticResourceLocation;
                        import org.springframework.boot.security.autoconfigure.web.reactive.PathRequest;
                        import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.web.reactive.StaticResourceRequest;
                        import org.springframework.boot.security.autoconfigure.web.servlet.ConditionalOnDefaultWebSecurity;
                        import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
                        import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
                        import org.springframework.boot.security.web.reactive.ApplicationContextServerWebExchangeMatcher;
                        import org.springframework.boot.security.web.servlet.ApplicationContextRequestMatcher;

                        class Demo {
                            EndpointRequest reactiveEndpointRequest = null;
                            ReactiveManagementWebSecurityAutoConfiguration reactiveManagementWebSecurityAutoConfiguration = null;
                            ManagementWebSecurityAutoConfiguration managementWebSecurityAutoConfiguration = null;
                            SecurityRequestMatchersManagementContextConfiguration requestMatcherProvider = null;
                            ConditionalOnDefaultWebSecurity conditionalOnDefaultWebSecurity = null;
                            SecurityProperties securityProperties = null;
                            StaticResourceLocation staticResourceLocation = null;
                            ReactiveWebSecurityAutoConfiguration reactiveSecurityAutoConfiguration = null;
                            ReactiveUserDetailsServiceAutoConfiguration reactiveUserDetailsServiceAutoConfiguration = null;
                            RSocketSecurityAutoConfiguration rSocketSecurityAutoConfiguration = null;
                            ServletWebSecurityAutoConfiguration securityAutoConfiguration = null;
                            SecurityFilterAutoConfiguration securityFilterAutoConfiguration = null;
                            UserDetailsServiceAutoConfiguration userDetailsServiceAutoConfiguration = null;
                            ApplicationContextServerWebExchangeMatcher applicationContextServerWebExchangeMatcher = null;
                            ApplicationContextRequestMatcher applicationContextRequestMatcher = null;
                            PathRequest reactivePathRequest = null;
                            org.springframework.boot.security.autoconfigure.web.servlet.PathRequest servletPathRequest = null;
                            StaticResourceRequest reactiveStaticResourceRequest = null;
                            org.springframework.boot.security.autoconfigure.web.servlet.StaticResourceRequest servletStaticResourceRequest = null;
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
                            testImplementation "org.springframework.security:spring-security-test"
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
                            testImplementation "org.springframework.boot:spring-boot-starter-security-test"
                        }
                        """
                )
        );
    }

}
