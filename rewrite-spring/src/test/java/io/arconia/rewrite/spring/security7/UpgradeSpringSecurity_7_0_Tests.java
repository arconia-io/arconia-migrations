package io.arconia.rewrite.spring.security7;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;

/**
 * Unit tests for "io.arconia.rewrite.spring.security7.UpgradeSpringSecurity_7_0".
 */
class UpgradeSpringSecurity_7_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.security7.UpgradeSpringSecurity_7_0")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-security-core-6.5", "spring-security-config-6.5", "spring-security-web-6.5",
                        "spring-security-oauth2-core-6.5", "spring-security-oauth2-client-6.5",
                        "spring-security-oauth2-resource-server-6.5", "spring-security-saml2-service-provider-6.5",
                        "spring-security-cas-6.5", "jakarta.servlet-api", "reactive-streams"));
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
                            implementation "org.springframework.security:spring-security-core:6.5.0"
                            implementation "org.springframework.security.kerberos:spring-security-kerberos-core:2.1.1"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("spring-security dependency upgraded to 7.0.x")
                                    .containsPattern("org\\.springframework\\.security:spring-security-core:7\\.0\\.\\d+")
                                    .as("Kerberos extension moved into Spring Security")
                                    .containsPattern("org\\.springframework\\.security:spring-security-kerberos-core:7\\.0\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    @DocumentExample
    void authorizationManagerCheckRenamedToAuthorize() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.function.Supplier;

                        import org.springframework.security.authorization.AuthorizationDecision;
                        import org.springframework.security.authorization.AuthorizationManager;
                        import org.springframework.security.core.Authentication;

                        class AlwaysGranted implements AuthorizationManager<Object> {

                            @Override
                            public AuthorizationDecision check(Supplier<Authentication> authentication, Object object) {
                                return new AuthorizationDecision(true);
                            }

                            AuthorizationDecision delegate(AuthorizationManager<Object> manager, Supplier<Authentication> authentication) {
                                return manager.check(authentication, new Object());
                            }
                        }
                        """,
                        """
                        import java.util.function.Supplier;

                        import org.springframework.security.authorization.AuthorizationDecision;
                        import org.springframework.security.authorization.AuthorizationManager;
                        import org.springframework.security.core.Authentication;

                        class AlwaysGranted implements AuthorizationManager<Object> {

                            @Override
                            public AuthorizationDecision authorize(Supplier<Authentication> authentication, Object object) {
                                return new AuthorizationDecision(true);
                            }

                            AuthorizationDecision delegate(AuthorizationManager<Object> manager, Supplier<Authentication> authentication) {
                                return manager.authorize(authentication, new Object());
                            }
                        }
                        """
                )
        );
    }

    @Test
    void configAndCasTypes() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
                        import org.springframework.security.config.annotation.ObjectPostProcessor;
                        import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

                        @EnableWebMvcSecurity
                        class SecurityConfig {
                            ObjectPostProcessor<Object> postProcessor = null;
                            ServiceAuthenticationDetails details = null;
                        }
                        """,
                        """
                        import org.springframework.security.cas.authentication.ServiceAuthenticationDetails;
                        import org.springframework.security.config.ObjectPostProcessor;
                        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

                        @EnableWebSecurity
                        class SecurityConfig {
                            ObjectPostProcessor<Object> postProcessor = null;
                            ServiceAuthenticationDetails details = null;
                        }
                        """
                )
        );
    }

    @Test
    void oauth2Types() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
                        import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
                        import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
                        import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
                        import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;

                        class OAuth2Config {
                            DefaultAuthorizationCodeTokenResponseClient codeClient = new DefaultAuthorizationCodeTokenResponseClient();
                            DefaultClientCredentialsTokenResponseClient credentialsClient = new DefaultClientCredentialsTokenResponseClient();
                            BearerTokenAuthenticationToken token = new BearerTokenAuthenticationToken("token");
                            NimbusOpaqueTokenIntrospector introspector = new NimbusOpaqueTokenIntrospector("https://issuer/introspect", "client", "secret");
                            BearerTokenAuthenticationFilter filter = null;
                        }
                        """,
                        """
                        import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
                        import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
                        import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
                        import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
                        import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

                        class OAuth2Config {
                            RestClientAuthorizationCodeTokenResponseClient codeClient = new RestClientAuthorizationCodeTokenResponseClient();
                            RestClientClientCredentialsTokenResponseClient credentialsClient = new RestClientClientCredentialsTokenResponseClient();
                            BearerTokenAuthenticationToken token = new BearerTokenAuthenticationToken("token");
                            SpringOpaqueTokenIntrospector introspector = new SpringOpaqueTokenIntrospector("https://issuer/introspect", "client", "secret");
                            BearerTokenAuthenticationFilter filter = null;
                        }
                        """
                )
        );
    }

    @Test
    void saml2Types() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
                        import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
                        import org.springframework.security.saml2.provider.service.web.OpenSamlAuthenticationTokenConverter;

                        class Saml2Config {
                            OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
                            OpenSamlAuthenticationTokenConverter converter = null;

                            String entityId(RelyingPartyRegistration registration) {
                                return registration.getAssertingPartyDetails().getEntityId();
                            }
                        }
                        """,
                        """
                        import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
                        import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
                        import org.springframework.security.saml2.provider.service.web.OpenSaml5AuthenticationTokenConverter;

                        class Saml2Config {
                            OpenSaml5AuthenticationProvider provider = new OpenSaml5AuthenticationProvider();
                            OpenSaml5AuthenticationTokenConverter converter = null;

                            String entityId(RelyingPartyRegistration registration) {
                                return registration.getAssertingPartyMetadata().getEntityId();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void accessApiDependencyAddedWhenUsed() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        import org.springframework.security.access.AccessDecisionManager;

                                        class LegacyAccess {
                                            AccessDecisionManager manager = null;
                                        }
                                        """
                                )
                        ),
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
                                    implementation "org.springframework.security:spring-security-core:6.5.0"
                                }
                                """,
                                spec -> spec.after(actual -> {
                                    assertThat(actual)
                                            .as("spring-security-access added for the legacy Access API")
                                            .containsPattern("implementation \"org\\.springframework\\.security:spring-security-access:7\\.0\\.\\d+\"");
                                    return actual;
                                })
                        )
                )
        );
    }

}
