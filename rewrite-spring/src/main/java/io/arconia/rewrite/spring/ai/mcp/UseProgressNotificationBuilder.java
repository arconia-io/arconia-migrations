package io.arconia.rewrite.spring.ai.mcp;

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
 * Replaces {@code new McpSchema.ProgressNotification(token, progress, total, message)} with
 * {@code McpSchema.ProgressNotification.builder(token, progress).total(total).message(message).build()}.
 */
public class UseProgressNotificationBuilder extends Recipe {

    private static final String FQN_PROGRESS_NOTIFICATION = "io.modelcontextprotocol.spec.McpSchema$ProgressNotification";

    private static final MethodMatcher CONSTRUCTOR_MATCHER
            = new MethodMatcher(FQN_PROGRESS_NOTIFICATION + " <constructor>(java.lang.Object, double, java.lang.Double, java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `McpSchema.ProgressNotification.builder(...)` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new McpSchema.ProgressNotification(token, progress, total, message)` with " +
                "`McpSchema.ProgressNotification.builder(token, progress).total(total).message(message).build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(CONSTRUCTOR_MATCHER), new JavaVisitor<>() {
            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);
                if (!CONSTRUCTOR_MATCHER.matches(nc)) {
                    return nc;
                }
                List<Expression> args = nc.getArguments();
                return JavaTemplate.builder(
                                "McpSchema.ProgressNotification.builder(#{any(java.lang.Object)}, #{any(double)})" +
                                ".total(#{any(java.lang.Double)}).message(#{any(java.lang.String)}).build()")
                        .imports("io.modelcontextprotocol.spec.McpSchema")
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "mcp-core-2.0"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(),
                                args.get(0), args.get(1), args.get(2), args.get(3));
            }
        });
    }

}
