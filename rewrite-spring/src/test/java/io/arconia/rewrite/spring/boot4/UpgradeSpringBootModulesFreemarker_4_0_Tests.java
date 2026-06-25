package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesFreemarker_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateFreemarkerModule")
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
                        import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;

                        class Demo {
                            FreeMarkerAutoConfiguration autoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.freemarker.autoconfigure.FreeMarkerAutoConfiguration;

                        class Demo {
                            FreeMarkerAutoConfiguration autoConfiguration = null;
                        }
                        """
                )
        );
    }

}
