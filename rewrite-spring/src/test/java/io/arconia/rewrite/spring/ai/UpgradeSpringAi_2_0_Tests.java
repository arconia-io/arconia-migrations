package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

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
    void useChatOptionsBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
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
    void opensearchContainerTypeChange() {
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
    void toolCallbacksMethodRename() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
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
    void useAnthropicChatModelBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-ai-anthropic-2.0.0-M2", "spring-ai-model-2.0.0-M2",
                                "spring-core-7", "micrometer-observation-1.16")),
                //language=java
                java(
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry) {
                                return new AnthropicChatModel(anthropicApi, options, toolCallingManager,
                                        retryTemplate, observationRegistry);
                            }
                        }
                        """,
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry) {
                                return AnthropicChatModel.builder()
                                        .anthropicClient(anthropicApi)
                                        .options(options)
                                        .toolCallingManager(toolCallingManager)
                                        .observationRegistry(observationRegistry)
                                        .build();
                            }
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
                spec -> spec.typeValidationOptions(TypeValidation.none()),
                //language=java
                java(
                        """
                        import org.springaicommunity.mcp.annotation.McpTool;
                        import org.springaicommunity.mcp.context.McpContext;
                        import org.springaicommunity.mcp.method.McpMethod;
                        import org.springaicommunity.mcp.provider.McpProvider;

                        class Demo {
                            McpTool tool = null;
                            McpContext context = null;
                            McpMethod method = null;
                            McpProvider provider = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.annotation.McpTool;
                        import org.springframework.ai.mcp.annotation.context.McpContext;
                        import org.springframework.ai.mcp.annotation.method.McpMethod;
                        import org.springframework.ai.mcp.annotation.provider.McpProvider;

                        class Demo {
                            McpTool tool = null;
                            McpContext context = null;
                            McpMethod method = null;
                            McpProvider provider = null;
                        }
                        """
                )
        );
    }

    @Test
    void mcpClientTransportTypeChanges() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().identifiers(false).build())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "mcp-spring-webflux-0.18.2")),
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
                spec -> spec.typeValidationOptions(TypeValidation.builder().identifiers(false).build())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "mcp-spring-webflux-0.18.2")),
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
                spec -> spec.typeValidationOptions(TypeValidation.builder().identifiers(false).build())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "mcp-spring-webmvc-0.18.2")),
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
                            implementation "org.springaicommunity:mcp-annotations:0.5.1"
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
                            implementation "io.modelcontextprotocol.sdk:mcp-spring-webflux:0.10.0"
                            implementation "io.modelcontextprotocol.sdk:mcp-spring-webmvc:0.10.0"
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
                            implementation "org.springframework.ai:mcp-spring-webflux:0.10.0"
                            implementation "org.springframework.ai:mcp-spring-webmvc:0.10.0"
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

}
