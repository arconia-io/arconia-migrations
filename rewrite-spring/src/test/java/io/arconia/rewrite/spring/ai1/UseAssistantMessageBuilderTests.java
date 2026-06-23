package io.arconia.rewrite.spring.ai1;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseAssistantMessageBuilder}.
 */
class UseAssistantMessageBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseAssistantMessageBuilder())
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
                        import org.springframework.ai.chat.messages.AssistantMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                AssistantMessage message1 = new AssistantMessage("content");
                                AssistantMessage message2 = new AssistantMessage("content", Map.of());
                                AssistantMessage message3 = new AssistantMessage("content", Map.of(), List.of());
                                AssistantMessage message4 = new AssistantMessage("content", Map.of(), List.of(), List.of());
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.messages.AssistantMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                AssistantMessage message1 = AssistantMessage.builder().content("content").build();
                                AssistantMessage message2 = AssistantMessage.builder().content("content").properties(Map.of()).build();
                                AssistantMessage message3 = AssistantMessage.builder().content("content").properties(Map.of()).toolCalls(List.of()).build();
                                AssistantMessage message4 = AssistantMessage.builder().content("content").properties(Map.of()).toolCalls(List.of()).media(List.of()).build();
                            }
                        }
                        """
                )
        );
    }

}
