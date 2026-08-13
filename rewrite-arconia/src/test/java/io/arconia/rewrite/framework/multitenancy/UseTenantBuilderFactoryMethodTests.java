package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseTenantBuilderFactoryMethod}.
 */
class UseTenantBuilderFactoryMethodTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseTenantBuilderFactoryMethod())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.29"));
    }

    @Test
    @DocumentExample
    void useFactoryMethod() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            Tenant create() {
                                return new Tenant.Builder().identifier("acme").build();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            Tenant create() {
                                return Tenant.builder().identifier("acme").build();
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
                            static class Tenant {
                                static class Builder {
                                    Builder() {}
                                }
                            }

                            Object create() {
                                return new Tenant.Builder();
                            }
                        }
                        """
                )
        );
    }

}
