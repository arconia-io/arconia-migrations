package io.arconia.rewrite.framework.docling;

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
 * Replaces {@code new HealthCheckResponse(status)} with {@code HealthCheckResponse.builder().status(status).build()}.
 */
public class UseHealthCheckResponseBuilder extends Recipe {

    private static final String FQN_HEALTH_CHECK_RESPONSE = "ai.docling.api.serve.health.HealthCheckResponse";

    private static final MethodMatcher HEALTH_CHECK_RESPONSE_CONSTRUCTOR_MATCHER
            = new MethodMatcher("ai.docling.api.serve.health.HealthCheckResponse <constructor>(*)");

    @Override
    public String getDisplayName() {
        return "Use `HealthCheckResponse.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new HealthCheckResponse(STATUS)` with `HealthCheckResponse.builder().status(status).build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(HEALTH_CHECK_RESPONSE_CONSTRUCTOR_MATCHER),
                new HealthCheckResponseVisitor()
        );
    }

    private static class HealthCheckResponseVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            if (!HEALTH_CHECK_RESPONSE_CONSTRUCTOR_MATCHER.matches(nc)) {
                return nc;
            }

            Expression arg = nc.getArguments().getFirst();

            maybeAddImport(FQN_HEALTH_CHECK_RESPONSE);

            return JavaTemplate.builder("HealthCheckResponse.builder().status(#{any(String)}).build()")
                    .imports(FQN_HEALTH_CHECK_RESPONSE)
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,"docling-serve-api"))
                    .build()
                    .apply(getCursor(), nc.getCoordinates().replace(), arg);
        }

    }

}
