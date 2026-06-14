package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseToolResponseMessageBuilder}.
 */
class UseToolResponseMessageBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseToolResponseMessageBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-1.0", "spring-ai-model-1.0"));
    }

    @Test
    @DocumentExample
    void useBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.messages.ToolResponseMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                ToolResponseMessage message1 = new ToolResponseMessage(List.of());
                                ToolResponseMessage message2 = new ToolResponseMessage(List.of(), Map.of());
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.messages.ToolResponseMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                ToolResponseMessage message1 = ToolResponseMessage.builder().responses(List.of()).build();
                                ToolResponseMessage message2 = ToolResponseMessage.builder().responses(List.of()).metadata(Map.of()).build();
                            }
                        }
                        """
                )
        );
    }

}
