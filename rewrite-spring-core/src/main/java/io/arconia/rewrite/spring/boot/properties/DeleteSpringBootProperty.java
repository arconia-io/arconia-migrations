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
 * Deletes a property in Spring Boot configuration files, including YAML and properties files.
 * <p>
 * This recipe is a convenient wrapper around the existing {@link org.openrewrite.yaml.DeleteProperty} and
 * {@link org.openrewrite.properties.DeleteProperty} recipes, allowing you to delete a property
 * in Spring Boot configuration files at once, without needing to specify the file format.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class DeleteSpringBootProperty extends Recipe {

    @Option(displayName = "Property key",
            description = "The key of the property to delete.",
            example = "arconia.modes.name")
    private final String propertyKey;

    @Option(displayName = "Use relaxed binding",
            description = "Whether to match the property key using Spring Boot relaxed binding rules. Defaults to `true`.",
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
    public DeleteSpringBootProperty(String propertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.propertyKey = propertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Delete a Spring Boot configuration property";
    }

    @Override
    public String getDescription() {
        return "Delete a property in Spring Boot configuration files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new DeleteSpringBootPropertyKeyVisitor(propertyKey, relaxedBinding));
    }

    private static class DeleteSpringBootPropertyKeyVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String propertyKey;
        @Nullable
        private final Boolean relaxedBinding;

        public DeleteSpringBootPropertyKeyVisitor(String propertyKey, @Nullable Boolean relaxedBinding) {
            this.propertyKey = propertyKey;
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
            Recipe yamlRecipe = new org.openrewrite.yaml.DeleteProperty(
                    propertyKey, false, !Boolean.FALSE.equals(relaxedBinding), null);
            return yamlRecipe.getVisitor();
        }

        private TreeVisitor<?, ExecutionContext> getPropertiesVisitor() {
            Recipe propertiesRecipe = new org.openrewrite.properties.DeleteProperty(
                    propertyKey, !Boolean.FALSE.equals(relaxedBinding));
            return propertiesRecipe.getVisitor();
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeleteSpringBootProperty that = (DeleteSpringBootProperty) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyKey, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "DeleteSpringBootProperty{" +
                "propertyKey='" + propertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
