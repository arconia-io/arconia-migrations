package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

/**
 * Unit tests for "io.arconia.rewrite.spring.ai2.UpgradeSpringAi_2_1".
 */
class UpgradeSpringAi_2_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai2.UpgradeSpringAi_2_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-mcp-annotations-2.0"));
    }

    @Test
    @DocumentExample
    void mcpToolMethodCallbacksWithoutToolCallExceptionClass() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.lang.reflect.Method;

                        import org.springframework.ai.mcp.annotation.method.tool.AsyncMcpToolMethodCallback;
                        import org.springframework.ai.mcp.annotation.method.tool.ReturnMode;
                        import org.springframework.ai.mcp.annotation.method.tool.SyncMcpToolMethodCallback;

                        class Demo {
                            SyncMcpToolMethodCallback sync(Method method, Object provider) {
                                return new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider, Exception.class);
                            }

                            AsyncMcpToolMethodCallback async(Method method, Object provider) {
                                return new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider, RuntimeException.class);
                            }
                        }
                        """,
                        """
                        import java.lang.reflect.Method;

                        import org.springframework.ai.mcp.annotation.method.tool.AsyncMcpToolMethodCallback;
                        import org.springframework.ai.mcp.annotation.method.tool.ReturnMode;
                        import org.springframework.ai.mcp.annotation.method.tool.SyncMcpToolMethodCallback;

                        class Demo {
                            SyncMcpToolMethodCallback sync(Method method, Object provider) {
                                return new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);
                            }

                            AsyncMcpToolMethodCallback async(Method method, Object provider) {
                                return new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removedMcpServerProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.mcp.server.sse-endpoint=/sse
                        """,
                        """
                        # Deprecated property removed in Spring AI 2.1.
                        # spring.ai.mcp.server.sse-endpoint=/sse
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
