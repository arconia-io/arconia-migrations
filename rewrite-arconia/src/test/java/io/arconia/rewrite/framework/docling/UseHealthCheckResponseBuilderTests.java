package io.arconia.rewrite.framework.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseHealthCheckResponseBuilder}.
 */
class UseHealthCheckResponseBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseHealthCheckResponseBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "docling-serve-api-0.1"));
    }

    @Test
    @DocumentExample
    void migratesStringLiteralConstructor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.api.serve.health.HealthCheckResponse;

                        class Demo {
                            HealthCheckResponse response() {
                                return new HealthCheckResponse("ok");
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.health.HealthCheckResponse;

                        class Demo {
                            HealthCheckResponse response() {
                                return HealthCheckResponse.builder().status("ok").build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migratesStringVariableConstructor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.api.serve.health.HealthCheckResponse;

                        class Demo {
                            HealthCheckResponse response(String status) {
                                return new HealthCheckResponse(status);
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.health.HealthCheckResponse;

                        class Demo {
                            HealthCheckResponse response(String status) {
                                return HealthCheckResponse.builder().status(status).build();
                            }
                        }
                        """
                )
        );
    }

}
