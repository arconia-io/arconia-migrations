package io.arconia.rewrite.spring.boot.properties;

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
 * Comment a property in Spring Boot configuration files, including YAML and properties files.
 * <p>
 * This recipe is a convenient wrapper around the existing {@link org.openrewrite.yaml.CommentOutProperty} and
 * {@link org.openrewrite.properties.AddPropertyComment} recipes, allowing you to add a comment
 * to a property in Spring Boot configuration files at once, without needing to specify the file format.
 * <p>
 * It relies on the {@link FindSpringBootConfigFiles} recipe to find the relevant configuration files based on
 * the provided path matchers.
 */
public class CommentSpringBootProperty extends Recipe {

    @Option(displayName = "Property key",
            description = "The key for the property to comment.",
            example = "spring.ai.ollama.chat.enabled")
    private final String propertyKey;

    @Option(displayName = "Comment",
            description = "The comment to add before the property key.",
            example = "This property is deprecated and will be removed in future versions.")
    private final String comment;

    @Option(displayName = "Comment out property",
            description = "Whether to comment out the property. Defaults to `true`.",
            required = false)
    private final Boolean commentOutProperty;

    @JsonCreator
    public CommentSpringBootProperty(String propertyKey, String comment, @Nullable Boolean commentOutProperty) {
        this.propertyKey = propertyKey;
        this.comment = comment;
        this.commentOutProperty = commentOutProperty != null ? commentOutProperty : true;
    }

    @Override
    public String getDisplayName() {
        return "Add a comment to a Spring Boot configuration property";
    }

    @Override
    public String getDescription() {
        return "Add a comment to a property in Spring Boot configuration files. Optionally, comment out the property itself.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindSpringBootConfigFiles(null).getVisitor(),
                new CommentSpringBootPropertyKeyVisitor(propertyKey, comment, commentOutProperty));
    }

    private static class CommentSpringBootPropertyKeyVisitor extends TreeVisitor<Tree, ExecutionContext> {

        private final String propertyKey;
        private final String comment;
        private final Boolean commentOutProperty;

        public CommentSpringBootPropertyKeyVisitor(String propertyKey, String comment, Boolean commentOutProperty) {
            this.propertyKey = propertyKey;
            this.comment = comment;
            this.commentOutProperty = commentOutProperty;
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
            Recipe yamlRecipe = new org.openrewrite.yaml.CommentOutProperty(
                    propertyKey, comment, commentOutProperty);
            return yamlRecipe.getVisitor();
        }

        private TreeVisitor<?, ExecutionContext> getPropertiesVisitor() {
            Recipe propertiesRecipe = new org.openrewrite.properties.AddPropertyComment(
                    propertyKey, comment, commentOutProperty);
            return propertiesRecipe.getVisitor();
        }

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CommentSpringBootProperty that = (CommentSpringBootProperty) o;
        return Objects.equals(propertyKey, that.propertyKey) && Objects.equals(comment, that.comment) && Objects.equals(commentOutProperty, that.commentOutProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyKey, comment, commentOutProperty);
    }

    @Override
    public String toString() {
        return "CommentSpringBootProperty{" +
                "propertyKey='" + propertyKey + '\'' +
                ", comment='" + comment + '\'' +
                ", commentOutProperty=" + commentOutProperty +
                '}';
    }

}
