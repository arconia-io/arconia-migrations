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
 * Replaces {@code new MdcTenantEventListener(...)} with builder pattern {@code MdcTenantEventListener.builder()....build()}.
 */
public class UseMdcTenantEventListenerBuilder extends Recipe {

    private static final String FQN_MDC_TENANT_EVENT_LISTENER
            = "io.arconia.multitenancy.core.observability.MdcTenantEventListener";

    private static final MethodMatcher DEFAULT_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_MDC_TENANT_EVENT_LISTENER + " <constructor>()");

    private static final MethodMatcher TENANT_IDENTIFIER_KEY_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_MDC_TENANT_EVENT_LISTENER + " <constructor>(java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `MdcTenantEventListener.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new MdcTenantEventListener(...)` with `MdcTenantEventListener.builder()....build()`.";
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
                            maybeAddImport(FQN_MDC_TENANT_EVENT_LISTENER);
                            return template("MdcTenantEventListener.builder().build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace());
                        }

                        if (TENANT_IDENTIFIER_KEY_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_MDC_TENANT_EVENT_LISTENER);
                            return template("MdcTenantEventListener.builder()"
                                    + ".tenantIdentifierKey(#{any(java.lang.String)})"
                                    + ".build()", ctx)
                                    .apply(getCursor(), nc.getCoordinates().replace(), nc.getArguments().get(0));
                        }

                        return nc;
                    }

                    private JavaTemplate template(String code, ExecutionContext ctx) {
                        return JavaTemplate.builder(code)
                                .imports(FQN_MDC_TENANT_EVENT_LISTENER)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "arconia-multitenancy-core-0.30"))
                                .build();
                    }
                }
        );
    }

}
