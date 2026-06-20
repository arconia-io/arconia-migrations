package io.arconia.rewrite.framework;

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
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_27_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_27")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "arconia-observation-0.26",
                "arconia-langsmith-semantic-conventions-0.26",
                "arconia-openinference-semantic-conventions-0.26",
                "arconia-opentelemetry-semantic-conventions-0.26",
                "arconia-multitenancy-core-0.26",
                "arconia-multitenancy-web-0.26"));
    }

    @Test
    void observationSemanticConventionsDependencyChanges() {
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
                            implementation "io.arconia:arconia-langsmith-semantic-conventions"
                            implementation "io.arconia:arconia-openinference-semantic-conventions"
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
                            implementation "io.arconia:arconia-opentelemetry-ai-semantic-conventions"
                            implementation "io.arconia:arconia-openinference-ai-semantic-conventions"
                        }
                        """
                )
        );
    }

    @Test
    @DocumentExample
    void observationTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.conventions.ObservationConventionsProvider;
                        import io.arconia.observation.autoconfigure.MultipleObservationConventionsException;

                        class Demo {
                            ObservationConventionsProvider provider;
                            MultipleObservationConventionsException ex;
                        }
                        """,
                        """
                        import io.arconia.observation.autoconfigure.MultipleAiObservationConventionsException;
                        import io.arconia.observation.conventions.AiObservationConventionsProvider;

                        class Demo {
                            AiObservationConventionsProvider provider;
                            MultipleAiObservationConventionsException ex;
                        }
                        """
                )
        );
    }

    @Test
    void observationPackageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.openinference.autoconfigure.OpenInferenceAutoConfiguration;
                        import io.arconia.observation.openinference.instrumentation.OpenInferenceAdvisorObservationConvention;

                        class Demo {
                            OpenInferenceAutoConfiguration config;
                            OpenInferenceAdvisorObservationConvention convention;
                        }
                        """,
                        """
                        import io.arconia.observation.openinference.ai.autoconfigure.OpenInferenceAutoConfiguration;
                        import io.arconia.observation.openinference.ai.instrumentation.OpenInferenceAdvisorObservationConvention;

                        class Demo {
                            OpenInferenceAutoConfiguration config;
                            OpenInferenceAdvisorObservationConvention convention;
                        }
                        """
                )
        );
    }

    @Test
    void observationOpenTelemetryPackageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.opentelemetry.instrumentation.genai.OpenTelemetryChatModelObservationConvention;
                        import io.arconia.observation.opentelemetry.instrumentation.genai.OpenTelemetryChatClientObservationConvention;

                        class Demo {
                            OpenTelemetryChatModelObservationConvention chatModel;
                            OpenTelemetryChatClientObservationConvention chatClient;
                        }
                        """,
                        """
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryChatModelObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryChatClientObservationConvention;

                        class Demo {
                            OpenTelemetryChatModelObservationConvention chatModel;
                            OpenTelemetryChatClientObservationConvention chatClient;
                        }
                        """
                )
        );
    }

    @Test
    void observationOpenTelemetryTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.opentelemetry.instrumentation.genai.GenAiMoreIncubatingAttributes;
                        import io.arconia.observation.opentelemetry.instrumentation.genai.OpenTelemetryEmbeddingModelMeterObservationHandler;
                        import io.arconia.observation.opentelemetry.instrumentation.genai.OpenTelemetryToolCallingModelObservationConvention;

                        class Demo {
                            GenAiMoreIncubatingAttributes attributes;
                            OpenTelemetryEmbeddingModelMeterObservationHandler embeddingHandler;
                            OpenTelemetryToolCallingModelObservationConvention toolCallingConvention;
                        }
                        """,
                        """
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryEmbeddingMeterObservationHandler;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryToolCallingObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.shared.GenAiAttributes;

                        class Demo {
                            GenAiAttributes attributes;
                            OpenTelemetryEmbeddingMeterObservationHandler embeddingHandler;
                            OpenTelemetryToolCallingObservationConvention toolCallingConvention;
                        }
                        """
                )
        );
    }

    @Test
    void observationLangSmithPackageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.langsmith.instrumentation.LangSmithChatModelObservationConvention;
                        import io.arconia.observation.langsmith.instrumentation.LangSmithEmbeddingModelObservationConvention;

                        class Demo {
                            LangSmithChatModelObservationConvention chatModel;
                            LangSmithEmbeddingModelObservationConvention embedding;
                        }
                        """,
                        """
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithChatModelObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithEmbeddingModelObservationConvention;

                        class Demo {
                            LangSmithChatModelObservationConvention chatModel;
                            LangSmithEmbeddingModelObservationConvention embedding;
                        }
                        """
                )
        );
    }

    @Test
    void observationLangSmithTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.observation.langsmith.autoconfigure.LangSmithConventionsAutoConfiguration;
                        import io.arconia.observation.langsmith.instrumentation.LangSmithAdvisorObservationConvention;

                        class Demo {
                            LangSmithConventionsAutoConfiguration autoConfig;
                            LangSmithAdvisorObservationConvention advisor;
                        }
                        """,
                        """
                        import io.arconia.observation.opentelemetry.ai.autoconfigure.LangSmithFlavorConfiguration;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithAiAdvisorConvention;

                        class Demo {
                            LangSmithFlavorConfiguration autoConfig;
                            LangSmithAiAdvisorConvention advisor;
                        }
                        """
                )
        );
    }

    @Test
    void observationOpenTelemetryPropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.generative-ai.enabled=true
                        arconia.observations.conventions.opentelemetry.generative-ai.inference.capture-content=span-events
                        arconia.observations.conventions.opentelemetry.generative-ai.inference.include-tool-definitions=true
                        arconia.observations.conventions.opentelemetry.generative-ai.tool-execution.include-content=true
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.enabled=true
                        arconia.observations.conventions.opentelemetry.ai.capture-content=span-events
                        arconia.observations.conventions.opentelemetry.ai.include-tool-definitions=true
                        arconia.observations.conventions.opentelemetry.ai.include-tool-call-content=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationOpenTelemetryPropertyChangesInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                generative-ai:
                                  enabled: true
                                  inference:
                                    capture-content: span-events
                                    include-tool-definitions: true
                                  tool-execution:
                                    include-content: true
                        """,
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                  ai.enabled: true
                                  ai.capture-content: span-events
                                  ai.include-tool-definitions: true
                                  ai.include-tool-call-content: true
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void observationLangSmithTypePropertyRename() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.type=langsmith
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.flavor=langsmith
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationLangSmithTypePropertyRenameInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          observations:
                            conventions:
                              type: langsmith
                        """,
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                ai:
                                  flavor: langsmith
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void observationLangSmithPropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.langsmith.inference.include-content=true
                        arconia.observations.conventions.langsmith.inference.include-tool-definitions=true
                        arconia.observations.conventions.langsmith.tool-execution.include-content=true
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-content=span-events
                        arconia.observations.conventions.opentelemetry.ai.include-tool-definitions=true
                        arconia.observations.conventions.opentelemetry.ai.include-tool-call-content=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationLangSmithIncludeContentFalsePropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.langsmith.inference.include-content=false
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-content=none
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationOpenInferenceTypePropertyRename() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.type=openinference
                        """,
                        """
                        arconia.observations.conventions.openinference.ai.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationOpenInferenceTypePropertyRenameInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          observations:
                            conventions:
                              type: openinference
                        """,
                        """
                        arconia:
                          observations:
                            conventions:
                              openinference:
                                ai:
                                  enabled: true
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void observationOpenInferencePropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.openinference.base64-image-max-length=1000
                        arconia.observations.conventions.openinference.hide-choices=true
                        arconia.observations.conventions.openinference.hide-embeddings-text=false
                        arconia.observations.conventions.openinference.hide-embeddings-vectors=true
                        arconia.observations.conventions.openinference.hide-llm-invocation-parameters=false
                        arconia.observations.conventions.openinference.hide-inputs=true
                        arconia.observations.conventions.openinference.hide-input-images=false
                        arconia.observations.conventions.openinference.hide-input-messages=true
                        arconia.observations.conventions.openinference.hide-input-text=false
                        arconia.observations.conventions.openinference.hide-outputs=true
                        arconia.observations.conventions.openinference.hide-output-text=false
                        arconia.observations.conventions.openinference.hide-output-messages=true
                        arconia.observations.conventions.openinference.hide-prompts=false
                        arconia.observations.conventions.openinference.project-name=my-project
                        """,
                        """
                        arconia.observations.conventions.openinference.ai.base64-image-max-length=1000
                        arconia.observations.conventions.openinference.ai.hide-choices=true
                        arconia.observations.conventions.openinference.ai.hide-embeddings-text=false
                        arconia.observations.conventions.openinference.ai.hide-embeddings-vectors=true
                        arconia.observations.conventions.openinference.ai.hide-llm-invocation-parameters=false
                        arconia.observations.conventions.openinference.ai.hide-inputs=true
                        arconia.observations.conventions.openinference.ai.hide-input-images=false
                        arconia.observations.conventions.openinference.ai.hide-input-messages=true
                        arconia.observations.conventions.openinference.ai.hide-input-text=false
                        arconia.observations.conventions.openinference.ai.hide-outputs=true
                        arconia.observations.conventions.openinference.ai.hide-output-text=false
                        arconia.observations.conventions.openinference.ai.hide-output-messages=true
                        arconia.observations.conventions.openinference.ai.hide-prompts=false
                        arconia.observations.conventions.openinference.ai.project-name=my-project
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
