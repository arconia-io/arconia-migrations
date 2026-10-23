package io.arconia.rewrite.spring.modulith2;

import java.util.HashMap;
import java.util.Map;

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
 * {@code MigrateSpringModulithIncompleteEventPublications_2_2}, which must apply to the whole project or not at all.
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
                    ProjectUsage usage = acc.usage(sourceFile);
                    if (!usage.unsupported) {
                        new UsageScanner().visit(sourceFile, usage);
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
                if (tree instanceof JavaSourceFile sourceFile && !acc.usage(sourceFile).unsupported) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "IncompleteEventPublications can be migrated to FailedEventPublications")));
                }
                return tree;
            }
        };
    }

    private static class UsageScanner extends JavaIsoVisitor<ProjectUsage> {

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ProjectUsage usage) {
            JavaType.FullyQualified type = classDecl.getType();
            if (type != null && !TypeUtils.isOfClassType(type, INCOMPLETE_EVENT_PUBLICATIONS_FQN)
                    && TypeUtils.isAssignableTo(INCOMPLETE_EVENT_PUBLICATIONS_FQN, type)) {
                usage.unsupported = true;
            }
            return super.visitClassDeclaration(classDecl, usage);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ProjectUsage usage) {
            if (RESUBMIT_WITH_FILTER.matches(method) || RESUBMIT_OLDER_THAN.matches(method)) {
                usage.unsupported = true;
            }
            return super.visitMethodInvocation(method, usage);
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ProjectUsage usage) {
            if (RESUBMIT_WITH_FILTER.matches(memberRef) || RESUBMIT_OLDER_THAN.matches(memberRef)) {
                usage.unsupported = true;
            }
            return super.visitMemberReference(memberRef, usage);
        }

    }

    public static class Accumulator {

        // IncompleteEventPublications usage of each project, keyed by the project the source files belong to.
        private final Map<@Nullable JavaProject, ProjectUsage> projects = new HashMap<>();

        ProjectUsage usage(SourceFile sourceFile) {
            return projects.computeIfAbsent(sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null),
                    project -> new ProjectUsage());
        }

    }

    static class ProjectUsage {

        // Whether anything in the project rules out the migration.
        boolean unsupported;

    }

}
