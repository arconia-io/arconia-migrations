package io.arconia.rewrite.spring.framework.framework7;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_0".
 */
class UpgradeSpringFramework_7_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_0")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-core-6.2.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.lang.Nullable;

                        class Demo {
                            Nullable nullable = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.jspecify.annotations.Nullable;

                        class Demo {
                            Nullable nullable = null;
                        }
                        """
                )
        );
    }

}
