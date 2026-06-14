package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_22_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_22")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-dev-services-core-0.21", "arconia-dev-services-otel-collector-0.21"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.config.DevServicesProperties;
                        import io.arconia.dev.services.core.config.JdbcDevServicesProperties;
                        import io.arconia.dev.services.opentelemetry.OpenTelemetryDevServicesAutoConfiguration;
                        import io.arconia.dev.services.opentelemetry.OpenTelemetryDevServicesProperties;

                        class Demo {
                            DevServicesProperties properties;
                            JdbcDevServicesProperties jdbcProperties;
                            OpenTelemetryDevServicesProperties otelProperties;
                            OpenTelemetryDevServicesAutoConfiguration config;
                        }
                        """,
                        """
                        import io.arconia.dev.services.api.config.BaseDevServicesProperties;
                        import io.arconia.dev.services.api.config.JdbcDevServicesProperties;
                        import io.arconia.dev.services.opentelemetry.collector.OtelCollectorDevServicesAutoConfiguration;
                        import io.arconia.dev.services.opentelemetry.collector.OtelCollectorDevServicesProperties;

                        class Demo {
                            BaseDevServicesProperties properties;
                            JdbcDevServicesProperties jdbcProperties;
                            OtelCollectorDevServicesProperties otelProperties;
                            OtelCollectorDevServicesAutoConfiguration config;
                        }
                        """
                )
        );
    }

    @Test
    void propertyValueChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.artemis.shared=dev-mode
                        """,
                        """
                        arconia.dev.services.artemis.shared=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void methodChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.config.DevServicesProperties;

                        class Demo {
                            void call(DevServicesProperties properties) {
                                properties.getShared();
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.api.config.BaseDevServicesProperties;

                        class Demo {
                            void call(BaseDevServicesProperties properties) {
                                properties.isShared();
                            }
                        }
                        """
                )
        );
    }

}
