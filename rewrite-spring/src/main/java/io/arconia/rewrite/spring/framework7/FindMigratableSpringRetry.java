package io.arconia.rewrite.spring.framework7;

import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;

/**
 * Finds the source files of a project whose Spring Retry usage can be migrated to the retry support built into
 * Spring Framework without changing what the application does. Meant as the precondition of
 * {@code MigrateSpringRetryToSpringFramework}, which migrates a project only when every Spring Retry usage in it
 * can be migrated.
 * Each project of a multi-project build, identified by its {@link JavaProject} marker, qualifies on its own.
 *
 * <p>A project qualifies when its Java code uses Spring Retry only through {@code @EnableRetry} and
 * {@code @Retryable} annotations, optionally with a {@code @Backoff}, that {@link SpringRetryAnnotations} maps one
 * to one onto Spring Framework. Any other Spring Retry type, such as {@code RetryTemplate}, {@code @Recover},
 * {@code @CircuitBreaker}, or {@code RetrySynchronizationManager}, an annotation without a counterpart, or Spring
 * Retry used from a source file other than Java rules the project out. A project that does not use Spring Retry
 * in code qualifies, so that an unused Spring Retry dependency can be removed.
 */
public class FindMigratableSpringRetry extends ScanningRecipe<FindMigratableSpringRetry.Accumulator> {

    @Override
    public String getDisplayName() {
        return "Find projects whose Spring Retry usage can be migrated to Spring Framework";
    }

    @Override
    public String getDescription() {
        return "Find the source files of a project that uses Spring Retry only through `@EnableRetry` and `@Retryable` " +
                "annotations that map one to one onto the retry support built into Spring Framework. A project that " +
                "uses another Spring Retry API, recovery, stateful retry, listeners, SpEL expressions, a random " +
                "backoff, or retries a method with a reactive return type does not qualify.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (!(tree instanceof JavaSourceFile sourceFile)) {
                    return tree;
                }
                JavaProject project = project(sourceFile);
                if (acc.unsupportedProjects.contains(project)) {
                    return tree;
                }
                if (sourceFile instanceof J.CompilationUnit compilationUnit) {
                    UsageScanner scanner = new UsageScanner();
                    scanner.visit(compilationUnit, ctx);
                    if (scanner.unsupported) {
                        acc.unsupportedProjects.add(project);
                    }
                }
                else if (sourceFile.getTypesInUse().getTypesInUse().stream().anyMatch(FindMigratableSpringRetry::isSpringRetryType)) {
                    acc.unsupportedProjects.add(project);
                }
                return tree;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile sourceFile && !acc.unsupportedProjects.contains(project(sourceFile))) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Spring Retry can be migrated to Spring Framework")));
                }
                return tree;
            }
        };
    }

    private static boolean isSpringRetryType(@Nullable JavaType type) {
        JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type);
        return fullyQualified != null && fullyQualified.getFullyQualifiedName().startsWith(SpringRetryAnnotations.SPRING_RETRY_PACKAGE);
    }

    private static class UsageScanner extends JavaIsoVisitor<ExecutionContext> {

        // Whether anything in the scanned source file rules out the migration.
        boolean unsupported;

        @Override
        public J.Import visitImport(J.Import anImport, ExecutionContext ctx) {
            return anImport;
        }

        @Override
        public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
            if (TypeUtils.isOfClassType(annotation.getType(), SpringRetryAnnotations.ENABLE_RETRY_FQN)) {
                // Its attributes match those of @EnableResilientMethods.
                return annotation;
            }
            if (TypeUtils.isOfClassType(annotation.getType(), SpringRetryAnnotations.RETRYABLE_FQN)) {
                if (SpringRetryAnnotations.plan(annotation, getCursor()) == null) {
                    unsupported = true;
                }
                // A supported annotation, including its @Backoff, is migrated as a whole.
                return annotation;
            }
            return super.visitAnnotation(annotation, ctx);
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
            if (isSpringRetryType(identifier.getType())) {
                unsupported = true;
            }
            return super.visitIdentifier(identifier, ctx);
        }

        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
            if (isSpringRetryType(fieldAccess.getType())) {
                unsupported = true;
            }
            return super.visitFieldAccess(fieldAccess, ctx);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            JavaType.Method methodType = method.getMethodType();
            if (methodType != null && isSpringRetryType(methodType.getDeclaringType())) {
                unsupported = true;
            }
            return super.visitMethodInvocation(method, ctx);
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ExecutionContext ctx) {
            JavaType.Method methodType = memberRef.getMethodType();
            if (methodType != null && isSpringRetryType(methodType.getDeclaringType())) {
                unsupported = true;
            }
            return super.visitMemberReference(memberRef, ctx);
        }

    }

    // Files without a JavaProject marker, such as in tests, count as a single project.
    private static @Nullable JavaProject project(SourceFile sourceFile) {
        return sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null);
    }

    public static class Accumulator {

        // The projects in which something rules out the migration.
        private final Set<@Nullable JavaProject> unsupportedProjects = new HashSet<>();

    }

}
