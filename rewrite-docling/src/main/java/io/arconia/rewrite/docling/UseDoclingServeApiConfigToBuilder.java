package io.arconia.rewrite.docling;

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
 * Replaces {@code api.toBuilder()} with {@code api.config().toBuilder()} on a {@code DoclingServeApi}.
 * <p>
 * Only invocations declared on the {@code DoclingServeApi} interface are migrated. The {@code toBuilder()}
 * methods of the concrete clients, such as {@code DoclingServeJackson3Client}, are not deprecated
 * and keep the client-specific settings, so they are left untouched.
 */
public class UseDoclingServeApiConfigToBuilder extends Recipe {

    private static final MethodMatcher TO_BUILDER_MATCHER
            = new MethodMatcher("ai.docling.serve.api.DoclingServeApi toBuilder()");

    @Override
    public String getDisplayName() {
        return "Use `DoclingServeApi.config().toBuilder()` instead of `toBuilder()`";
    }

    @Override
    public String getDescription() {
        return "Replace the deprecated `DoclingServeApi.toBuilder()` with `config().toBuilder()`, " +
                "which creates a modified copy of the API through the available `DoclingServeApiProvider`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(TO_BUILDER_MATCHER),
                new ToBuilderVisitor()
        );
    }

    private static class ToBuilderVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

            if (!TO_BUILDER_MATCHER.matches(mi)) {
                return mi;
            }

            Expression select = mi.getSelect();
            if (select == null) {
                // An unqualified call from inside an implementation: the implementation decides how to migrate it.
                return mi;
            }

            return JavaTemplate.builder("#{any(ai.docling.serve.api.DoclingServeApi)}.config().toBuilder()")
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "docling-serve-api-0.7"))
                    .build()
                    .apply(getCursor(), mi.getCoordinates().replace(), select);
        }

    }

}
