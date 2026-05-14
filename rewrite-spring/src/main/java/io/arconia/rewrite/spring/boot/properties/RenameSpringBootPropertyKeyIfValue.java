package io.arconia.rewrite.spring.boot.properties;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Renames a property key in Spring Boot configuration files only when its current value matches a given value.
 * <p>
 * This is useful when the same property key is used for multiple purposes and only a value-specific subset
 * should be migrated to a new key name.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class RenameSpringBootPropertyKeyIfValue extends Recipe {

    @Option(displayName = "Old property key",
            description = "The property key to rename.",
            example = "arconia.observations.conventions.type")
    private final String oldPropertyKey;

    @Option(displayName = "Required property value",
            description = "The property value that must be present for the key to be renamed.",
            example = "langsmith")
    private final String propertyValue;

    @Option(displayName = "New property key",
            description = "The new name for the property key.",
            example = "arconia.observations.conventions.opentelemetry.ai.flavor")
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
    public RenameSpringBootPropertyKeyIfValue(String oldPropertyKey, String propertyValue, String newPropertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.oldPropertyKey = oldPropertyKey;
        this.propertyValue = propertyValue;
        this.newPropertyKey = newPropertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Rename a Spring Boot configuration property key if its value matches";
    }

    @Override
    public String getDescription() {
        return "Rename the key of a property in Spring Boot configuration files when its current value matches the given value.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new RenameIfValueVisitor(oldPropertyKey, propertyValue, newPropertyKey, relaxedBinding));
    }

    private static class RenameIfValueVisitor extends TreeVisitor<Tree, ExecutionContext> {

        // A sentinel used as a temporary value in probe visitors to detect whether a key/value pair exists.
        // It is never written to any real file — the probe runs in a discarded InMemoryExecutionContext.
        private static final String PROBE_MARKER = "__rename_probe_marker__";

        private final String oldPropertyKey;
        private final String propertyValue;
        private final String newPropertyKey;
        @Nullable
        private final Boolean relaxedBinding;

        public RenameIfValueVisitor(String oldPropertyKey, String propertyValue, String newPropertyKey, @Nullable Boolean relaxedBinding) {
            this.oldPropertyKey = oldPropertyKey;
            this.propertyValue = propertyValue;
            this.newPropertyKey = newPropertyKey;
            this.relaxedBinding = relaxedBinding;
        }

        @Override
        public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
            if (tree == null) {
                return null;
            }

            boolean relaxed = !Boolean.FALSE.equals(relaxedBinding);
            return switch (tree) {
                case Yaml.Documents yamlTree -> renameYamlIfValue(yamlTree, relaxed, ctx);
                case Properties.File propertiesTree -> renamePropertiesIfValue(propertiesTree, relaxed, ctx);
                default -> tree;
            };
        }

        private Yaml.Documents renameYamlIfValue(Yaml.Documents documents, boolean relaxed, ExecutionContext ctx) {
            Tree probed = new org.openrewrite.yaml.ChangePropertyValue(
                    oldPropertyKey, PROBE_MARKER, propertyValue, false, relaxed, null).getVisitor().visit(documents, probeContext(ctx));
            if (probed == documents) {
                return documents;
            }
            // ChangePropertyKey cannot create new nesting levels when the new path is deeper than
            // the old one, so we delete the old key and merge in the fully-expanded nested structure.
            Tree afterDelete = new org.openrewrite.yaml.DeleteProperty(
                    oldPropertyKey, false, relaxed, null).getVisitor().visit(documents, ctx);
            return (Yaml.Documents) new org.openrewrite.yaml.MergeYaml(
                    "$", toNestedYaml(newPropertyKey, propertyValue), false, null, null, null, null, true)
                    .getVisitor().visit(afterDelete, ctx);
        }

        private static String toNestedYaml(String dotSeparatedKey, String value) {
            String[] parts = dotSeparatedKey.split("\\.");
            StringBuilder yaml = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                yaml.append("  ".repeat(i)).append(parts[i]).append(":\n");
            }
            yaml.append("  ".repeat(parts.length - 1))
                    .append(parts[parts.length - 1]).append(": ").append(value);
            return yaml.toString();
        }

        private Properties.File renamePropertiesIfValue(Properties.File file, boolean relaxed, ExecutionContext ctx) {
            Tree probed = new org.openrewrite.properties.ChangePropertyValue(
                    oldPropertyKey, PROBE_MARKER, propertyValue, false, relaxed).getVisitor().visit(file, probeContext(ctx));
            if (probed == file) {
                return file;
            }
            return (Properties.File) new org.openrewrite.properties.ChangePropertyKey(
                    oldPropertyKey, newPropertyKey, relaxed, false).getVisitor().visit(file, ctx);
        }

        private static ExecutionContext probeContext(ExecutionContext ctx) {
            ExecutionContext probeCtx = new InMemoryExecutionContext();
            probeCtx.putMessage(ExecutionContext.CURRENT_CYCLE, ctx.getMessage(ExecutionContext.CURRENT_CYCLE));
            return probeCtx;
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RenameSpringBootPropertyKeyIfValue that = (RenameSpringBootPropertyKeyIfValue) o;
        return Objects.equals(oldPropertyKey, that.oldPropertyKey) && Objects.equals(propertyValue, that.propertyValue) && Objects.equals(newPropertyKey, that.newPropertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldPropertyKey, propertyValue, newPropertyKey, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "RenameSpringBootPropertyKeyIfValue{" +
                "oldPropertyKey='" + oldPropertyKey + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", newPropertyKey='" + newPropertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
