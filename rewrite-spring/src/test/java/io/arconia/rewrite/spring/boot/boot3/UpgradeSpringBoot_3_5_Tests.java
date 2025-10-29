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
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-boot-autoconfigure-3.4.*"));
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

    @Test
    void typeChangesMockito() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-3.4.*")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.mockito.Answers;
                        import org.springframework.boot.test.mock.mockito.MockBean;
                        import org.springframework.boot.test.mock.mockito.MockReset;
                        import org.springframework.boot.test.mock.mockito.SpyBean;

                        class Demo {
                            @MockBean(classes = {com.yourorg.Demo.class}, answer = Answers.RETURNS_DEFAULTS, reset = MockReset.BEFORE)
                            private String someDependency;

                            @SpyBean(value = {com.yourorg.Demo.class}, classes = {com.yourorg.Demo.class}, reset = MockReset.BEFORE)
                            private String someOtherDependency;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.mockito.Answers;
                        import org.springframework.test.context.bean.override.mockito.MockReset;
                        import org.springframework.test.context.bean.override.mockito.MockitoBean;
                        import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

                        class Demo {
                            @MockitoBean(answers = Answers.RETURNS_DEFAULTS, reset = MockReset.BEFORE)
                            private String someDependency;

                            @MockitoSpyBean(reset = MockReset.BEFORE)
                            private String someOtherDependency;
                        }
                        """
                )
        );
    }

}
