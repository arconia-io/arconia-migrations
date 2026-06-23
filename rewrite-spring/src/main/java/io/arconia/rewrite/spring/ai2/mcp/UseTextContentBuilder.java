package io.arconia.rewrite.spring.ai2.mcp;

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
 * Replaces {@code new McpSchema.TextContent(text)} with {@code McpSchema.TextContent.builder(text).build()}.
 */
public class UseTextContentBuilder extends Recipe {

    private static final String FQN_TEXT_CONTENT = "io.modelcontextprotocol.spec.McpSchema$TextContent";

    private static final MethodMatcher CONSTRUCTOR_MATCHER
            = new MethodMatcher(FQN_TEXT_CONTENT + " <constructor>(java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `McpSchema.TextContent.builder(...)` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new McpSchema.TextContent(text)` with `McpSchema.TextContent.builder(text).build()`.";
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
                return JavaTemplate.builder("McpSchema.TextContent.builder(#{any(java.lang.String)}).build()")
                        .imports("io.modelcontextprotocol.spec.McpSchema")
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "mcp-core-2.0"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), arg);
            }
        });
    }

}
