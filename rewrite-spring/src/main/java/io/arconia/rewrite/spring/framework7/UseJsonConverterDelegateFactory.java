package io.arconia.rewrite.spring.framework7;

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
 * Replaces the {@code HttpMessageContentConverter.of(..)} factory methods, removed in Spring Framework 7.1,
 * with {@code JsonConverterDelegate.of(Iterable)}. Since {@code JsonConverterDelegate} has no varargs
 * variant, converters passed individually are collected with {@code List.of(..)}.
 */
public class UseJsonConverterDelegateFactory extends Recipe {

    private static final String HTTP_MESSAGE_CONTENT_CONVERTER_FQN = "org.springframework.test.http.HttpMessageContentConverter";

    private static final String JSON_CONVERTER_DELEGATE_FQN = "org.springframework.test.json.JsonConverterDelegate";

    private static final MethodMatcher FACTORY = new MethodMatcher(HTTP_MESSAGE_CONTENT_CONVERTER_FQN + " of(..)");

    private static final MethodMatcher VARARGS_FACTORY = new MethodMatcher(HTTP_MESSAGE_CONTENT_CONVERTER_FQN +
            " of(org.springframework.http.converter.HttpMessageConverter[])");

    @Override
    public String getDisplayName() {
        return "Use the `JsonConverterDelegate` factory method";
    }

    @Override
    public String getDescription() {
        return "Replace the `HttpMessageContentConverter.of(..)` factory methods, removed in Spring Framework 7.1, with " +
                "`JsonConverterDelegate.of(Iterable)`, collecting converters passed individually with `List.of(..)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(FACTORY), new JavaVisitor<>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                if (!FACTORY.matches(m)) {
                    return m;
                }

                List<Expression> arguments = m.getArguments().stream()
                        .filter(argument -> !(argument instanceof J.Empty))
                        .toList();
                String converters = String.join(", ", arguments.stream().map(argument -> "#{any()}").toList());
                String code = VARARGS_FACTORY.matches(m)
                        ? "JsonConverterDelegate.of(List.of(" + converters + "))"
                        : "JsonConverterDelegate.of(" + converters + ")";

                maybeAddImport(JSON_CONVERTER_DELEGATE_FQN);
                if (VARARGS_FACTORY.matches(m)) {
                    maybeAddImport("java.util.List");
                }
                maybeRemoveImport(HTTP_MESSAGE_CONTENT_CONVERTER_FQN);
                return JavaTemplate.builder(code)
                        .imports(JSON_CONVERTER_DELEGATE_FQN, "java.util.List")
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-test-7.0", "spring-web-7.0", "spring-core-7.0"))
                        .build()
                        .apply(getCursor(), m.getCoordinates().replace(), arguments.toArray());
            }
        });
    }

}
