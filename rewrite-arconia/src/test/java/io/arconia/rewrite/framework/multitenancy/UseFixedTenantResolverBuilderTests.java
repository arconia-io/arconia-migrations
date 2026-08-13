package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseFixedTenantResolverBuilder}.
 */
class UseFixedTenantResolverBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseFixedTenantResolverBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.29"));
    }

    @Test
    @DocumentExample
    void useBuilderWithTenantIdentifier() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;

                        class Demo {
                            FixedTenantResolver create(String tenantIdentifier) {
                                return new FixedTenantResolver(tenantIdentifier);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;

                        class Demo {
                            FixedTenantResolver create(String tenantIdentifier) {
                                return FixedTenantResolver.builder().tenantIdentifier(tenantIdentifier).build();
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
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;

                        class Demo {
                            FixedTenantResolver create() {
                                return new FixedTenantResolver();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;

                        class Demo {
                            FixedTenantResolver create() {
                                return FixedTenantResolver.builder().build();
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
                            static class FixedTenantResolver {
                                FixedTenantResolver() {}
                                FixedTenantResolver(String tenantIdentifier) {}
                            }

                            Object create() {
                                return new FixedTenantResolver("acme");
                            }
                        }
                        """
                )
        );
    }

}
