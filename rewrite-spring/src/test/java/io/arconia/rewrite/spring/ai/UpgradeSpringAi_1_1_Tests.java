package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;

/**
 * Tests for "io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_1".
 */
public class UpgradeSpringAi_1_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_1")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                    "spring-ai-advisors-vector-store-1.0.*", "spring-ai-bedrock-1.0.*", "spring-ai-openai-1.0.*", "spring-ai-vector-store-1.0.*", "spring-ai-model-1.0.*", "spring-ai-client-chat-1.0.*",
                    "spring-ai-autoconfigure-mcp-client-1.0.*", "spring-ai-autoconfigure-mcp-server-1.0.*"));
    }

    @Test
    void springAiBedrockModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel.InputType;
                import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingOptions;

                class Demo {
                    void method() {
                        BedrockTitanEmbeddingOptions options = BedrockTitanEmbeddingOptions.builder()
                            .withInputType(InputType.TEXT)
                            .build();
                    }
                }
                """,
                """
                import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel.InputType;
                import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingOptions;

                class Demo {
                    void method() {
                        BedrockTitanEmbeddingOptions options = BedrockTitanEmbeddingOptions.builder()
                            .inputType(InputType.TEXT)
                            .build();
                    }
                }
                """
            )
        );
    }

    @Test
    void mcpDependencies() {
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
                    implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client"
                    implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server"
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
                    implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client-common"
                    implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server-common"
                }
                """
            )
        );
    }

    @Test
    void mcpClientHttpDependency() {
        rewriteRun(
            spec -> spec.beforeRecipe(withToolingApi()),
            mavenProject("project",
                srcMainJava(
                    //language=java
                    java("""
                        import org.springframework.ai.mcp.client.autoconfigure.SseHttpClientTransportAutoConfiguration;

                        class Demo {
                          SseHttpClientTransportAutoConfiguration config = null;
                        }
                        """,
                            """
                            import org.springframework.ai.mcp.client.httpclient.autoconfigure.SseHttpClientTransportAutoConfiguration;

                            class Demo {
                              SseHttpClientTransportAutoConfiguration config = null;
                            }
                            """)
                ),
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
                            implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client"
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
                              implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client-common"
                              implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client-httpclient"
                          }
                          """
                )
            )
        );
    }

    @Test
    void mcpClientWebFluxDependency() {
        rewriteRun(
            spec -> spec.beforeRecipe(withToolingApi()),
            mavenProject("project",
                srcMainJava(
                    //language=java
                    java("""
                        import org.springframework.ai.mcp.client.autoconfigure.SseWebFluxTransportAutoConfiguration;

                        class Demo {
                          SseWebFluxTransportAutoConfiguration config = null;
                        }
                        """,
                            """
                            import org.springframework.ai.mcp.client.webflux.autoconfigure.SseWebFluxTransportAutoConfiguration;

                            class Demo {
                              SseWebFluxTransportAutoConfiguration config = null;
                            }
                            """)
                ),
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client"
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client-common"
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-client-webflux"
                    }
                    """
                )
            )
        );
    }

    @Test
    void mcpServerWebFluxDependency() {
        rewriteRun(
            spec -> spec.beforeRecipe(withToolingApi()),
            mavenProject("project",
                srcMainJava(
                    //language=java
                    java("""
                        import org.springframework.ai.mcp.server.autoconfigure.McpWebFluxServerAutoConfiguration;

                        class Demo {
                          McpWebFluxServerAutoConfiguration config = null;
                        }
                    """,
                    """
                        import org.springframework.ai.mcp.server.autoconfigure.McpServerSseWebFluxAutoConfiguration;

                        class Demo {
                          McpServerSseWebFluxAutoConfiguration config = null;
                        }
                    """)
                ),
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server"
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server-common"
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server-webflux"
                    }
                    """
                )
            )
        );
    }

    @Test
    void mcpServerWebMvcDependency() {
        rewriteRun(
            spec -> spec.beforeRecipe(withToolingApi()),
            mavenProject("project",
                srcMainJava(
                    //language=java
                    java("""
                        import org.springframework.ai.mcp.server.autoconfigure.McpWebMvcServerAutoConfiguration;

                        class Demo {
                          McpWebMvcServerAutoConfiguration config = null;
                        }
                        """,
                        """
                        import org.springframework.ai.mcp.server.autoconfigure.McpServerSseWebMvcAutoConfiguration;

                        class Demo {
                          McpServerSseWebMvcAutoConfiguration config = null;
                        }
                        """)
                ),
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server"
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
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server-common"
                        implementation "org.springframework.ai:spring-ai-autoconfigure-mcp-server-webmvc"
                    }
                    """
                )
            )
        );
    }

    @Test
    void mcpClientAutoconfigureTypeChanges() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.ai.mcp.client.autoconfigure.McpClientAutoConfiguration;
                import org.springframework.ai.mcp.client.autoconfigure.McpToolCallbackAutoConfiguration;
                import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
                import org.springframework.ai.mcp.client.autoconfigure.StdioTransportAutoConfiguration;
                import org.springframework.ai.mcp.client.autoconfigure.configurer.McpAsyncClientConfigurer;
                import org.springframework.ai.mcp.client.autoconfigure.configurer.McpSyncClientConfigurer;
                import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
                import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
                import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;
                import org.springframework.ai.mcp.client.autoconfigure.SseHttpClientTransportAutoConfiguration;
                import org.springframework.ai.mcp.client.autoconfigure.SseWebFluxTransportAutoConfiguration;

                class Demo {
                    McpClientAutoConfiguration mcpClientAutoConfiguration = null;
                    McpToolCallbackAutoConfiguration mcpToolCallbackAutoConfiguration = null;
                    NamedClientMcpTransport namedClientMcpTransport = null;
                    StdioTransportAutoConfiguration stdioTransportAutoConfiguration = null;
                    McpAsyncClientConfigurer mcpAsyncClientConfigurer = null;
                    McpSyncClientConfigurer mcpSyncClientConfigurer = null;
                    McpClientCommonProperties mcpClientCommonProperties = null;
                    McpSseClientProperties mcpSseClientProperties = null;
                    McpStdioClientProperties mcpStdioClientProperties = null;
                    SseHttpClientTransportAutoConfiguration sseHttpClientTransportAutoConfiguration = null;
                    SseWebFluxTransportAutoConfiguration sseWebFluxTransportAutoConfiguration = null;
                }
                """,
                """
                import org.springframework.ai.mcp.client.common.autoconfigure.McpClientAutoConfiguration;
                import org.springframework.ai.mcp.client.common.autoconfigure.McpToolCallbackAutoConfiguration;
                import org.springframework.ai.mcp.client.common.autoconfigure.NamedClientMcpTransport;
                import org.springframework.ai.mcp.client.common.autoconfigure.StdioTransportAutoConfiguration;
                import org.springframework.ai.mcp.client.common.autoconfigure.configurer.McpAsyncClientConfigurer;
                import org.springframework.ai.mcp.client.common.autoconfigure.configurer.McpSyncClientConfigurer;
                import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpClientCommonProperties;
                import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpSseClientProperties;
                import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStdioClientProperties;
                import org.springframework.ai.mcp.client.httpclient.autoconfigure.SseHttpClientTransportAutoConfiguration;
                import org.springframework.ai.mcp.client.webflux.autoconfigure.SseWebFluxTransportAutoConfiguration;

                class Demo {
                    McpClientAutoConfiguration mcpClientAutoConfiguration = null;
                    McpToolCallbackAutoConfiguration mcpToolCallbackAutoConfiguration = null;
                    NamedClientMcpTransport namedClientMcpTransport = null;
                    StdioTransportAutoConfiguration stdioTransportAutoConfiguration = null;
                    McpAsyncClientConfigurer mcpAsyncClientConfigurer = null;
                    McpSyncClientConfigurer mcpSyncClientConfigurer = null;
                    McpClientCommonProperties mcpClientCommonProperties = null;
                    McpSseClientProperties mcpSseClientProperties = null;
                    McpStdioClientProperties mcpStdioClientProperties = null;
                    SseHttpClientTransportAutoConfiguration sseHttpClientTransportAutoConfiguration = null;
                    SseWebFluxTransportAutoConfiguration sseWebFluxTransportAutoConfiguration = null;
                }
                """
            )
        );
    }

    @Test
    void mcpServerAutoconfigureTypeChanges() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.ai.mcp.server.autoconfigure.McpServerAutoConfiguration;
                import org.springframework.ai.mcp.server.autoconfigure.McpServerStdioDisabledCondition;
                import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
                import org.springframework.ai.mcp.server.autoconfigure.McpWebFluxServerAutoConfiguration;
                import org.springframework.ai.mcp.server.autoconfigure.McpWebMvcServerAutoConfiguration;

                class Demo {
                    McpServerAutoConfiguration mcpServerAutoConfiguration = null;
                    McpServerStdioDisabledCondition mcpServerStdioDisabledCondition = null;
                    McpServerProperties mcpServerProperties = null;
                    McpWebFluxServerAutoConfiguration mcpWebFluxServerAutoConfiguration = null;
                    McpWebMvcServerAutoConfiguration mcpWebMvcServerAutoConfiguration = null;
                }
                """,
                """
                import org.springframework.ai.mcp.server.autoconfigure.McpServerSseWebFluxAutoConfiguration;
                import org.springframework.ai.mcp.server.autoconfigure.McpServerSseWebMvcAutoConfiguration;
                import org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration;
                import org.springframework.ai.mcp.server.common.autoconfigure.McpServerStdioDisabledCondition;
                import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerProperties;

                class Demo {
                    McpServerAutoConfiguration mcpServerAutoConfiguration = null;
                    McpServerStdioDisabledCondition mcpServerStdioDisabledCondition = null;
                    McpServerProperties mcpServerProperties = null;
                    McpServerSseWebFluxAutoConfiguration mcpWebFluxServerAutoConfiguration = null;
                    McpServerSseWebMvcAutoConfiguration mcpWebMvcServerAutoConfiguration = null;
                }
                """
            )
        );
    }

    @Test
    void springAiModelModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.ai.openai.audio.speech.Speech;
                import org.springframework.ai.openai.audio.speech.SpeechMessage;
                import org.springframework.ai.openai.audio.speech.SpeechModel;
                import org.springframework.ai.openai.audio.speech.SpeechPrompt;
                import org.springframework.ai.openai.audio.speech.SpeechResponse;
                import org.springframework.ai.openai.audio.speech.StreamingSpeechModel;

                class Demo {
                    Speech speech = null;
                    SpeechMessage speechMessage = null;
                    SpeechModel speechModel = null;
                    SpeechPrompt speechPrompt = null;
                    SpeechResponse speechResponse = null;
                    StreamingSpeechModel streamingSpeechModel = null;
                }
                """,
                """
                import org.springframework.ai.audio.tts.*;

                class Demo {
                    Speech speech = null;
                    TextToSpeechMessage speechMessage = null;
                    TextToSpeechModel speechModel = null;
                    TextToSpeechPrompt speechPrompt = null;
                    TextToSpeechResponse speechResponse = null;
                    StreamingTextToSpeechModel streamingSpeechModel = null;
                }
                """
            )
        );
    }

}
