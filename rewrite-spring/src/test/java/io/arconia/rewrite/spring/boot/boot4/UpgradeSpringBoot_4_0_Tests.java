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

}
