package io.arconia.rewrite.spring.modulith2;

import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;

/**
 * Finds the Java source files of a project whose {@code IncompleteEventPublications} usages can be moved to
 * {@code FailedEventPublications} without changing what the application does. Meant as the precondition of
 * {@code MigrateSpringModulithIncompleteEventPublications_2_2}, which migrates a project only when every
 * {@code IncompleteEventPublications} usage in it can be migrated.
 * Each project of a multi-project build, identified by its {@link JavaProject} marker, qualifies on its own.
 *
 * <p>Only the {@code resubmitIncompletePublications(ResubmissionOptions)} overload maps one to one onto
 * {@code FailedEventPublications.resubmit(ResubmissionOptions)}. The overloads taking a {@code Predicate} or a
 * {@code Duration} resubmit every incomplete publication rather than only the failed ones, and a class implementing
 * {@code IncompleteEventPublications} would not implement {@code FailedEventPublications} after a type change, so
 * either rules the project out.
 */
public class FindMigratableIncompleteEventPublications extends ScanningRecipe<FindMigratableIncompleteEventPublications.Accumulator> {

    static final String INCOMPLETE_EVENT_PUBLICATIONS_FQN = "org.springframework.modulith.events.IncompleteEventPublications";

    private static final MethodMatcher RESUBMIT_WITH_FILTER = new MethodMatcher(
            INCOMPLETE_EVENT_PUBLICATIONS_FQN + " resubmitIncompletePublications(java.util.function.Predicate)", true);

    private static final MethodMatcher RESUBMIT_OLDER_THAN = new MethodMatcher(
            INCOMPLETE_EVENT_PUBLICATIONS_FQN + " resubmitIncompletePublicationsOlderThan(java.time.Duration)", true);

    @Override
    public String getDisplayName() {
        return "Find projects whose Spring Modulith `IncompleteEventPublications` usages can be migrated";
    }

    @Override
    public String getDescription() {
        return "Find the Java source files of a project that uses `IncompleteEventPublications` only through " +
                "`resubmitIncompletePublications(ResubmissionOptions)`, which maps one to one onto " +
                "`FailedEventPublications.resubmit(ResubmissionOptions)`. A project that calls the overloads taking a " +
                "`Predicate` or a `Duration`, or implements `IncompleteEventPublications`, does not qualify.";
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
                if (tree instanceof JavaSourceFile sourceFile) {
                    JavaProject project = project(sourceFile);
                    if (!acc.unsupportedProjects.contains(project)) {
                        UsageScanner scanner = new UsageScanner();
                        scanner.visit(sourceFile, ctx);
                        if (scanner.unsupported) {
                            acc.unsupportedProjects.add(project);
                        }
                    }
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
                if (tree instanceof JavaSourceFile sourceFile && !acc.unsupportedProjects.contains(project(sourceFile))) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "IncompleteEventPublications can be migrated to FailedEventPublications")));
                }
                return tree;
            }
        };
    }

    private static class UsageScanner extends JavaIsoVisitor<ExecutionContext> {

        // Whether anything in the scanned source file rules out the migration.
        boolean unsupported;

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            JavaType.FullyQualified type = classDecl.getType();
            if (type != null && !TypeUtils.isOfClassType(type, INCOMPLETE_EVENT_PUBLICATIONS_FQN)
                    && TypeUtils.isAssignableTo(INCOMPLETE_EVENT_PUBLICATIONS_FQN, type)) {
                unsupported = true;
            }
            return super.visitClassDeclaration(classDecl, ctx);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            if (RESUBMIT_WITH_FILTER.matches(method) || RESUBMIT_OLDER_THAN.matches(method)) {
                unsupported = true;
            }
            return super.visitMethodInvocation(method, ctx);
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ExecutionContext ctx) {
            if (RESUBMIT_WITH_FILTER.matches(memberRef) || RESUBMIT_OLDER_THAN.matches(memberRef)) {
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
