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
 * Replaces {@code new TenantObservationFilter(...)} with builder pattern {@code TenantObservationFilter.builder()....build()}.
 */
public class UseTenantObservationFilterBuilder extends Recipe {

    private static final String FQN_TENANT_OBSERVATION_FILTER
            = "io.arconia.multitenancy.core.observability.TenantObservationFilter";

    private static final MethodMatcher DEFAULT_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_TENANT_OBSERVATION_FILTER + " <constructor>()");

    private static final MethodMatcher TENANT_IDENTIFIER_KEY_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_TENANT_OBSERVATION_FILTER + " <constructor>("
            + "java.lang.String, "
            + "io.arconia.multitenancy.core.observability.Cardinality)");

    @Override
    public String getDisplayName() {
        return "Use `TenantObservationFilter.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new TenantObservationFilter(...)` with `TenantObservationFilter.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(DEFAULT_CONSTRUCTOR_MATCHER),
                        new UsesMethod<>(TENANT_IDENTIFIER_KEY_CONSTRUCTOR_MATCHER)),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (DEFAULT_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_TENANT_OBSERVATION_FILTER);
                            return template("TenantObservationFilter.builder().build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace());
                        }

                        if (TENANT_IDENTIFIER_KEY_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_TENANT_OBSERVATION_FILTER);
                            return template("TenantObservationFilter.builder()"
                                    + ".tenantIdentifierKey(#{any(java.lang.String)})"
                                    + ".cardinality(#{any(io.arconia.multitenancy.core.observability.Cardinality)})"
                                    + ".build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace(),
                                            nc.getArguments().get(0), nc.getArguments().get(1));
                        }

                        return nc;
                    }

                    private JavaTemplate template(String code, ExecutionContext ctx) {
                        return JavaTemplate.builder(code)
                                .imports(FQN_TENANT_OBSERVATION_FILTER)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,
                                        "arconia-multitenancy-core-0.30", "micrometer-observation-1.16"))
                                .build();
                    }
                }
        );
    }

}
