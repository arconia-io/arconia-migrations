package io.arconia.rewrite.spring.ai2.mcp;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseGetPromptResultBuilder}.
 */
class UseGetPromptResultBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseGetPromptResultBuilder())
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

                        import java.util.List;

                        class Demo {
                            McpSchema.GetPromptResult test(String description, List<McpSchema.PromptMessage> messages) {
                                return new McpSchema.GetPromptResult(description, messages);
                            }
                        }
                        """,
                        """
                        import io.modelcontextprotocol.spec.McpSchema;

                        import java.util.List;

                        class Demo {
                            McpSchema.GetPromptResult test(String description, List<McpSchema.PromptMessage> messages) {
                                return McpSchema.GetPromptResult.builder(messages).description(description).build();
                            }
                        }
                        """
                )
        );
    }

}
