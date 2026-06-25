package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesReactor_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateReactorModule")
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
                        import org.springframework.boot.autoconfigure.reactor.ReactorAutoConfiguration;
                        import org.springframework.boot.autoconfigure.reactor.ReactorProperties;

                        class Demo {
                            ReactorAutoConfiguration autoConfiguration = null;
                            ReactorProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.reactor.autoconfigure.ReactorAutoConfiguration;
                        import org.springframework.boot.reactor.autoconfigure.ReactorProperties;

                        class Demo {
                            ReactorAutoConfiguration autoConfiguration = null;
                            ReactorProperties properties = null;
                        }
                        """
                )
        );
    }

}
