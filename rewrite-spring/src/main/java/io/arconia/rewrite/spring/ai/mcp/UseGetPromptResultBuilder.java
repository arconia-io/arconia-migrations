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
 * Replaces {@code new McpSchema.GetPromptResult(description, messages)} with
 * {@code McpSchema.GetPromptResult.builder(messages).description(description).build()},
 * swapping the argument order to match the new builder factory signature.
 */
public class UseGetPromptResultBuilder extends Recipe {

    private static final String FQN_GET_PROMPT_RESULT = "io.modelcontextprotocol.spec.McpSchema$GetPromptResult";

    private static final MethodMatcher CONSTRUCTOR_MATCHER
            = new MethodMatcher(FQN_GET_PROMPT_RESULT + " <constructor>(java.lang.String, java.util.List)");

    @Override
    public String getDisplayName() {
        return "Use `McpSchema.GetPromptResult.builder(...)` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new McpSchema.GetPromptResult(description, messages)` with " +
                "`McpSchema.GetPromptResult.builder(messages).description(description).build()`.";
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
                Expression description = args.get(0);
                Expression messages = args.get(1);
                return JavaTemplate.builder(
                                "McpSchema.GetPromptResult.builder(#{any(java.util.List)})" +
                                ".description(#{any(java.lang.String)}).build()")
                        .imports("io.modelcontextprotocol.spec.McpSchema")
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "mcp-core-2.0"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), messages, description);
            }
        });
    }

}
