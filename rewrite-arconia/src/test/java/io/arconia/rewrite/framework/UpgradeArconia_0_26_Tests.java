package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_26_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_26")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "arconia-multitenancy-core-0.25",
                "arconia-openinference-semantic-conventions-0.25"));
    }

    @Test
    void multitenancyTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.exceptions.TenantResolutionException;
                        import io.arconia.multitenancy.core.context.events.MdcTenantContextEventListener;
                        import io.arconia.multitenancy.core.events.TenantEvent;
                        import io.arconia.multitenancy.core.autoconfigure.tenantdetails.TenantDetailsConfiguration;
                        import io.arconia.multitenancy.core.autoconfigure.tenantdetails.TenantDetailsProperties;
                        import io.arconia.multitenancy.core.autoconfigure.tenantdetails.PropertiesTenantDetailsService;
                        import io.arconia.multitenancy.core.events.TenantEventPublisher;
                        import io.arconia.multitenancy.core.events.DefaultTenantEventPublisher;
                        import io.arconia.multitenancy.core.context.TenantContextHolder;

                        class Demo {
                            TenantResolutionException ex;
                            MdcTenantContextEventListener listener;
                            TenantEvent event;
                            TenantDetailsConfiguration config;
                            TenantDetailsProperties props;
                            PropertiesTenantDetailsService service;
                            TenantEventPublisher publisher;
                            DefaultTenantEventPublisher defaultPublisher;
                            TenantContextHolder holder;
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.autoconfigure.PropertiesTenantDetailsService;
                        import io.arconia.multitenancy.core.autoconfigure.TenantDetailsConfiguration;
                        import io.arconia.multitenancy.core.autoconfigure.TenantDetailsProperties;
                        import io.arconia.multitenancy.core.context.TenantContext;
                        import io.arconia.multitenancy.core.exceptions.TenantVerificationException;
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;
                        import org.springframework.context.ApplicationEventPublisher;
                        import io.arconia.multitenancy.core.context.events.TenantEvent;

                        class Demo {
                            TenantVerificationException ex;
                            MdcTenantEventListener listener;
                            TenantEvent event;
                            TenantDetailsConfiguration config;
                            TenantDetailsProperties props;
                            PropertiesTenantDetailsService service;
                            ApplicationEventPublisher publisher;
                            ApplicationEventPublisher defaultPublisher;
                            TenantContext holder;
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyMethodChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.tenantdetails.TenantDetails;
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            void call(TenantDetails details) {
                                details.getIdentifier();
                                details.isEnabled();
                                details.getAttributes();
                            }
                            void anotherCall(Tenant tenant) {
                                tenant.getIdentifier();
                                tenant.isEnabled();
                                tenant.getAttributes();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.tenantdetails.TenantDetails;
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            void call(TenantDetails details) {
                                details.identifier();
                                details.enabled();
                                details.attributes();
                            }
                            void anotherCall(Tenant tenant) {
                                tenant.identifier();
                                tenant.enabled();
                                tenant.attributes();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyPropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.multitenancy.management.mdc.enabled=true
                        arconia.multitenancy.management.mdc.key=tenant-id
                        arconia.multitenancy.management.observations.enabled=true
                        arconia.multitenancy.management.observations.key=tenant-id
                        arconia.multitenancy.management.observations.cardinality=low
                        """,
                        """
                        arconia.multitenancy.logging.mdc.enabled=true
                        arconia.multitenancy.logging.mdc.key-name=tenant-id
                        arconia.multitenancy.observations.enabled=true
                        arconia.multitenancy.observations.key-name=tenant-id
                        arconia.multitenancy.observations.cardinality=low
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void observationTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.openinference.observation.instrumentation.ai.OpenInferenceTracingOptions;
                        import io.arconia.openinference.observation.instrumentation.ai.OpenInferenceGenerativeAiOnlyObservationPredicate;

                        class Demo {
                            OpenInferenceTracingOptions options;
                            OpenInferenceGenerativeAiOnlyObservationPredicate predicate;
                        }
                        """,
                        """
                        import io.arconia.observation.openinference.instrumentation.OpenInferenceGenerativeAiOnlyObservationPredicate;
                        import io.arconia.observation.openinference.instrumentation.OpenInferenceOptions;

                        class Demo {
                            OpenInferenceOptions options;
                            OpenInferenceGenerativeAiOnlyObservationPredicate predicate;
                        }
                        """
                )
        );
    }

    @Test
    void observationPropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.generative-ai.openinference.enabled=true
                        arconia.observations.generative-ai.openinference.exclusive=true
                        arconia.observations.generative-ai.openinference.traces.base64-image-max-length=1000
                        arconia.observations.generative-ai.openinference.traces.hide-embedding-vectors=true
                        arconia.observations.generative-ai.openinference.traces.hide-llm-invocation-parameters=false
                        arconia.observations.generative-ai.openinference.traces.hide-inputs=true
                        arconia.observations.generative-ai.openinference.traces.hide-input-images=false
                        arconia.observations.generative-ai.openinference.traces.hide-input-messages=true
                        arconia.observations.generative-ai.openinference.traces.hide-input-text=false
                        arconia.observations.generative-ai.openinference.traces.hide-outputs=true
                        arconia.observations.generative-ai.openinference.traces.hide-output-text=false
                        arconia.observations.generative-ai.openinference.traces.hide-output-messages=true
                        arconia.observations.generative-ai.openinference.traces.hide-prompts=false
                        """,
                        """
                        arconia.observations.conventions.type=openinference
                        # Property removed in Arconia 0.26. The filtering of observations is now left to the telemetry collector.
                        # arconia.observations.generative-ai.openinference.exclusive=true
                        arconia.observations.conventions.openinference.base64-image-max-length=1000
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
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        """
                        arconia.observations.generative-ai.openinference.enabled=false
                        """,
                        """
                        arconia.observations.conventions.type=micrometer
                        """,
                        s -> s.path("src/main/resources/application-another.properties"))
        );
    }

}
