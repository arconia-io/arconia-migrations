package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseTenantObservationFilterBuilder}.
 */
class UseTenantObservationFilterBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseTenantObservationFilterBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.29"));
    }

    @Test
    @DocumentExample
    void useBuilderWithTenantIdentifierKeyAndCardinality() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.observability.Cardinality;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;

                        class Demo {
                            TenantObservationFilter create(String tenantIdentifierKey, Cardinality cardinality) {
                                return new TenantObservationFilter(tenantIdentifierKey, cardinality);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.observability.Cardinality;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;

                        class Demo {
                            TenantObservationFilter create(String tenantIdentifierKey, Cardinality cardinality) {
                                return TenantObservationFilter.builder().tenantIdentifierKey(tenantIdentifierKey).cardinality(cardinality).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void useBuilderWithDefaults() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;

                        class Demo {
                            TenantObservationFilter create() {
                                return new TenantObservationFilter();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;

                        class Demo {
                            TenantObservationFilter create() {
                                return TenantObservationFilter.builder().build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void doesNotChangeUnrelatedType() {
        rewriteRun(
                //language=java
                java(
                        """
                        class Demo {
                            static class TenantObservationFilter {
                                TenantObservationFilter() {}
                            }

                            Object create() {
                                return new TenantObservationFilter();
                            }
                        }
                        """
                )
        );
    }

}
