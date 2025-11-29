package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseToolResponseMessageBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseToolResponseMessageBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.0.*"));
    }

    @Test
    void useBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.messages.ToolResponseMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                var message1 = new ToolResponseMessage(List.of());
                                var message2 = new ToolResponseMessage(List.of(), Map.of());
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.messages.ToolResponseMessage;

                        import java.util.List;
                        import java.util.Map;

                        class Demo {
                            void test() {
                                var message1 = ToolResponseMessage.builder().responses(List.of()).build();
                                var message2 = ToolResponseMessage.builder().responses(List.of()).metadata(Map.of()).build();
                            }
                        }
                        """
                )
        );
    }

}
