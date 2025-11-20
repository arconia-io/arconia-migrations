package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.SourceSpecs.text;

class UpgradeSpringBoot_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0");
    }

    @Test
    void typeChangesCore() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5.*", "spring-boot-test-autoconfigure-3.5.*")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.boot.BootstrapRegistry;
                        import org.springframework.boot.env.EnvironmentPostProcessor;

                        class Demo {
                            BootstrapRegistry bootstrapRegistry = null;
                            EnvironmentPostProcessor environmentPostProcessor = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.boot.EnvironmentPostProcessor;
                        import org.springframework.boot.bootstrap.BootstrapRegistry;

                        class Demo {
                            BootstrapRegistry bootstrapRegistry = null;
                            EnvironmentPostProcessor environmentPostProcessor = null;
                        }
                        """
                ),
                //language=text
                text("""
                     org.springframework.boot.env.EnvironmentPostProcessor=\
                     io.arconia.openinference.observation.autoconfigure.ai.OpenInferenceEnvironmentPostProcessor
                     """,
                        """
                        org.springframework.boot.EnvironmentPostProcessor=\
                        io.arconia.openinference.observation.autoconfigure.ai.OpenInferenceEnvironmentPostProcessor
                        """,
                        s -> s.path("src/main/resources/META-INF/spring.factories"))
        );
    }

    @Test
    void typeChangesData() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.boot.autoconfigure.domain.EntityScan;

                        class Demo {
                            EntityScan entityScan = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.boot.persistence.autoconfigure.EntityScan;

                        class Demo {
                            EntityScan entityScan = null;
                        }
                        """
                )
        );
    }

    @Test
    void typeChangesSecurity() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*")),
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

                        class Demo {
                            PathRequest pathRequest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;

                        class Demo {
                            PathRequest pathRequest = null;
                        }
                        """
                )
        );
    }

    @Test
    void typeChangesTest() {
        rewriteRun(
                r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-3.5.*", "spring-boot-test-autoconfigure-3.5.*")),
                //language=java
                java(
                        """
                        import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
                        import org.springframework.boot.test.web.client.TestRestTemplate;

                        class Demo {
                            PropertyMapping propertyMapping = null;
                            TestRestTemplate testRestTemplate = null;
                        }
                        """,
                        """
                        import org.springframework.boot.resttestclient.TestRestTemplate;
                        import org.springframework.boot.test.context.PropertyMapping;

                        class Demo {
                            PropertyMapping propertyMapping = null;
                            TestRestTemplate testRestTemplate = null;
                        }
                        """
                )
        );
    }

}
