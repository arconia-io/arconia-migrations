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
 * Changes a property value in Spring Boot configuration files, including both YAML and properties files.
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

    @Option(displayName = "Use Regex",
            description = """
                    Default is `false`. If enabled, `oldValue` will be treated as a regular expression,
                    and only matching parts will be replaced. You can use capturing groups in `newValue`.
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
                    A list of glob expressions used to match which files contain Spring Boot configuration properties
                    and therefore will be modified. If no value is provided, default path expressions will be used to
                    match the main YAML and Properties files supported by Spring Boot for configuration.
                    If multiple patterns are supplied, any of the patterns matching will be interpreted as a match.
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
        return "Change value of a Spring Boot configuration property key";
    }

    @Override
    public String getDescription() {
        return "Change the value of a property in Spring Boot configuration files, including both YAML and properties files.";
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
    public boolean equals(Object o) {
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
