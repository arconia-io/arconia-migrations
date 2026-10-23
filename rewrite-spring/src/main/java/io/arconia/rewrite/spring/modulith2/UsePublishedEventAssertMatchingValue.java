package io.arconia.rewrite.spring.modulith2;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Renames calls to the Spring Modulith {@code PublishedEventAssert.matching(Function, Object)} overload,
 * deprecated in Spring Modulith 2.1 and removed in 2.2, to {@code matchingValue(Function, Object)}.
 *
 * <p>A method pattern cannot single out that overload, since the parameter types of a call are those of the
 * arguments, so a call qualifies only when its second argument is not a {@code Predicate}, which would make
 * it a call to the {@code matching(Function, Predicate)} overload instead.
 */
public class UsePublishedEventAssertMatchingValue extends Recipe {

    private static final MethodMatcher MATCHING = new MethodMatcher(
            "org.springframework.modulith.test.PublishedEventsAssert$PublishedEventAssert matching(java.util.function.Function, *)");

    private static final String PREDICATE_FQN = "java.util.function.Predicate";

    private static final String MATCHING_VALUE = "matchingValue";

    @Override
    public String getDisplayName() {
        return "Use `matchingValue` to match a Spring Modulith published event against a value";
    }

    @Override
    public String getDescription() {
        return "Replace `PublishedEventAssert.matching(Function, Object)`, deprecated in Spring Modulith 2.1 and " +
                "removed in 2.2, with `matchingValue(Function, Object)`. Calls passing a `Predicate` keep using `matching`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(MATCHING), new JavaIsoVisitor<>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                JavaType.Method methodType = m.getMethodType();
                if (methodType == null || !MATCHING.matches(m)
                        || TypeUtils.isAssignableTo(PREDICATE_FQN, methodType.getParameterTypes().get(1))) {
                    return m;
                }
                JavaType.Method renamedType = methodType.withName(MATCHING_VALUE);
                return m.withName(m.getName().withSimpleName(MATCHING_VALUE).withType(renamedType))
                        .withMethodType(renamedType);
            }
        });
    }

}
