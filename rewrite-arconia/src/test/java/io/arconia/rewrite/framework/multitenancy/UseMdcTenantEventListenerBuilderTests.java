package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseMdcTenantEventListenerBuilder}.
 */
class UseMdcTenantEventListenerBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseMdcTenantEventListenerBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.29"));
    }

    @Test
    @DocumentExample
    void useBuilderWithTenantIdentifierKey() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;

                        class Demo {
                            MdcTenantEventListener create(String tenantIdentifierKey) {
                                return new MdcTenantEventListener(tenantIdentifierKey);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;

                        class Demo {
                            MdcTenantEventListener create(String tenantIdentifierKey) {
                                return MdcTenantEventListener.builder().tenantIdentifierKey(tenantIdentifierKey).build();
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
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;

                        class Demo {
                            MdcTenantEventListener create() {
                                return new MdcTenantEventListener();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;

                        class Demo {
                            MdcTenantEventListener create() {
                                return MdcTenantEventListener.builder().build();
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
                            static class MdcTenantEventListener {
                                MdcTenantEventListener() {}
                                MdcTenantEventListener(String tenantIdentifierKey) {}
                            }

                            Object create() {
                                return new MdcTenantEventListener("tenant.id");
                            }
                        }
                        """
                )
        );
    }

}
