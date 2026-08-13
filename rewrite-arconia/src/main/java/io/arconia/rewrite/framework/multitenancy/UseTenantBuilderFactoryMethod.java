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
 * Replaces {@code new Tenant.Builder()} with the factory method {@code Tenant.builder()}.
 */
public class UseTenantBuilderFactoryMethod extends Recipe {

    private static final String FQN_TENANT = "io.arconia.multitenancy.core.tenantdetails.Tenant";

    private static final MethodMatcher TENANT_BUILDER_CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_TENANT + "$Builder <constructor>()");

    @Override
    public String getDisplayName() {
        return "Use `Tenant.builder()` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new Tenant.Builder()` with `Tenant.builder()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(TENANT_BUILDER_CONSTRUCTOR_MATCHER),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (TENANT_BUILDER_CONSTRUCTOR_MATCHER.matches(nc)) {
                            maybeAddImport(FQN_TENANT);
                            return JavaTemplate.builder("Tenant.builder()")
                                    .imports(FQN_TENANT)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "arconia-multitenancy-core-0.30"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace());
                        }

                        return nc;
                    }
                }
        );
    }

}
