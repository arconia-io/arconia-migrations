package io.arconia.rewrite.spring.boot.boot3;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.spring.boot.UpgradeSpringBoot_3_5".
 */
class UpgradeSpringBoot_3_5_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot.UpgradeSpringBoot_3_5")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-boot-autoconfigure-3.4.7"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
                        import org.springframework.boot.autoconfigure.security.servlet.RequestMatcherProvider;

                        class Demo {
                            OAuth2ClientAutoConfiguration config = null;
                            RequestMatcherProvider provider = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.boot.actuate.autoconfigure.security.servlet.RequestMatcherProvider;
                        import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;

                        class Demo {
                            OAuth2ClientAutoConfiguration config = null;
                            RequestMatcherProvider provider = null;
                        }
                        """
                )
        );
    }

}
