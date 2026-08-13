package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseHeaderTenantResolverBuilder}.
 */
class UseHeaderTenantResolverBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseHeaderTenantResolverBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-web-0.29"));
    }

    @Test
    @DocumentExample
    void useBuilderWithTenantHeaderName() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            HeaderTenantResolver create(String tenantHeaderName) {
                                return new HeaderTenantResolver(tenantHeaderName);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            HeaderTenantResolver create(String tenantHeaderName) {
                                return HeaderTenantResolver.builder().tenantHeaderName(tenantHeaderName).build();
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
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            HeaderTenantResolver create() {
                                return new HeaderTenantResolver();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            HeaderTenantResolver create() {
                                return HeaderTenantResolver.builder().build();
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
                            static class HeaderTenantResolver {
                                HeaderTenantResolver() {}
                                HeaderTenantResolver(String tenantHeaderName) {}
                            }

                            Object create() {
                                return new HeaderTenantResolver("X-TenantId");
                            }
                        }
                        """
                )
        );
    }

}
