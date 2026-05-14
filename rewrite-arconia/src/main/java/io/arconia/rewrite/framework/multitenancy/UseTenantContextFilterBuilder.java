package io.arconia.rewrite.framework.multitenancy;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Replaces {@code new TenantContextFilter(...)} with builder pattern {@code TenantContextFilter.builder()....build()}.
 */
public class UseTenantContextFilterBuilder extends Recipe {

    private static final String FQN_TENANT_CONTEXT_FILTER
            = "io.arconia.multitenancy.web.context.filters.TenantContextFilter";

    private static final MethodMatcher TENANT_CONTEXT_FILTER_CONSTRUCTOR_MATCHER = new MethodMatcher(
            "io.arconia.multitenancy.web.context.filters.TenantContextFilter <constructor>("
            + "io.arconia.multitenancy.web.context.resolvers.HttpRequestTenantResolver, "
            + "io.arconia.multitenancy.web.context.filters.TenantContextIgnorePathMatcher, "
            + "org.springframework.context.ApplicationEventPublisher, "
            + "io.arconia.multitenancy.core.tenantdetails.TenantVerifier)");

    @Override
    public String getDisplayName() {
        return "Use `TenantContextFilter.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new TenantContextFilter(...)` with `TenantContextFilter.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(TENANT_CONTEXT_FILTER_CONSTRUCTOR_MATCHER),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (TENANT_CONTEXT_FILTER_CONSTRUCTOR_MATCHER.matches(nc)) {
                            List<Expression> args = nc.getArguments();
                            maybeAddImport(FQN_TENANT_CONTEXT_FILTER);
                            return JavaTemplate.builder("TenantContextFilter.builder()"
                                            + ".httpRequestTenantResolver(#{any(io.arconia.multitenancy.web.context.resolvers.HttpRequestTenantResolver)})"
                                            + ".tenantContextIgnorePathMatcher(#{any(io.arconia.multitenancy.web.context.filters.TenantContextIgnorePathMatcher)})"
                                            + ".eventPublisher(#{any(org.springframework.context.ApplicationEventPublisher)})"
                                            + ".tenantVerifier(#{any(io.arconia.multitenancy.core.tenantdetails.TenantVerifier)})"
                                            + ".build()")
                                    .imports(FQN_TENANT_CONTEXT_FILTER)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "arconia-multitenancy-web-0.27", "spring-context-7.0"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1), args.get(2), args.get(3));
                        }

                        return nc;
                    }
                }
        );
    }

}
