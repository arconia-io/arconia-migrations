package io.arconia.rewrite.spring.ai.mcp;

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
 * Replaces {@code new McpSchema.ReadResourceResult(contents)} with
 * {@code McpSchema.ReadResourceResult.builder(contents).build()}.
 */
public class UseReadResourceResultBuilder extends Recipe {

    private static final String FQN_READ_RESOURCE_RESULT = "io.modelcontextprotocol.spec.McpSchema$ReadResourceResult";

    private static final MethodMatcher CONSTRUCTOR_MATCHER
            = new MethodMatcher(FQN_READ_RESOURCE_RESULT + " <constructor>(java.util.List)");

    @Override
    public String getDisplayName() {
        return "Use `McpSchema.ReadResourceResult.builder(...)` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new McpSchema.ReadResourceResult(contents)` with " +
                "`McpSchema.ReadResourceResult.builder(contents).build()`.";
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
                Expression arg = nc.getArguments().getFirst();
                return JavaTemplate.builder("McpSchema.ReadResourceResult.builder(#{any(java.util.List)}).build()")
                        .imports("io.modelcontextprotocol.spec.McpSchema")
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "mcp-core-2.0"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), arg);
            }
        });
    }

}
