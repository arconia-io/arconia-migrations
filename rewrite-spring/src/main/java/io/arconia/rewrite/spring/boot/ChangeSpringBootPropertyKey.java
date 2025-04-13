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
 * Renames a property key in Spring Boot configuration files, including both YAML and properties files.
 */
public class ChangeSpringBootPropertyKey extends Recipe {

    @Option(displayName = "Old property key",
            description = "The property key to rename.",
            example = "arconia.dev.profiles.test")
    private final String oldPropertyKey;

    @Option(displayName = "New property key",
            description = "The new name for the property key.",
            example = "arconia.config.profiles.test")
    private final String newPropertyKey;

    @Option(displayName = "Use relaxed binding",
            description = "Whether to match the old property key using Spring Boot relaxed binding rules. Defaults to `true`.",
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
    public ChangeSpringBootPropertyKey(String oldPropertyKey, String newPropertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.oldPropertyKey = oldPropertyKey;
        this.newPropertyKey = newPropertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Change Spring Boot configuration property key";
    }

    @Override
    public String getDescription() {
        return "Change the key of a property in Spring Boot configuration files, including both YAML and properties files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new ChangeSpringBootPropertyKeyVisitor(oldPropertyKey, newPropertyKey, relaxedBinding));
    }

    private static class ChangeSpringBootPropertyKeyVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String oldPropertyKey;
        private final String newPropertyKey;
        @Nullable
        private final Boolean relaxedBinding;

        public ChangeSpringBootPropertyKeyVisitor(String oldPropertyKey, String newPropertyKey, @Nullable Boolean relaxedBinding) {
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
        ChangeSpringBootPropertyKey that = (ChangeSpringBootPropertyKey) o;
        return Objects.equals(oldPropertyKey, that.oldPropertyKey) && Objects.equals(newPropertyKey, that.newPropertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldPropertyKey, newPropertyKey, relaxedBinding);
    }

    @Override
    public String toString() {
        return "ChangePropertyKey{" +
                "oldPropertyKey='" + oldPropertyKey + '\'' +
                ", newPropertyKey='" + newPropertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                '}';
    }

}
