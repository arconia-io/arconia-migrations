package io.arconia.rewrite.openapi.swagger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.marker.GradleDependencyConfiguration;
import org.openrewrite.gradle.marker.GradleProject;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.ResolvedDependency;

/**
 * Finds the source files of a project that declares none of the {@code javax} variants of the Swagger REST artifacts.
 * Meant as the precondition of {@code UpgradeSwaggerCore_3}, which migrates a project only when it is already on the
 * {@code jakarta} namespace, since Swagger Core 3 supports no other.
 * Each project of a multi-project build, identified by its {@link JavaProject} marker, qualifies on its own.
 *
 * <p>Only direct dependencies count. A {@code javax} artifact brought in by a third-party library says nothing about
 * the namespace of the project, only that the library needs a release compatible with Swagger Core 3.
 *
 * <p>In a Gradle build, only the compile and runtime classpaths of the source sets count. Other configurations hold the
 * classpath of a build tool rather than of the project: for example, the {@code swaggerDeps} configuration of the
 * Swagger Gradle plugin resolves {@code swagger-jaxrs2} by default, whatever namespace the project uses.
 */
public class FindProjectsWithoutJavaxSwaggerArtifacts extends ScanningRecipe<FindProjectsWithoutJavaxSwaggerArtifacts.Accumulator> {

    private static final String SWAGGER_GROUP_ID = "io.swagger.core.v3";

    private static final List<String> JAVAX_ARTIFACT_IDS = List.of(
            "swagger-jaxrs2",
            "swagger-jaxrs2-servlet-initializer",
            "swagger-jaxrs2-servlet-initializer-v2");

    @Override
    public String getDisplayName() {
        return "Find projects that declare no `javax` Swagger REST artifacts";
    }

    @Override
    public String getDescription() {
        return "Find the source files of a project that declares none of `swagger-jaxrs2`, " +
                "`swagger-jaxrs2-servlet-initializer`, or `swagger-jaxrs2-servlet-initializer-v2`, the `javax` " +
                "variants of the Swagger REST artifacts. " +
                "Swagger Core 3 supports only the `jakarta` namespace, so a project that declares them does not " +
                "qualify. The artifacts brought in by a third-party library don't count.";
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
                if (tree instanceof SourceFile sourceFile) {
                    JavaProject project = project(sourceFile);
                    if (!acc.javaxProjects.contains(project) && declaresJavaxArtifact(sourceFile)) {
                        acc.javaxProjects.add(project);
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
                if (tree instanceof SourceFile sourceFile && !acc.javaxProjects.contains(project(sourceFile))) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Declares no javax Swagger REST artifacts")));
                }
                return tree;
            }
        };
    }

    private static boolean declaresJavaxArtifact(SourceFile sourceFile) {
        MavenResolutionResult maven = sourceFile.getMarkers().findFirst(MavenResolutionResult.class).orElse(null);
        if (maven != null) {
            for (String artifactId : JAVAX_ARTIFACT_IDS) {
                for (ResolvedDependency dependency : maven.findDependencies(SWAGGER_GROUP_ID, artifactId, null)) {
                    if (dependency.isDirect()) {
                        return true;
                    }
                }
            }
        }
        GradleProject gradle = sourceFile.getMarkers().findFirst(GradleProject.class).orElse(null);
        if (gradle != null) {
            for (GradleDependencyConfiguration configuration : gradle.getConfigurations()) {
                if (!isSourceSetClasspath(configuration.getName())) {
                    continue;
                }
                for (ResolvedDependency dependency : configuration.getDirectResolved()) {
                    if (isJavaxArtifact(dependency)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isJavaxArtifact(ResolvedDependency dependency) {
        return SWAGGER_GROUP_ID.equals(dependency.getGroupId()) && JAVAX_ARTIFACT_IDS.contains(dependency.getArtifactId());
    }

    // The compile and runtime classpaths of the main source set and of any other, such as testCompileClasspath.
    private static boolean isSourceSetClasspath(String configurationName) {
        return configurationName.equals("compileClasspath") || configurationName.endsWith("CompileClasspath")
                || configurationName.equals("runtimeClasspath") || configurationName.endsWith("RuntimeClasspath");
    }

    // Files without a JavaProject marker, such as in tests, count as a single project.
    private static @Nullable JavaProject project(SourceFile sourceFile) {
        return sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null);
    }

    public static class Accumulator {

        // The projects that declare a javax Swagger REST artifact.
        private final Set<@Nullable JavaProject> javaxProjects = new HashSet<>();

    }

}
