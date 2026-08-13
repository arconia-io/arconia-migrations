package io.arconia.rewrite.framework.multitenancy;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

/**
 * Replaces {@code new HeaderTenantResolver(...)} with builder pattern {@code HeaderTenantResolver.builder()....build()}.
 */
public class UseHeaderTenantResolverBuilder extends Recipe {

    private static final String FQN_HEADER_TENANT_RESOLVER
            = "io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver";

    private static final MethodMatcher DEFAULT_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_HEADER_TENANT_RESOLVER + " <constructor>()");

    private static final MethodMatcher TENANT_HEADER_NAME_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_HEADER_TENANT_RESOLVER + " <constructor>(java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `HeaderTenantResolver.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new HeaderTenantResolver(...)` with `HeaderTenantResolver.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(DEFAULT_CONSTRUCTOR_MATCHER),
                        new UsesMethod<>(TENANT_HEADER_NAME_CONSTRUCTOR_MATCHER)),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (DEFAULT_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_HEADER_TENANT_RESOLVER);
                            return template("HeaderTenantResolver.builder().build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace());
                        }

                        if (TENANT_HEADER_NAME_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_HEADER_TENANT_RESOLVER);
                            return template("HeaderTenantResolver.builder()"
                                    + ".tenantHeaderName(#{any(java.lang.String)})"
                                    + ".build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace(), nc.getArguments().get(0));
                        }

                        return nc;
                    }

                    private JavaTemplate template(String code, ExecutionContext ctx) {
                        return JavaTemplate.builder(code)
                                .imports(FQN_HEADER_TENANT_RESOLVER)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,
                                        "arconia-multitenancy-core-0.30", "arconia-multitenancy-web-0.30"))
                                .build();
                    }
                }
        );
    }

}
