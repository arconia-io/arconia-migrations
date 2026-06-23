package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseOpenAiResponseFormatBuilder}.
 *
 * <p>The recipe matches the post-{@link ChangeType} state where
 * {@code org.springframework.ai.openai.api.ResponseFormat} has already been relocated to
 * {@code org.springframework.ai.openai.OpenAiChatModel.ResponseFormat}. The tests therefore
 * compose the same {@code ChangeType} entries that precede the builder migration in the
 * Spring AI 2.0 chain so the matcher sees the new FQN at visit time.
 */
class UseOpenAiResponseFormatBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipes(
                        new ChangeType(
                                "org.springframework.ai.openai.api.ResponseFormat",
                                "org.springframework.ai.openai.OpenAiChatModel.ResponseFormat",
                                false),
                        new ChangeType(
                                "org.springframework.ai.openai.api.ResponseFormat.Type",
                                "org.springframework.ai.openai.OpenAiChatModel.ResponseFormat.Type",
                                false),
                        new UseOpenAiResponseFormatBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1"));
    }

    @Test
    @DocumentExample
    void useBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.api.ResponseFormat;

                        class Demo {
                            Object format() {
                                return new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, "{}");
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.openai.OpenAiChatModel.ResponseFormat;

                        class Demo {
                            Object format() {
                                return ResponseFormat.builder().type(ResponseFormat.Type.JSON_SCHEMA).jsonSchema("{}").build();
                            }
                        }
                        """
                )
        );
    }

}
