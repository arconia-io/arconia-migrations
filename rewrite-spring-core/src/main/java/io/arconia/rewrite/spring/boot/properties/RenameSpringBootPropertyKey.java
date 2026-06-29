package io.arconia.rewrite.spring.boot.properties;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Renames a property key in Spring Boot configuration files, including YAML and properties files.
 * <p>
 * This recipe is a convenient wrapper around the existing {@link org.openrewrite.yaml.ChangePropertyKey} and
 * {@link org.openrewrite.properties.ChangePropertyKey} recipes, allowing you to rename the key of a property
 * in Spring Boot configuration files at once, without needing to specify the file format.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class RenameSpringBootPropertyKey extends Recipe {

    @Option(displayName = "Old property key",
            description = "The property key to rename.",
            example = "arconia.config.profiles.test")
    private final String oldPropertyKey;

    @Option(displayName = "New property key",
            description = "The new name for the property key.",
            example = "arconia.test.profiles")
    private final String newPropertyKey;

    @Option(displayName = "Use relaxed binding",
            description = "Whether to match the old property key using Spring Boot relaxed binding rules. Defaults to `true`.",
            required = false)
    @Nullable
    private final Boolean relaxedBinding;

    @Option(displayName = "Configuration files path matchers",
            description = """
                    Glob expressions to match Spring Boot configuration files to modify.
                    Defaults to standard `application.yml`, `application.yaml`, and `application.properties` files (including profile-specific variants).
                    Multiple patterns are OR-ed together.
                    """,
            required = false)
    @Nullable
    private final List<String> pathExpressions;

    @JsonCreator
    public RenameSpringBootPropertyKey(String oldPropertyKey, String newPropertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.oldPropertyKey = oldPropertyKey;
        this.newPropertyKey = newPropertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Rename Spring Boot configuration property key";
    }

    @Override
    public String getDescription() {
        return "Rename the key of a property in Spring Boot configuration files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new RenameSpringBootPropertyKeyVisitor(oldPropertyKey, newPropertyKey, relaxedBinding));
    }

    private static class RenameSpringBootPropertyKeyVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String oldPropertyKey;
        private final String newPropertyKey;
        @Nullable
        private final Boolean relaxedBinding;

        public RenameSpringBootPropertyKeyVisitor(String oldPropertyKey, String newPropertyKey, @Nullable Boolean relaxedBinding) {
            this.oldPropertyKey = oldPropertyKey;
            this.newPropertyKey = newPropertyKey;
            this.relaxedBinding = relaxedBinding;
        }

        @Override
        public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
            if (tree == null) {
                return null;
            }

            return switch (tree) {
                case Yaml.Documents yamlTree -> getYamlVisitor().visit(yamlTree, ctx);
                case Properties.File propertiesTree -> getPropertiesVisitor().visit(propertiesTree, ctx);
                default -> tree;
            };
        }

        private TreeVisitor<?, ExecutionContext> getYamlVisitor() {
            Recipe yamlRecipe = new org.openrewrite.yaml.ChangePropertyKey(
                    oldPropertyKey, newPropertyKey, !Boolean.FALSE.equals(relaxedBinding), List.of(), null);
            return yamlRecipe.getVisitor();
        }

        private TreeVisitor<?, ExecutionContext> getPropertiesVisitor() {
            Recipe propertiesRecipe = new org.openrewrite.properties.ChangePropertyKey(
                    oldPropertyKey, newPropertyKey, !Boolean.FALSE.equals(relaxedBinding), false);
            return propertiesRecipe.getVisitor();
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RenameSpringBootPropertyKey that = (RenameSpringBootPropertyKey) o;
        return Objects.equals(oldPropertyKey, that.oldPropertyKey) && Objects.equals(newPropertyKey, that.newPropertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldPropertyKey, newPropertyKey, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "RenameSpringBootPropertyKey{" +
                "oldPropertyKey='" + oldPropertyKey + '\'' +
                ", newPropertyKey='" + newPropertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
