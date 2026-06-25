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

class UpgradeSpringBootModulesSecurityOAuth2Client_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSecurityOAuth2ClientModule")
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
                        import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;

                        class Demo {
                            OAuth2ClientAutoConfiguration autoConfiguration = null;
                            ReactiveOAuth2ClientAutoConfiguration reactiveAutoConfiguration = null;
                            OAuth2ClientWebSecurityAutoConfiguration webSecurityAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
                        import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration;
                        import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;

                        class Demo {
                            OAuth2ClientAutoConfiguration autoConfiguration = null;
                            ReactiveOAuth2ClientAutoConfiguration reactiveAutoConfiguration = null;
                            OAuth2ClientWebSecurityAutoConfiguration webSecurityAutoConfiguration = null;
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
                            implementation "org.springframework.boot:spring-boot-starter-oauth2-client"
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
                            implementation "org.springframework.boot:spring-boot-starter-security-oauth2-client"

                            testImplementation "org.springframework.boot:spring-boot-starter-security-oauth2-client-test"
                        }
                        """
                )
        );
    }

}
