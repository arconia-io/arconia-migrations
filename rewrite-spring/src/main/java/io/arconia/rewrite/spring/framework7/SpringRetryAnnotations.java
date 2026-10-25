package io.arconia.rewrite.spring.framework7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Maps a Spring Retry {@code @Retryable} annotation onto the Spring Framework {@code @Retryable} annotation with the
 * same retry and backoff behavior, or rejects it when no such mapping exists.
 *
 * <p>Spring Retry counts the initial invocation in {@code maxAttempts} and defaults it to 3, while Spring Framework
 * counts only the retries in {@code maxRetries} and defaults it to 3, so the mapping always sets {@code maxRetries}.
 * A {@code @Backoff} with a positive multiplier is exponential and capped at its {@code maxDelay}, or at 30 seconds
 * when that is not greater than the delay; without a multiplier it is a fixed delay, or a uniform random delay
 * between the delay and a greater {@code maxDelay}, which Spring Framework has no counterpart for.
 *
 * <p>Recovery, stateful retry, listeners, custom interceptors, SpEL expressions, random backoff, and methods with a
 * reactive return type have no one to one counterpart either, so an annotation using any of them is rejected.
 */
final class SpringRetryAnnotations {

    static final String SPRING_RETRY_PACKAGE = "org.springframework.retry.";

    static final String RETRYABLE_FQN = "org.springframework.retry.annotation.Retryable";

    static final String BACKOFF_FQN = "org.springframework.retry.annotation.Backoff";

    static final String ENABLE_RETRY_FQN = "org.springframework.retry.annotation.EnableRetry";

    static final String FRAMEWORK_RETRYABLE_FQN = "org.springframework.resilience.annotation.Retryable";

    private static final String PUBLISHER_FQN = "org.reactivestreams.Publisher";

    // Spring Retry caps an exponential backoff at 30 seconds unless a greater maxDelay is set.
    private static final long DEFAULT_MAX_INTERVAL = 30000L;

    private static final long DEFAULT_DELAY = 1000L;

    private static final Set<String> INCLUDES = Set.of("value", "retryFor", "include");

    private static final Set<String> EXCLUDES = Set.of("noRetryFor", "exclude");

    private SpringRetryAnnotations() {
    }

    /**
     * Plans the migration of a Spring Retry {@code @Retryable} annotation, with the cursor pointing at it.
     * Returns {@code null} when the annotation has no Spring Framework counterpart with the same behavior.
     */
    static @Nullable Plan plan(J.Annotation retryable, Cursor cursor) {
        if (hasReactiveTarget(cursor)) {
            return null;
        }
        Map<String, Expression> attributes = attributes(retryable);
        if (attributes == null) {
            return null;
        }

        Expression includes = null;
        Expression excludes = null;
        Expression maxAttempts = null;
        J.Annotation backoff = null;
        for (Map.Entry<String, Expression> attribute : attributes.entrySet()) {
            String name = attribute.getKey();
            Expression value = attribute.getValue();
            if (INCLUDES.contains(name)) {
                if (includes != null) {
                    return null;
                }
                includes = value;
            }
            else if (EXCLUDES.contains(name)) {
                if (excludes != null) {
                    return null;
                }
                excludes = value;
            }
            else if ("maxAttempts".equals(name)) {
                maxAttempts = value;
            }
            else if ("backoff".equals(name) && value instanceof J.Annotation annotation
                    && TypeUtils.isOfClassType(annotation.getType(), BACKOFF_FQN)) {
                backoff = annotation;
            }
            else if ("stateful".equals(name) && isLiteral(value, Boolean.FALSE)) {
                // Stateless retry is the default in both.
            }
            else if (!"label".equals(name)) {
                // The label only names the retry for listeners and statistics, which have no counterpart.
                return null;
            }
        }

        String maxRetries;
        Expression maxRetriesBase = null;
        if (maxAttempts == null) {
            maxRetries = "2";
        }
        else if (maxAttempts instanceof J.Literal literal && literal.getValue() instanceof Number number) {
            if (number.intValue() < 1) {
                return null;
            }
            maxRetries = Long.toString(number.longValue() - 1);
        }
        else {
            maxRetries = "0";
            maxRetriesBase = maxAttempts;
        }

        List<String> backoffArguments = backoff == null ? List.of() : backoffArguments(backoff);
        if (backoffArguments == null) {
            return null;
        }
        return new Plan(includes, excludes, maxRetries, maxRetriesBase, backoffArguments);
    }

    private static @Nullable List<String> backoffArguments(J.Annotation backoff) {
        Map<String, Expression> attributes = attributes(backoff);
        if (attributes == null) {
            return null;
        }
        Number delay = DEFAULT_DELAY;
        Number maxDelay = 0L;
        Number multiplier = 0.0;
        String multiplierSource = null;
        for (Map.Entry<String, Expression> attribute : attributes.entrySet()) {
            Expression value = attribute.getValue();
            if ("random".equals(attribute.getKey()) && isLiteral(value, Boolean.FALSE)) {
                continue;
            }
            if (!(value instanceof J.Literal literal) || !(literal.getValue() instanceof Number number)) {
                return null;
            }
            switch (attribute.getKey()) {
                case "value", "delay" -> delay = number;
                case "maxDelay" -> maxDelay = number;
                case "multiplier" -> {
                    multiplier = number;
                    multiplierSource = literal.getValueSource();
                }
                default -> {
                    return null;
                }
            }
        }

        if (delay.longValue() < 0) {
            return null;
        }
        List<String> arguments = new ArrayList<>();
        if (delay.longValue() != DEFAULT_DELAY) {
            arguments.add("delay = " + delay.longValue());
        }
        if (multiplier.doubleValue() > 0) {
            // Spring Framework requires a multiplier of at least 1, and a smaller one shrinks the delay in Spring Retry.
            if (multiplier.doubleValue() < 1) {
                return null;
            }
            if (multiplier.doubleValue() != 1) {
                arguments.add("multiplier = " + multiplierSource);
            }
            long cap = maxDelay.longValue() > delay.longValue() ? maxDelay.longValue() : DEFAULT_MAX_INTERVAL;
            arguments.add("maxDelay = " + cap);
        }
        else if (maxDelay.longValue() > delay.longValue()) {
            return null;
        }
        return arguments;
    }

    // The attributes of an annotation by name, or null when it has an argument that is not a named attribute or a single value.
    private static @Nullable Map<String, Expression> attributes(J.Annotation annotation) {
        Map<String, Expression> attributes = new HashMap<>();
        if (annotation.getArguments() == null) {
            return attributes;
        }
        for (Expression argument : annotation.getArguments()) {
            if (argument instanceof J.Empty) {
                continue;
            }
            if (argument instanceof J.Assignment assignment && assignment.getVariable() instanceof J.Identifier name) {
                attributes.put(name.getSimpleName(), assignment.getAssignment());
            }
            else if (annotation.getArguments().size() == 1) {
                attributes.put("value", argument);
            }
            else {
                return null;
            }
        }
        return attributes;
    }

    private static boolean isLiteral(Expression expression, Object value) {
        return expression instanceof J.Literal literal && value.equals(literal.getValue());
    }

    // Spring Framework retries a reactive method by resubscribing, while Spring Retry only retries the call itself.
    private static boolean hasReactiveTarget(Cursor cursor) {
        Object annotated = cursor.getParentTreeCursor().getValue();
        if (annotated instanceof J.MethodDeclaration method) {
            return isReactive(method);
        }
        if (annotated instanceof J.ClassDeclaration classDecl) {
            for (Statement statement : classDecl.getBody().getStatements()) {
                if (statement instanceof J.MethodDeclaration method && isReactive(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isReactive(J.MethodDeclaration method) {
        JavaType.Method methodType = method.getMethodType();
        return methodType != null && TypeUtils.isAssignableTo(PUBLISHER_FQN, methodType.getReturnType());
    }

    /**
     * The Spring Framework {@code @Retryable} attributes equivalent to a Spring Retry {@code @Retryable} annotation.
     * The exception types are the original expressions. When {@code maxRetriesBase} is set, {@code maxRetries} is a
     * placeholder to replace with one less than that expression.
     */
    record Plan(@Nullable Expression includes, @Nullable Expression excludes, String maxRetries,
            @Nullable Expression maxRetriesBase, List<String> backoffArguments) {

        String template() {
            List<String> arguments = new ArrayList<>();
            if (includes != null) {
                arguments.add("includes = {}");
            }
            if (excludes != null) {
                arguments.add("excludes = {}");
            }
            arguments.add("maxRetries = " + maxRetries);
            arguments.addAll(backoffArguments);
            return "@Retryable(" + String.join(", ", arguments) + ")";
        }

    }

}
