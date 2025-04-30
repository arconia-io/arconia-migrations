package io.arconia.rewrite.spring.boot;

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
 * Deletes a property in Spring Boot configuration files, including both YAML and properties files.
 */
public class DeleteSpringBootPropertyKey extends Recipe {

    @Option(displayName = "Property key",
            description = "Key for the property to delete.",
            example = "arconia.modes.name")
    private final String propertyKey;

    @Option(displayName = "Use relaxed binding",
            description = "Whether to match the property key using Spring Boot relaxed binding rules. Defaults to `true`.",
            required = false)
    @Nullable
    private final Boolean relaxedBinding;

    @Option(displayName = "Configuration files path matchers",
            description = """
                    A list of glob expressions used to match which files contain Spring Boot configuration properties
                    and therefore will be modified. If no value is provided, default path expressions will be used to
                    match the main YAML and Properties files supported by Spring Boot for configuration.
                    If multiple patterns are supplied, any of the patterns matching will be interpreted as a match.
                    """,
            required = false)
    @Nullable
    private final List<String> pathExpressions;

    @JsonCreator
    public DeleteSpringBootPropertyKey(String propertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.propertyKey = propertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Delete Spring Boot configuration property key";
    }

    @Override
    public String getDescription() {
        return "Delete a property in Spring Boot configuration files, including both YAML and properties files.";
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeleteSpringBootPropertyKey that = (DeleteSpringBootPropertyKey) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyKey, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "DeleteSpringBootPropertyKey{" +
                "propertyKey='" + propertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
