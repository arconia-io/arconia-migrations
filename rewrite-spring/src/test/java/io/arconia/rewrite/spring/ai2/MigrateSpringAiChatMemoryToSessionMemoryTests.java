package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class MigrateSpringAiChatMemoryToSessionMemoryTests implements RewriteTest {

    private static final String POM_BEFORE = pom("spring-ai-starter-model-chat-memory-repository-jdbc");

    private static final String POM_AFTER = pom("spring-ai-starter-model-session-jdbc");

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai2.MigrateSpringAiChatMemoryToSessionMemory")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-client-chat-2.1", "spring-ai-model-2.1", "spring-core-7.0"));
    }

    @Test
    @DocumentExample
    void migrateChatMemoryToSessionMemory() {
        rewriteRun(
                pomXml(POM_BEFORE, POM_AFTER),
                //language=java
                java(
                        """
                        package com.example;

                        import java.util.List;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
                        import org.springframework.ai.chat.memory.ChatMemory;
                        import org.springframework.ai.chat.messages.Message;

                        class ChatService {

                            private final ChatClient chatClient;

                            private final ChatMemory chatMemory;

                            ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
                                this.chatClient = chatClientBuilder
                                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                                        .build();
                                this.chatMemory = chatMemory;
                            }

                            String chat(String conversationId, String message) {
                                return chatClient.prompt()
                                        .user(message)
                                        .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, conversationId))
                                        .call()
                                        .content();
                            }

                            List<Message> history(String conversationId) {
                                return chatMemory.get(conversationId);
                            }

                            void reset(String conversationId) {
                                chatMemory.clear(conversationId);
                            }

                        }
                        """,
                        """
                        package com.example;

                        import java.util.List;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.client.advisor.api.MemoryAdvisor;
                        import org.springframework.ai.chat.messages.Message;
                        import org.springframework.ai.session.EventFilter;
                        import org.springframework.ai.session.SessionService;
                        import org.springframework.ai.session.advisor.SessionAdvisors;

                        class ChatService {

                            private final ChatClient chatClient;

                            private final SessionService chatMemory;

                            ChatService(ChatClient.Builder chatClientBuilder, SessionService chatMemory) {
                                this.chatClient = chatClientBuilder
                                        .defaultAdvisors(SessionAdvisors.builder(chatMemory).memory(memory -> memory.eventFilter(EventFilter.lastN(20))).build())
                                        .build();
                                this.chatMemory = chatMemory;
                            }

                            String chat(String conversationId, String message) {
                                return chatClient.prompt()
                                        .user(message)
                                        .advisors(advisors -> advisors.param(MemoryAdvisor.CONVERSATION_ID, conversationId))
                                        .call()
                                        .content();
                            }

                            List<Message> history(String conversationId) {
                                return chatMemory.getActiveMessages(conversationId);
                            }

                            void reset(String conversationId) {
                                chatMemory.delete(conversationId);
                            }

                        }
                        """
                )
        );
    }

    @Test
    void keepOtherAdvisorsAndOrder() {
        rewriteRun(
                pomXml(POM_BEFORE, POM_AFTER),
                //language=java
                java(
                        """
                        package com.example;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
                        import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
                        import org.springframework.ai.chat.memory.ChatMemory;

                        class ChatConfiguration {

                            ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
                                return chatClientBuilder
                                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).order(10).build(), new SimpleLoggerAdvisor())
                                        .build();
                            }

                        }
                        """,
                        """
                        package com.example;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
                        import org.springframework.ai.session.EventFilter;
                        import org.springframework.ai.session.SessionService;
                        import org.springframework.ai.session.advisor.SessionAdvisors;

                        class ChatConfiguration {

                            ChatClient chatClient(ChatClient.Builder chatClientBuilder, SessionService chatMemory) {
                                return chatClientBuilder
                                        .defaultAdvisors(new SimpleLoggerAdvisor())
                                        .defaultAdvisors(SessionAdvisors.builder(chatMemory).memory(memory -> memory.eventFilter(EventFilter.lastN(20)).order(10)).build())
                                        .build();
                            }

                        }
                        """
                )
        );
    }

    @Test
    void migrateJdbcRepositoryProperties() {
        rewriteRun(
                pomXml(POM_BEFORE, POM_AFTER),
                //language=properties
                properties(
                        """
                        spring.ai.chat.memory.repository.jdbc.initialize-schema=always
                        spring.ai.chat.memory.repository.jdbc.platform=postgresql
                        spring.ai.chat.memory.repository.jdbc.schema=classpath:schema-chat-memory.sql
                        """,
                        """
                        spring.ai.session.repository.jdbc.initialize-schema=always
                        spring.ai.session.repository.jdbc.platform=postgresql
                        # The chat memory schema script does not create the session memory tables. Use spring.ai.session.repository.jdbc.schema with a session schema script instead.
                        # spring.ai.chat.memory.repository.jdbc.schema=classpath:schema-chat-memory.sql
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          ai:
                            chat:
                              memory:
                                repository:
                                  jdbc:
                                    continue-on-error: true
                        """,
                        """
                        spring:
                          ai:
                            session.repository.jdbc.continue-on-error: true
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void migrateGradleStarter() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation platform("org.springframework.ai:spring-ai-bom:2.1.0-RC1")
                            implementation "org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc"
                        }
                        """,
                        """
                        plugins {
                            id 'java'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation platform("org.springframework.ai:spring-ai-bom:2.1.0-RC1")
                            implementation "org.springframework.ai:spring-ai-starter-model-session-jdbc"
                        }
                        """
                )
        );
    }

    @Test
    void leaveProjectsWithoutSessionCounterpartUnchanged() {
        // A chat memory built in code sets its own store and window.
        rewriteRun(
                pomXml(POM_BEFORE),
                //language=java
                java(
                        """
                        package com.example;

                        import org.springframework.ai.chat.memory.ChatMemory;
                        import org.springframework.ai.chat.memory.ChatMemoryRepository;
                        import org.springframework.ai.chat.memory.MessageWindowChatMemory;

                        class ChatMemoryConfiguration {

                            ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
                                return MessageWindowChatMemory.builder()
                                        .chatMemoryRepository(chatMemoryRepository)
                                        .maxMessages(50)
                                        .build();
                            }

                        }
                        """
                ),
                chatServiceUnchanged()
        );
        // Messages appended directly need a session created first.
        rewriteRun(
                pomXml(POM_BEFORE),
                //language=java
                java(
                        """
                        package com.example;

                        import java.util.List;

                        import org.springframework.ai.chat.memory.ChatMemory;
                        import org.springframework.ai.chat.messages.Message;

                        class ChatImporter {

                            void importMessages(ChatMemory chatMemory, String conversationId, List<Message> messages) {
                                chatMemory.add(conversationId, messages);
                            }

                        }
                        """
                ),
                chatServiceUnchanged()
        );
        // An advisor held in a bean cannot become a list of advisors in place.
        rewriteRun(
                pomXml(POM_BEFORE),
                //language=java
                java(
                        """
                        package com.example;

                        import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
                        import org.springframework.ai.chat.memory.ChatMemory;

                        class AdvisorConfiguration {

                            MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory) {
                                return MessageChatMemoryAdvisor.builder(chatMemory).build();
                            }

                        }
                        """
                )
        );
        // Only the JDBC store has a session counterpart.
        rewriteRun(
                pomXml(pom("spring-ai-starter-model-chat-memory")),
                chatServiceUnchanged()
        );
    }

    private static SourceSpecs chatServiceUnchanged() {
        //language=java
        return java(
                """
                package com.example;

                import org.springframework.ai.chat.client.ChatClient;
                import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
                import org.springframework.ai.chat.memory.ChatMemory;

                class ChatService {

                    private final ChatClient chatClient;

                    ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
                        this.chatClient = chatClientBuilder
                                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                                .build();
                    }

                }
                """
        );
    }

    private static String pom(String artifactId) {
        //language=xml
        return """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>0.0.1</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.ai</groupId>
                            <artifactId>%s</artifactId>
                            <version>2.1.0-RC1</version>
                        </dependency>
                    </dependencies>
                </project>
                """.formatted(artifactId);
    }

}
