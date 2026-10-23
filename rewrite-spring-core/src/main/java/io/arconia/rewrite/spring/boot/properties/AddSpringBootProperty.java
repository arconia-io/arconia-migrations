package io.arconia.rewrite.spring.boot.properties;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.properties.search.FindProperties;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.MergeYaml;
import org.openrewrite.yaml.search.FindProperty;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Adds a property to Spring Boot configuration files, including YAML and properties files.
 * <p>
 * This recipe is a convenient wrapper around the existing {@link org.openrewrite.properties.AddProperty} and
 * {@link MergeYaml} recipes, allowing you to add a property to Spring Boot configuration files at once, without
 * needing to specify the file format. A configuration file that already sets the property is left unchanged.
 * <p>
 * Without a required property key, the property is added to properties files and to the first document of YAML
 * files. With a required property key, it is added only to the properties files and YAML documents that set that
 * key, which keeps a new property next to the configuration it complements.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class AddSpringBootProperty extends Recipe {

    @Option(displayName = "Property key",
            description = "The property key to add.",
            example = "spring.cloud.config.retry.enabled")
    private final String propertyKey;

    @Option(displayName = "Property value",
            description = "The value of the property to add.",
            example = "true")
    private final String propertyValue;

    @Option(displayName = "Required property key",
            description = """
                    Only add the property to the properties files and YAML documents that set this property key.
                    If no value is provided, the property is added to properties files and to the first document of YAML files.
                    """,
            required = false,
            example = "spring.cloud.config.fail-fast")
    @Nullable
    private final String requiredPropertyKey;

    @Option(displayName = "Use relaxed binding",
            description = "Whether to match property keys using Spring Boot relaxed binding rules. Defaults to `true`.",
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
    public AddSpringBootProperty(String propertyKey, String propertyValue, @Nullable String requiredPropertyKey, @Nullable Boolean relaxedBinding, @Nullable List<String> pathExpressions) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        this.requiredPropertyKey = requiredPropertyKey;
        this.relaxedBinding = relaxedBinding;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Add a Spring Boot configuration property";
    }

    @Override
    public String getDescription() {
        return "Add a property to Spring Boot configuration files that do not set it yet, optionally only where another property is set.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(pathExpressions).getVisitor(),
                new AddSpringBootPropertyVisitor(propertyKey, propertyValue, requiredPropertyKey, !Boolean.FALSE.equals(relaxedBinding)));
    }

    private static class AddSpringBootPropertyVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String propertyKey;
        private final String propertyValue;
        @Nullable
        private final String requiredPropertyKey;
        private final boolean relaxedBinding;

        public AddSpringBootPropertyVisitor(String propertyKey, String propertyValue, @Nullable String requiredPropertyKey, boolean relaxedBinding) {
            this.propertyKey = propertyKey;
            this.propertyValue = propertyValue;
            this.requiredPropertyKey = requiredPropertyKey;
            this.relaxedBinding = relaxedBinding;
        }

        @Override
        public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
            if (tree == null) {
                return null;
            }

            return switch (tree) {
                case Yaml.Documents yamlTree -> addToYaml(yamlTree, ctx);
                case Properties.File propertiesTree -> addToProperties(propertiesTree, ctx);
                default -> tree;
            };
        }

        private Properties.File addToProperties(Properties.File file, ExecutionContext ctx) {
            if (!FindProperties.find(file, propertyKey, relaxedBinding).isEmpty()) {
                return file;
            }
            // Keep the new property next to the required one, or append it after the existing ones.
            String insertAfter = null;
            if (requiredPropertyKey != null) {
                Properties.Entry required = FindProperties.find(file, requiredPropertyKey, relaxedBinding).stream().findFirst().orElse(null);
                if (required == null) {
                    return file;
                }
                insertAfter = required.getKey();
            }
            Properties.File added = (Properties.File) new org.openrewrite.properties.AddProperty(
                    propertyKey, propertyValue, null, null, false,
                    insertAfter != null ? org.openrewrite.properties.AddProperty.InsertMode.After : org.openrewrite.properties.AddProperty.InsertMode.Last,
                    insertAfter).getVisitor().visit(file, ctx);
            return added != null ? added : file;
        }

        private Yaml.Documents addToYaml(Yaml.Documents documents, ExecutionContext ctx) {
            Cursor parent = new Cursor(getCursor(), documents);
            if (requiredPropertyKey == null) {
                if (documents.getDocuments().isEmpty() || !FindProperty.find(documents, propertyKey, relaxedBinding).isEmpty()) {
                    return documents;
                }
                return withDocuments(documents, ListUtils.mapFirst(documents.getDocuments(), document -> mergeInto(document, parent, ctx)));
            }
            return withDocuments(documents, ListUtils.map(documents.getDocuments(), document ->
                    FindProperty.find(document, propertyKey, relaxedBinding).isEmpty()
                            && !FindProperty.find(document, requiredPropertyKey, relaxedBinding).isEmpty()
                            ? mergeInto(document, parent, ctx)
                            : document));
        }

        private static Yaml.Documents withDocuments(Yaml.Documents documents, @Nullable List<Yaml.Document> newDocuments) {
            return newDocuments == null ? documents : documents.withDocuments(newDocuments);
        }

        private Yaml.Document mergeInto(Yaml.Document document, Cursor parent, ExecutionContext ctx) {
            Yaml.Document merged = (Yaml.Document) new MergeYaml("$", toNestedYaml(), true, null, null, null, null, true)
                    .getVisitor().visit(document, ctx, parent);
            return merged != null ? merged : document;
        }

        private String toNestedYaml() {
            String[] keys = propertyKey.split("\\.");
            StringBuilder yaml = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                yaml.append("  ".repeat(i)).append(keys[i]).append(':');
                yaml.append(i == keys.length - 1 ? " " + propertyValue : "\n");
            }
            return yaml.toString();
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AddSpringBootProperty that = (AddSpringBootProperty) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(propertyValue, that.propertyValue) && Objects.equals(requiredPropertyKey, that.requiredPropertyKey) && Objects.equals(relaxedBinding, that.relaxedBinding) && Objects.equals(pathExpressions, that.pathExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyKey, propertyValue, requiredPropertyKey, relaxedBinding, pathExpressions);
    }

    @Override
    public String toString() {
        return "AddSpringBootProperty{" +
                "propertyKey='" + propertyKey + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", requiredPropertyKey='" + requiredPropertyKey + '\'' +
                ", relaxedBinding=" + relaxedBinding +
                ", pathExpressions=" + pathExpressions +
                '}';
    }

}
