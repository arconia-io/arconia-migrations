package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseCookieTenantResolverBuilder}.
 */
class UseCookieTenantResolverBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseCookieTenantResolverBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-web-0.29"));
    }

    @Test
    @DocumentExample
    void useBuilderWithTenantCookieName() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;

                        class Demo {
                            CookieTenantResolver create(String tenantCookieName) {
                                return new CookieTenantResolver(tenantCookieName);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;

                        class Demo {
                            CookieTenantResolver create(String tenantCookieName) {
                                return CookieTenantResolver.builder().tenantCookieName(tenantCookieName).build();
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
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;

                        class Demo {
                            CookieTenantResolver create() {
                                return new CookieTenantResolver();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;

                        class Demo {
                            CookieTenantResolver create() {
                                return CookieTenantResolver.builder().build();
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
                            static class CookieTenantResolver {
                                CookieTenantResolver() {}
                                CookieTenantResolver(String tenantCookieName) {}
                            }

                            Object create() {
                                return new CookieTenantResolver("TENANT-ID");
                            }
                        }
                        """
                )
        );
    }

}
