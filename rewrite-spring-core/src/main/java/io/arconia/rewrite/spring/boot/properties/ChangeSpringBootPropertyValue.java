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
 * Changes a property value in Spring Boot configuration files, including YAML and properties files.
 * <p>
 * This recipe is a convenient wrapper around the existing {@link org.openrewrite.yaml.ChangePropertyValue} and
 * {@link org.openrewrite.properties.ChangePropertyValue} recipes, allowing you to change the value of a property
 * in Spring Boot configuration files at once, without needing to specify the file format.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class ChangeSpringBootPropertyValue extends Recipe {

    @Option(displayName = "Property key",
            description = "The property key whose value must change.",
            example = "arconia.dev.services.*.reusable")
    private final String propertyKey;

    @Option(displayName = "Old property value",
            description = "The property value to change.",
            required = false,
            example = "true")
    @Nullable
    private final String oldPropertyValue;

    @Option(displayName = "New property value",
            description = "The new value for the property key.",
            example = "always")
    private final String newPropertyValue;

    @Option(displayName = "Use regular expressions to match the old value",
            description = """
                    Treats the old property value as a regular expression for matching.
                    When enabled, only matching portions are replaced, and capture groups
                    can be referenced in the new value. Defaults to `false`.
                    """,
            required = false)
    @Nullable
    private final Boolean useRegex;

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
    public ChangeSpringBootPropertyValue(String propertyKey, @Nullable String oldPropertyValue, String newPropertyValue, @Nullable Boolean useRegex, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.propertyKey = propertyKey;
        this.oldPropertyValue = oldPropertyValue;
        this.newPropertyValue = newPropertyValue;
        this.useRegex = useRegex;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Change the value of a Spring Boot configuration property key";
    }

    @Override
    public String getDescription() {
        return "Change the value of a property in Spring Boot configuration files.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new ChangeSpringBootPropertyValueVisitor(propertyKey, oldPropertyValue, newPropertyValue, useRegex, relaxedBinding));
    }

    private static class ChangeSpringBootPropertyValueVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String propertyKey;
        @Nullable
        private final String oldPropertyValue;
        private final String newPropertyValue;
        @Nullable
        private final Boolean useRegex;
        @Nullable
        private final Boolean relaxedBinding;

        public ChangeSpringBootPropertyValueVisitor(String propertyKey, @Nullable String oldPropertyValue, String newPropertyValue, @Nullable Boolean useRegex, @Nullable Boolean relaxedBinding) {
            this.propertyKey = propertyKey;
            this.oldPropertyValue = oldPropertyValue;
            this.newPropertyValue = newPropertyValue;
            this.useRegex = useRegex;
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
            Recipe yamlRecipe = new org.openrewrite.yaml.ChangePropertyValue(
                    propertyKey, newPropertyValue, oldPropertyValue, useRegex, !Boolean.FALSE.equals(relaxedBinding), null);
            return yamlRecipe.getVisitor();
        }

        private TreeVisitor<?, ExecutionContext> getPropertiesVisitor() {
            Recipe propertiesRecipe = new org.openrewrite.properties.ChangePropertyValue(
                    propertyKey, newPropertyValue, oldPropertyValue, useRegex, !Boolean.FALSE.equals(relaxedBinding));
            return propertiesRecipe.getVisitor();
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChangeSpringBootPropertyValue that = (ChangeSpringBootPropertyValue) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(oldPropertyValue, that.oldPropertyValue) && Objects.equals(newPropertyValue, that.newPropertyValue) && Objects.equals(useRegex, that.useRegex) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyKey, oldPropertyValue, newPropertyValue, useRegex, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "ChangeSpringBootPropertyValue{" +
                "propertyKey='" + propertyKey + '\'' +
                ", oldPropertyValue='" + oldPropertyValue + '\'' +
                ", newPropertyValue='" + newPropertyValue + '\'' +
                ", useRegex=" + useRegex +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
