package io.arconia.rewrite.spring.ai2.mcp;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseReadResourceResultBuilder}.
 */
class UseReadResourceResultBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseReadResourceResultBuilder())
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
                            McpSchema.ReadResourceResult test(List<McpSchema.ResourceContents> contents) {
                                return new McpSchema.ReadResourceResult(contents);
                            }
                        }
                        """,
                        """
                        import io.modelcontextprotocol.spec.McpSchema;

                        import java.util.List;

                        class Demo {
                            McpSchema.ReadResourceResult test(List<McpSchema.ResourceContents> contents) {
                                return McpSchema.ReadResourceResult.builder(contents).build();
                            }
                        }
                        """
                )
        );
    }

}
