package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseAssistantMessageBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseAssistantMessageBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-1.0.*", "spring-ai-model-1.0.*"));
    }

    @Test
    void useBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.messages.AssistantMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                var message1 = new AssistantMessage("content");
                                var message2 = new AssistantMessage("content", Map.of());
                                var message3 = new AssistantMessage("content", Map.of(), List.of());
                                var message4 = new AssistantMessage("content", Map.of(), List.of(), List.of());
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.messages.AssistantMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                var message1 = AssistantMessage.builder().content("content").build();
                                var message2 = AssistantMessage.builder().content("content").properties(Map.of()).build();
                                var message3 = AssistantMessage.builder().content("content").properties(Map.of()).toolCalls(List.of()).build();
                                var message4 = AssistantMessage.builder().content("content").properties(Map.of()).toolCalls(List.of()).media(List.of()).build();
                            }
                        }
                        """
                )
        );
    }

}
