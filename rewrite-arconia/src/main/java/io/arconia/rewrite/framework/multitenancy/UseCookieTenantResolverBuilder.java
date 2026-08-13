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
 * Replaces {@code new CookieTenantResolver(...)} with builder pattern {@code CookieTenantResolver.builder()....build()}.
 */
public class UseCookieTenantResolverBuilder extends Recipe {

    private static final String FQN_COOKIE_TENANT_RESOLVER
            = "io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver";

    private static final MethodMatcher DEFAULT_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_COOKIE_TENANT_RESOLVER + " <constructor>()");

    private static final MethodMatcher TENANT_COOKIE_NAME_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_COOKIE_TENANT_RESOLVER + " <constructor>(java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `CookieTenantResolver.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new CookieTenantResolver(...)` with `CookieTenantResolver.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(DEFAULT_CONSTRUCTOR_MATCHER),
                        new UsesMethod<>(TENANT_COOKIE_NAME_CONSTRUCTOR_MATCHER)),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (DEFAULT_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_COOKIE_TENANT_RESOLVER);
                            return template("CookieTenantResolver.builder().build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace());
                        }

                        if (TENANT_COOKIE_NAME_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_COOKIE_TENANT_RESOLVER);
                            return template("CookieTenantResolver.builder()"
                                    + ".tenantCookieName(#{any(java.lang.String)})"
                                    + ".build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace(), nc.getArguments().get(0));
                        }

                        return nc;
                    }

                    private JavaTemplate template(String code, ExecutionContext ctx) {
                        return JavaTemplate.builder(code)
                                .imports(FQN_COOKIE_TENANT_RESOLVER)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,
                                        "arconia-multitenancy-core-0.30", "arconia-multitenancy-web-0.30"))
                                .build();
                    }
                }
        );
    }

}
