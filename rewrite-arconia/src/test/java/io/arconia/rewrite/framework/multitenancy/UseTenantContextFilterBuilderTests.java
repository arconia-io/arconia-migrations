package io.arconia.rewrite.framework.multitenancy;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseTenantContextFilterBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseTenantContextFilterBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.26",
                        "arconia-multitenancy-web-0.26", "spring-context-7.0"));
    }

    @Test
    void useBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.tenantdetails.TenantVerifier;
                        import io.arconia.multitenancy.web.context.filters.TenantContextFilter;
                        import io.arconia.multitenancy.web.context.filters.TenantContextIgnorePathMatcher;
                        import io.arconia.multitenancy.web.context.resolvers.HttpRequestTenantResolver;
                        import org.springframework.context.ApplicationEventPublisher;

                        class Demo {
                            TenantContextFilter create(HttpRequestTenantResolver resolver,
                                    TenantContextIgnorePathMatcher matcher,
                                    ApplicationEventPublisher publisher,
                                    TenantVerifier verifier) {
                                return new TenantContextFilter(resolver, matcher, publisher, verifier);
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.tenantdetails.TenantVerifier;
                        import io.arconia.multitenancy.web.context.filters.TenantContextFilter;
                        import io.arconia.multitenancy.web.context.filters.TenantContextIgnorePathMatcher;
                        import io.arconia.multitenancy.web.context.resolvers.HttpRequestTenantResolver;
                        import org.springframework.context.ApplicationEventPublisher;

                        class Demo {
                            TenantContextFilter create(HttpRequestTenantResolver resolver,
                                    TenantContextIgnorePathMatcher matcher,
                                    ApplicationEventPublisher publisher,
                                    TenantVerifier verifier) {
                                return TenantContextFilter.builder().httpRequestTenantResolver(resolver).tenantContextIgnorePathMatcher(matcher).eventPublisher(publisher).tenantVerifier(verifier).build();
                            }
                        }
                        """
                )
        );
    }

}
