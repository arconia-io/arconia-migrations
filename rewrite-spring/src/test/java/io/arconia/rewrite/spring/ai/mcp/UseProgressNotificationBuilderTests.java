package io.arconia.rewrite.spring.ai.mcp;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseProgressNotificationBuilder}.
 */
class UseProgressNotificationBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseProgressNotificationBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "mcp-core-2.0"));
    }

    @Test
    @DocumentExample
    void useBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.modelcontextprotocol.spec.McpSchema;

                        class Demo {
                            McpSchema.ProgressNotification test(Object token, double progress, Double total, String message) {
                                return new McpSchema.ProgressNotification(token, progress, total, message);
                            }
                        }
                        """,
                        """
                        import io.modelcontextprotocol.spec.McpSchema;

                        class Demo {
                            McpSchema.ProgressNotification test(Object token, double progress, Double total, String message) {
                                return McpSchema.ProgressNotification.builder(token, progress).total(total).message(message).build();
                            }
                        }
                        """
                )
        );
    }

}
