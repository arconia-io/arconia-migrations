package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_33_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_33")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-ai-docling-document-reader-0.32", "arconia-testcontainers-phoenix-0.32", "testcontainers-2.0"));
    }

    @Test
    @DocumentExample
    void doclingDocumentMetadataRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.ai.document.docling.DocumentMetadata;

                        class DocumentKeys {

                            String fileNameKey() {
                                return DocumentMetadata.FILE_NAME.value();
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.ai.document.docling.DoclingDocumentMetadata;

                        class DocumentKeys {

                            String fileNameKey() {
                                return DoclingDocumentMetadata.FILE_NAME.value();
                            }

                        }
                        """)
        );
    }

    @Test
    void phoenixContainerOtlpGrpcPortRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.testcontainers.phoenix.PhoenixContainer;

                        class PhoenixEndpoints {

                            String grpcEndpoint(PhoenixContainer phoenix) {
                                return "localhost:" + phoenix.getGrpcPort();
                            }

                            int containerGrpcPort() {
                                return PhoenixContainer.GRPC_PORT;
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.testcontainers.phoenix.PhoenixContainer;

                        class PhoenixEndpoints {

                            String grpcEndpoint(PhoenixContainer phoenix) {
                                return "localhost:" + phoenix.getOtlpGrpcPort();
                            }

                            int containerGrpcPort() {
                                return PhoenixContainer.OTLP_GRPC_PORT;
                            }

                        }
                        """)
        );
    }

    @Test
    void observabilityDevServiceEnabledInTestsFollowsTelemetryExport() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.lgtm.skip-in-tests=false
                        arconia.dev.services.lgtm.port=3000
                        """,
                        """
                        spring.test.tracing.export=true
                        spring.test.metrics.export=true
                        spring.test.logging.export=true
                        arconia.dev.services.lgtm.port=3000
                        """,
                        s -> s.path("src/test/resources/application.properties"))
        );
    }

    @Test
    void observabilityDevServiceEnabledInTestsFollowsTelemetryExportInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            services:
                              lgtm:
                                skip-in-tests: false
                                port: 3000
                        """,
                        """
                        arconia:
                          dev:
                            services:
                              lgtm:
                                port: 3000
                        spring:
                          test:
                            logging:
                              export: true
                            metrics:
                              export: true
                            tracing:
                              export: true
                        """,
                        s -> s.path("src/test/resources/application.yml"))
        );
    }

    @Test
    void tracingDevServiceEnabledInTestsFollowsTracesExport() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.phoenix.skip-in-tests=false
                        """,
                        """
                        spring.test.tracing.export=true
                        """,
                        s -> s.path("src/test/resources/application.properties"))
        );
    }

    @Test
    void observabilityDevServiceSkipInTestsRemoved() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.lgtm.skip-in-tests=true
                        arconia.dev.services.lgtm.port=3000
                        """,
                        """
                        arconia.dev.services.lgtm.port=3000
                        """,
                        s -> s.path("src/test/resources/application.properties"))
        );
    }

    @Test
    void exemplarsFilterBecomesSpringBootExemplarsInclude() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.metrics.exemplars.filter=always-off
                        arconia.otel.metrics.cardinality-limit=1000
                        """,
                        """
                        management.tracing.exemplars.include=none
                        arconia.otel.metrics.cardinality-limit=1000
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void exemplarsFilterValuesMapped() {
        rewriteRun(
                //language=properties
                properties(
                        "arconia.otel.metrics.exemplars.filter=always-on",
                        "management.tracing.exemplars.include=all",
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        "arconia.otel.metrics.exemplars.filter=TRACE_BASED",
                        "management.tracing.exemplars.include=sampled-traces",
                        s -> s.path("src/main/resources/application-prod.properties"))
        );
    }

    @Test
    void exemplarsFilterBecomesSpringBootExemplarsIncludeInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          otel:
                            metrics:
                              cardinality-limit: 1000
                              exemplars:
                                filter: always-off
                        """,
                        """
                        arconia:
                          otel:
                            metrics:
                              cardinality-limit: 1000
                        management.tracing.exemplars.include: none
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
