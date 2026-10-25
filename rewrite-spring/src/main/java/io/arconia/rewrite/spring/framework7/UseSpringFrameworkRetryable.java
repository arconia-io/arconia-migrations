package io.arconia.rewrite.spring.framework7;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Replaces Spring Retry {@code @Retryable} annotations with the Spring Framework {@code @Retryable} annotation that
 * retries and backs off the same way, as planned by {@link SpringRetryAnnotations}. Annotations without such a
 * counterpart are left unchanged.
 */
public class UseSpringFrameworkRetryable extends Recipe {

    private static final JavaTemplate MINUS_ONE = JavaTemplate.builder("#{any(int)} - 1").build();

    @Override
    public String getDisplayName() {
        return "Use the Spring Framework `@Retryable` annotation instead of the Spring Retry one";
    }

    @Override
    public String getDescription() {
        return "Replace the Spring Retry `@Retryable` annotation with the Spring Framework `@Retryable` annotation, " +
                "carrying over the exception types, the number of attempts, and the backoff, so that methods retry the " +
                "same way. Spring Retry counts the initial invocation in `maxAttempts`, while Spring Framework counts " +
                "only the retries in `maxRetries`. Annotations that use recovery, stateful retry, listeners, SpEL " +
                "expressions, or a random backoff, or that apply to a method with a reactive return type, are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(SpringRetryAnnotations.RETRYABLE_FQN, false), new JavaIsoVisitor<>() {
            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                J.Annotation a = super.visitAnnotation(annotation, ctx);
                if (!TypeUtils.isOfClassType(a.getType(), SpringRetryAnnotations.RETRYABLE_FQN)) {
                    return a;
                }
                SpringRetryAnnotations.Plan plan = SpringRetryAnnotations.plan(a, getCursor());
                if (plan == null) {
                    return a;
                }

                J.Annotation migrated = JavaTemplate.builder(plan.template())
                        .imports(SpringRetryAnnotations.FRAMEWORK_RETRYABLE_FQN)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-context-7.0"))
                        .build()
                        .apply(getCursor(), a.getCoordinates().replace());
                migrated = migrated.withArguments(ListUtils.map(migrated.getArguments(), argument ->
                        argument instanceof J.Assignment assignment ? carryOver(assignment, plan) : argument));
                if (plan.maxRetriesBase() != null) {
                    migrated = (J.Annotation) new MaxRetriesVisitor(plan.maxRetriesBase())
                            .visitNonNull(migrated, ctx, getCursor().getParentOrThrow());
                }

                maybeRemoveImport(SpringRetryAnnotations.RETRYABLE_FQN);
                maybeRemoveImport(SpringRetryAnnotations.BACKOFF_FQN);
                maybeAddImport(SpringRetryAnnotations.FRAMEWORK_RETRYABLE_FQN);
                return migrated;
            }
        });
    }

    // Puts the original exception types in place of the empty arrays of the template.
    private static J.Assignment carryOver(J.Assignment assignment, SpringRetryAnnotations.Plan plan) {
        if (!(assignment.getVariable() instanceof J.Identifier name)) {
            return assignment;
        }
        Expression original = switch (name.getSimpleName()) {
            case "includes" -> plan.includes();
            case "excludes" -> plan.excludes();
            default -> null;
        };
        return original == null ? assignment
                : assignment.withAssignment(original.withPrefix(assignment.getAssignment().getPrefix()));
    }

    // Replaces the maxRetries placeholder with one less than the original maxAttempts expression.
    private static class MaxRetriesVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final Expression maxAttempts;

        MaxRetriesVisitor(Expression maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        @Override
        public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
            J.Assignment a = super.visitAssignment(assignment, ctx);
            if (!(a.getVariable() instanceof J.Identifier name) || !"maxRetries".equals(name.getSimpleName())) {
                return a;
            }
            Expression placeholder = a.getAssignment();
            Expression lessOne = MINUS_ONE.apply(new Cursor(getCursor(), placeholder),
                    placeholder.getCoordinates().replace(), maxAttempts.withPrefix(placeholder.getPrefix()));
            return a.withAssignment(lessOne);
        }

    }

}
