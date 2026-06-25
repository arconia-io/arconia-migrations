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

class UpgradeSpringBootModulesSecurityOAuth2ResourceServer_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSecurityOAuth2ResourceServerModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
                        import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;

                        class Demo {
                            ReactiveOAuth2ClientAutoConfiguration reactiveClientAutoConfiguration = null;
                            OAuth2ClientWebSecurityAutoConfiguration clientWebSecurityAutoConfiguration = null;
                            OAuth2ResourceServerProperties resourceServerProperties = null;
                            ReactiveOAuth2ResourceServerAutoConfiguration reactiveResourceServerAutoConfiguration = null;
                            OAuth2ResourceServerAutoConfiguration resourceServerAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration;
                        import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
                        import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
                        import org.springframework.boot.security.oauth2.server.resource.autoconfigure.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
                        import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;

                        class Demo {
                            ReactiveOAuth2ClientAutoConfiguration reactiveClientAutoConfiguration = null;
                            OAuth2ClientWebSecurityAutoConfiguration clientWebSecurityAutoConfiguration = null;
                            OAuth2ResourceServerProperties resourceServerProperties = null;
                            ReactiveOAuth2ResourceServerAutoConfiguration reactiveResourceServerAutoConfiguration = null;
                            OAuth2ResourceServerAutoConfiguration resourceServerAutoConfiguration = null;
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
                            implementation "org.springframework.boot:spring-boot-starter-oauth2-resource-server"
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
                            implementation "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server"

                            testImplementation "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test"
                        }
                        """
                )
        );
    }

}
