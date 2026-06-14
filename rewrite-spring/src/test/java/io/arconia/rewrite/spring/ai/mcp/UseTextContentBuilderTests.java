package io.arconia.rewrite.spring.ai.mcp;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseTextContentBuilder}.
 */
class UseTextContentBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseTextContentBuilder())
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
                            McpSchema.TextContent test() {
                                return new McpSchema.TextContent("hello");
                            }
                        }
                        """,
                        """
                        import io.modelcontextprotocol.spec.McpSchema;

                        class Demo {
                            McpSchema.TextContent test() {
                                return McpSchema.TextContent.builder("hello").build();
                            }
                        }
                        """
                )
        );
    }

}
