package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringAi_2_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAi_2_0")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-client-chat-2.0.0-M4", "spring-ai-commons-2.0.0-M8", "spring-ai-model-2.0.0-M4"));
    }

    @Test
    @DocumentExample
    void useChatOptionsBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ChatOptions.builder()
                                                .temperature(0.2)
                                                .build())
                                        .call()
                                        .content();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ChatOptions.builder()
                                                .temperature(0.2))
                                        .call()
                                        .content();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void openSearchContainerTypeChange() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "opensearch-testcontainers-2.1")),
                //language=java
                java(
                        """
                        import org.opensearch.testcontainers.OpensearchContainer;

                        class Demo {
                            OpensearchContainer container = null;
                        }
                        """,
                        """
                        import org.opensearch.testcontainers.OpenSearchContainer;

                        class Demo {
                            OpenSearchContainer container = null;
                        }
                        """
                )
        );
    }

    @Test
    void promptChatMemoryAdvisorTypeChange() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-client-chat-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;

                        class Demo {
                            PromptChatMemoryAdvisor advisor = null;
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

                        class Demo {
                            MessageChatMemoryAdvisor advisor = null;
                        }
                        """
                )
        );
    }

    @Test
    void chatModelGetDefaultOptionsMethodRename() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.model.ChatModel;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            ChatOptions test(ChatModel chatModel) {
                                return chatModel.getDefaultOptions();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.model.ChatModel;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            ChatOptions test(ChatModel chatModel) {
                                return chatModel.getOptions();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void optionsNMethodRenames() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-openai-1.1", "spring-ai-model-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.image.ImageOptions;
                        import org.springframework.ai.image.ImageOptionsBuilder;
                        import org.springframework.ai.openai.OpenAiChatOptions;
                        import org.springframework.ai.openai.OpenAiImageOptions;

                        class Demo {
                            OpenAiChatOptions chat() {
                                return OpenAiChatOptions.builder().N(2).build();
                            }
                            OpenAiImageOptions image() {
                                return OpenAiImageOptions.builder().N(3).build();
                            }
                            ImageOptions generic() {
                                return ImageOptionsBuilder.builder().N(4).build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.image.ImageOptions;
                        import org.springframework.ai.image.ImageOptionsBuilder;
                        import org.springframework.ai.openai.OpenAiChatOptions;
                        import org.springframework.ai.openai.OpenAiImageOptions;

                        class Demo {
                            OpenAiChatOptions chat() {
                                return OpenAiChatOptions.builder().n(2).build();
                            }
                            OpenAiImageOptions image() {
                                return OpenAiImageOptions.builder().n(3).build();
                            }
                            ImageOptions generic() {
                                return ImageOptionsBuilder.builder().n(4).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void toolCallbacksMethodRename() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-ai-client-chat-1.1", "spring-ai-model-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallback;
                        import org.springframework.ai.tool.ToolCallbackProvider;

                        class Demo {
                            void test(ChatClient chatClient, ToolCallback callback, ToolCallbackProvider provider) {
                                chatClient.prompt()
                                        .toolCallbacks(callback)
                                        .toolCallbacks(provider)
                                        .call()
                                        .content();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallback;
                        import org.springframework.ai.tool.ToolCallbackProvider;

                        class Demo {
                            void test(ChatClient chatClient, ToolCallback callback, ToolCallbackProvider provider) {
                                chatClient.prompt()
                                        .tools(callback)
                                        .tools(provider)
                                        .call()
                                        .content();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void commonsMethodChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.util.json.JsonParser;

                        class Demo {
                            void json() {
                                Object mapper = JsonParser.getObjectMapper();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.util.json.JsonParser;

                        class Demo {
                            void json() {
                                Object mapper = JsonParser.getJsonMapper();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void vectorStoreAdvisorDependencyChange() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-advisors-vector-store"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-vector-store-advisor"
                        }
                        """
                )
        );
    }

    @Test
    void anthropicTypeChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-anthropic-2.0.0-M2")),
                //language=java
                java(
                        """
                        import org.springframework.ai.anthropic.api.AnthropicCacheOptions;
                        import org.springframework.ai.anthropic.api.AnthropicCacheStrategy;
                        import org.springframework.ai.anthropic.api.AnthropicCacheTtl;
                        import org.springframework.ai.anthropic.api.CitationDocument;
                        import org.springframework.ai.anthropic.api.utils.CacheEligibilityResolver;

                        class Demo {
                            AnthropicCacheOptions cacheOptions = null;
                            AnthropicCacheStrategy cacheStrategy = null;
                            AnthropicCacheTtl cacheTtl = null;
                            CitationDocument citationDocument = null;
                            CacheEligibilityResolver eligibilityResolver = null;
                        }
                        """,
                        """
                        import org.springframework.ai.anthropic.*;

                        class Demo {
                            AnthropicCacheOptions cacheOptions = null;
                            AnthropicCacheStrategy cacheStrategy = null;
                            AnthropicCacheTtl cacheTtl = null;
                            AnthropicCitationDocument citationDocument = null;
                            CacheEligibilityResolver eligibilityResolver = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpPackageChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "mcp-annotations-0.9")),
                //language=java
                java(
                        """
                        import org.springaicommunity.mcp.annotation.McpTool;
                        import org.springaicommunity.mcp.context.McpRequestContextTypes;
                        import org.springaicommunity.mcp.method.tool.ReturnMode;
                        import org.springaicommunity.mcp.provider.sampling.SyncMcpSamplingProvider;

                        class Demo {
                            McpTool tool = null;
                            McpRequestContextTypes contextTypes = null;
                            ReturnMode returnMode = null;
                            SyncMcpSamplingProvider provider = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.annotation.McpTool;
                        import org.springframework.ai.mcp.annotation.context.McpRequestContextTypes;
                        import org.springframework.ai.mcp.annotation.method.tool.ReturnMode;
                        import org.springframework.ai.mcp.annotation.provider.sampling.SyncMcpSamplingProvider;

                        class Demo {
                            McpTool tool = null;
                            McpRequestContextTypes contextTypes = null;
                            ReturnMode returnMode = null;
                            SyncMcpSamplingProvider provider = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpClientTransportTypeChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "mcp-spring-webflux-0.18")),
                //language=java
                java(
                        """
                        import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
                        import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;

                        class Demo {
                            WebFluxSseClientTransport sseTransport = null;
                            WebClientStreamableHttpTransport streamableTransport = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.client.webflux.transport.WebClientStreamableHttpTransport;
                        import org.springframework.ai.mcp.client.webflux.transport.WebFluxSseClientTransport;

                        class Demo {
                            WebFluxSseClientTransport sseTransport = null;
                            WebClientStreamableHttpTransport streamableTransport = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpServerWebFluxTransportTypeChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "mcp-spring-webflux-0.18")),
                //language=java
                java(
                        """
                        import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
                        import io.modelcontextprotocol.server.transport.WebFluxStatelessServerTransport;
                        import io.modelcontextprotocol.server.transport.WebFluxStreamableServerTransportProvider;

                        class Demo {
                            WebFluxSseServerTransportProvider sseProvider = null;
                            WebFluxStreamableServerTransportProvider streamableProvider = null;
                            WebFluxStatelessServerTransport statelessTransport = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.server.webflux.transport.WebFluxSseServerTransportProvider;
                        import org.springframework.ai.mcp.server.webflux.transport.WebFluxStatelessServerTransport;
                        import org.springframework.ai.mcp.server.webflux.transport.WebFluxStreamableServerTransportProvider;

                        class Demo {
                            WebFluxSseServerTransportProvider sseProvider = null;
                            WebFluxStreamableServerTransportProvider streamableProvider = null;
                            WebFluxStatelessServerTransport statelessTransport = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpServerWebMvcTransportTypeChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "mcp-spring-webmvc-0.18")),
                //language=java
                java(
                        """
                        import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
                        import io.modelcontextprotocol.server.transport.WebMvcStatelessServerTransport;
                        import io.modelcontextprotocol.server.transport.WebMvcStreamableServerTransportProvider;

                        class Demo {
                            WebMvcSseServerTransportProvider sseProvider = null;
                            WebMvcStreamableServerTransportProvider streamableProvider = null;
                            WebMvcStatelessServerTransport statelessTransport = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.server.webmvc.transport.WebMvcSseServerTransportProvider;
                        import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStatelessServerTransport;
                        import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;

                        class Demo {
                            WebMvcSseServerTransportProvider sseProvider = null;
                            WebMvcStreamableServerTransportProvider streamableProvider = null;
                            WebMvcStatelessServerTransport statelessTransport = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpAnnotationsDependencyRemoved() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springaicommunity:mcp-annotations"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                        }
                        """
                )
        );
    }

    @Test
    void mcpTransportDependencyChanges() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "io.modelcontextprotocol.sdk:mcp-spring-webflux"
                            implementation "io.modelcontextprotocol.sdk:mcp-spring-webmvc"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:mcp-spring-webflux"
                            implementation "org.springframework.ai:mcp-spring-webmvc"
                        }
                        """
                )
        );
    }

    @Test
    void mistralAiMethodChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-mistral-ai-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;

                        class Demo {
                            MistralAiEmbeddingOptions options = MistralAiEmbeddingOptions.builder()
                                .withModel("devstral")
                                .withEncodingFormat("float")
                                .build();
                        }
                        """,
                        """
                        import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;

                        class Demo {
                            MistralAiEmbeddingOptions options = MistralAiEmbeddingOptions.builder()
                                .model("devstral")
                                .encodingFormat("float")
                                .build();
                        }
                        """
                )
        );
    }

    @Test
    void mistralAiEnumChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-mistral-ai-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.mistralai.api.MistralAiApi;

                        class Demo {
                            String modelName1 = MistralAiApi.ChatModel.DEVSTRAL_SMALL.getName();
                            String modelName2 = MistralAiApi.ChatModel.DEVSTRAL_MEDIUM.getName();
                            String modelName3 = MistralAiApi.ChatModel.MAGISTRAL_SMALL.getName();
                            String modelName4 = MistralAiApi.ChatModel.MINISTRAL_3B_LATEST.getName();
                            String modelName5 = MistralAiApi.ChatModel.MINISTRAL_8B_LATEST.getName();
                            String modelName6 = MistralAiApi.ChatModel.LARGE.getName();
                            String modelName7 = MistralAiApi.ChatModel.SMALL.getName();
                            String modelName8 = MistralAiApi.ChatModel.PIXTRAL.getName();
                            String modelName9 = MistralAiApi.ChatModel.PIXTRAL_LARGE.getName();
                        }
                        """,
                        """
                        import org.springframework.ai.mistralai.api.MistralAiApi.ChatModel;

                        class Demo {
                            String modelName1 = ChatModel.DEVSTRAL.getName();
                            String modelName2 = ChatModel.DEVSTRAL.getName();
                            String modelName3 = ChatModel.MISTRAL_SMALL.getName();
                            String modelName4 = ChatModel.MINISTRAL_3B.getName();
                            String modelName5 = ChatModel.MINISTRAL_8B.getName();
                            String modelName6 = ChatModel.MISTRAL_LARGE.getName();
                            String modelName7 = ChatModel.MISTRAL_SMALL.getName();
                            String modelName8 = ChatModel.MINISTRAL_14B.getName();
                            String modelName9 = ChatModel.MISTRAL_LARGE.getName();
                        }
                        """
                )
        );
    }

    @Test
    void openAiEnumChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1", "openai-java-core-4")),
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.api.OpenAiApi;

                        class Demo {
                            String modelName1 = OpenAiApi.ChatModel.O4_MINI.getValue();
                            String modelName2 = OpenAiApi.ChatModel.O3.getValue();
                            String modelName3 = OpenAiApi.ChatModel.O3_MINI.getValue();
                            String modelName4 = OpenAiApi.ChatModel.O1.getValue();
                            String modelName5 = OpenAiApi.ChatModel.O1_MINI.getValue();
                            String modelName6 = OpenAiApi.ChatModel.GPT_4_1.getValue();
                            String modelName7 = OpenAiApi.ChatModel.GPT_5.getValue();
                            String modelName8 = OpenAiApi.ChatModel.GPT_5_MINI.getValue();
                            String modelName9 = OpenAiApi.ChatModel.GPT_5_NANO.getValue();
                            String modelName10 = OpenAiApi.ChatModel.GPT_5_CHAT_LATEST.getValue();
                            String modelName11 = OpenAiApi.ChatModel.GPT_4_O.getValue();
                            String modelName12 = OpenAiApi.ChatModel.CHATGPT_4_O_LATEST.getValue();
                            String modelName13 = OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW.getValue();
                            String modelName14 = OpenAiApi.ChatModel.GPT_4_1_MINI.getValue();
                            String modelName15 = OpenAiApi.ChatModel.GPT_4_1_NANO.getValue();
                            String modelName16 = OpenAiApi.ChatModel.GPT_4_O_MINI.getValue();
                            String modelName17 = OpenAiApi.ChatModel.GPT_4_O_MINI_AUDIO_PREVIEW.getValue();
                            String modelName18 = OpenAiApi.ChatModel.GPT_4_TURBO.getValue();
                            String modelName19 = OpenAiApi.ChatModel.GPT_4.getValue();
                            String modelName20 = OpenAiApi.ChatModel.GPT_3_5_TURBO.getValue();
                            String modelName21 = OpenAiApi.ChatModel.GPT_4_O_SEARCH_PREVIEW.getValue();
                            String modelName22 = OpenAiApi.ChatModel.GPT_4_O_MINI_SEARCH_PREVIEW.getValue();
                        }
                        """,
                        """
                        import com.openai.models.ChatModel;

                        class Demo {
                            String modelName1 = ChatModel.O4_MINI.asString();
                            String modelName2 = ChatModel.O3.asString();
                            String modelName3 = ChatModel.O3_MINI.asString();
                            String modelName4 = ChatModel.O1.asString();
                            String modelName5 = ChatModel.O1_MINI.asString();
                            String modelName6 = ChatModel.GPT_4_1.asString();
                            String modelName7 = ChatModel.GPT_5.asString();
                            String modelName8 = ChatModel.GPT_5_MINI.asString();
                            String modelName9 = ChatModel.GPT_5_NANO.asString();
                            String modelName10 = ChatModel.GPT_5_CHAT_LATEST.asString();
                            String modelName11 = ChatModel.GPT_4O.asString();
                            String modelName12 = ChatModel.CHATGPT_4O_LATEST.asString();
                            String modelName13 = ChatModel.GPT_4O_AUDIO_PREVIEW.asString();
                            String modelName14 = ChatModel.GPT_4_1_MINI.asString();
                            String modelName15 = ChatModel.GPT_4_1_NANO.asString();
                            String modelName16 = ChatModel.GPT_4O_MINI.asString();
                            String modelName17 = ChatModel.GPT_4O_MINI_AUDIO_PREVIEW.asString();
                            String modelName18 = ChatModel.GPT_4_TURBO.asString();
                            String modelName19 = ChatModel.GPT_4.asString();
                            String modelName20 = ChatModel.GPT_3_5_TURBO.asString();
                            String modelName21 = ChatModel.GPT_4O_SEARCH_PREVIEW.asString();
                            String modelName22 = ChatModel.GPT_4O_MINI_SEARCH_PREVIEW.asString();
                        }
                        """
                )
        );
    }

    @Test
    void openAiEmbeddingEnumChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1", "openai-java-core-4")),
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.api.OpenAiApi;

                        class Demo {
                            String modelName1 = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_LARGE.getValue();
                            String modelName2 = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_SMALL.getValue();
                            String modelName3 = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_ADA_002.getValue();
                        }
                        """,
                        """
                        import com.openai.models.embeddings.EmbeddingModel;

                        class Demo {
                            String modelName1 = EmbeddingModel.TEXT_EMBEDDING_3_LARGE.asString();
                            String modelName2 = EmbeddingModel.TEXT_EMBEDDING_3_SMALL.asString();
                            String modelName3 = EmbeddingModel.TEXT_EMBEDDING_ADA_002.asString();
                        }
                        """
                )
        );
    }

    @Test
    void openAiImageEnumChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1", "openai-java-core-4")),
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.api.OpenAiImageApi;

                        class Demo {
                            String modelName1 = OpenAiImageApi.ImageModel.GTP_IMAGE_1_MINI.getValue();
                        }
                        """,
                        """
                        import com.openai.models.images.ImageModel;

                        class Demo {
                            String modelName1 = ImageModel.GPT_IMAGE_1_MINI.asString();
                        }
                        """
                )
        );
    }

    @Test
    void openAiAudioTypeChanges() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1")),
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.api.OpenAiAudioApi;

                        class Demo {
                            OpenAiAudioApi.SpeechRequest.Voice voice;
                            OpenAiAudioApi.SpeechRequest.AudioResponseFormat speechFormat;
                            OpenAiAudioApi.TranscriptResponseFormat transcriptFormat;
                        }
                        """,
                        """
                        import org.springframework.ai.openai.OpenAiAudioSpeechOptions.AudioResponseFormat;
                        import org.springframework.ai.openai.OpenAiAudioSpeechOptions.Voice;

                        class Demo {
                            Voice voice;
                            AudioResponseFormat speechFormat;
                            com.openai.models.audio.AudioResponseFormat transcriptFormat;
                        }
                        """
                )
        );
    }

    @Test
    void openAiEmbeddingEncodingFormatEnum() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-1.1", "spring-ai-model-1.1", "spring-ai-openai-1.1")),
                //language=java
                java(
                        """
                        import java.util.List;

                        import org.springframework.ai.embedding.EmbeddingModel;
                        import org.springframework.ai.embedding.EmbeddingRequest;
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            void test(EmbeddingModel embeddingModel, String query) {
                                var embeddings = embeddingModel.call(new EmbeddingRequest(List.of(query), OpenAiEmbeddingOptions.builder()
                                        .encodingFormat("float")
                                        .build()));
                            }
                        }
                        """,
                        """
                        import java.util.List;

                        import org.springframework.ai.embedding.EmbeddingModel;
                        import org.springframework.ai.embedding.EmbeddingRequest;
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            void test(EmbeddingModel embeddingModel, String query) {
                                var embeddings = embeddingModel.call(new EmbeddingRequest(List.of(query), OpenAiEmbeddingOptions.builder()
                                        .encodingFormat(OpenAiEmbeddingOptions.EncodingFormat.FLOAT)
                                        .build()));
                            }
                        }
                        """
                )
        );
    }

    @Test
    void openAiResponseFormatBuilder() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.1", "spring-ai-openai-1.1")),
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

    @Test
    void azureOpenAiDependenciesRemoved() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-starter-model-azure-openai"
                            implementation "org.springframework.ai:spring-ai-autoconfigure-model-azure-openai"
                            implementation "org.springframework.ai:spring-ai-azure-openai"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                        }
                        """
                )
        );
    }

    @Test
    void openAiSdkAutoconfigureDependencyRemoved() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-autoconfigure-model-openai-sdk"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                        }
                        """
                )
        );
    }

    @Test
    void googleGenAiEmbeddingConnectionDetailsTypeChange() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-google-genai-embedding-2.0.0-M8")),
                //language=java
                java(
                        """
                        import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;

                        class Demo {
                            GoogleGenAiEmbeddingConnectionDetails details = null;
                        }
                        """,
                        """
                        import org.springframework.ai.google.genai.embedding.GoogleGenAiEmbeddingConnectionDetails;

                        class Demo {
                            GoogleGenAiEmbeddingConnectionDetails details = null;
                        }
                        """
                )
        );
    }

    @Test
    void otherDependenciesRemoved() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-hanadb-store"
                            implementation "org.springframework.ai:spring-ai-spring-cloud-bindings"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                        }
                        """
                )
        );
    }

    @Test
    void openAiDependencyChanges() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-openai-sdk"
                            implementation "org.springframework.ai:spring-ai-starter-model-openai-sdk"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.ai:spring-ai-openai"
                            implementation "org.springframework.ai:spring-ai-starter-model-openai"
                        }
                        """
                )
        );
    }

    @Test
    void updateProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.anthropic.chat.options.max-tokens=1000
                        """,
                        """
                        spring.ai.anthropic.chat.max-tokens=1000
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void commentRemovedStreamToolCallResponsesProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.chat.client.tool-calling.stream-tool-call-responses=true
                        """,
                        """
                        # Deprecated property removed in Spring AI 2.0.
                        # spring.ai.chat.client.tool-calling.stream-tool-call-responses=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
