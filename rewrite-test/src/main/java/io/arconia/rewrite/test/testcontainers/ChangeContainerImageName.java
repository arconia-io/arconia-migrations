package io.arconia.rewrite.test.testcontainers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

/**
 * Rewrites the image argument of a Testcontainers container constructor when the original image
 * starts with a given prefix. Handles the string-literal form, {@code DockerImageName.parse(...)}
 * (including chained calls on the parsed instance), and the deprecated {@code new DockerImageName(...)}
 * one- and two-arg forms.
 */
public class ChangeContainerImageName extends Recipe {

    private static final String DOCKER_IMAGE_NAME = "org.testcontainers.utility.DockerImageName";
    private static final MethodMatcher DOCKER_IMAGE_PARSE =
            new MethodMatcher(DOCKER_IMAGE_NAME + " parse(java.lang.String)");

    @Option(displayName = "Container type",
            description = "Fully qualified name of the container class whose constructor's image argument should be rewritten.",
            example = "org.testcontainers.containers.KafkaContainer")
    private final String containerType;

    @Option(displayName = "Old image prefix",
            description = "Literal prefix matched via `String.startsWith` against the image string passed to the container constructor.",
            example = "confluentinc/cp-kafka")
    private final String oldImagePrefix;

    @Option(displayName = "New image name",
            description = "Image name (without tag) used to replace the matched image name.",
            example = "apache/kafka-native")
    private final String newImageName;

    @Option(displayName = "New image tag",
            description = "Image tag used to replace the matched image's tag. When omitted, the original tag is preserved " +
                    "(and the result has no tag if the original had none).",
            example = "latest",
            required = false)
    private final @Nullable String newImageTag;

    @Option(displayName = "Comment",
            description = "Optional block comment inserted inline before each rewritten constructor call. " +
                    "Useful for surfacing a migration note (e.g. suggesting an alternative API). " +
                    "Must not contain `*/`.",
            example = "To keep using Confluent, switch to ConfluentKafkaContainer.",
            required = false)
    private final @Nullable String comment;

    @JsonCreator
    public ChangeContainerImageName(String containerType, String oldImagePrefix, String newImageName,
                                    @Nullable String newImageTag, @Nullable String comment) {
        this.containerType = containerType;
        this.oldImagePrefix = oldImagePrefix;
        this.newImageName = newImageName;
        this.newImageTag = newImageTag;
        this.comment = comment;
    }

    @Override
    public String getDisplayName() {
        return "Change the image name passed to a Testcontainers container";
    }

    @Override
    public String getDescription() {
        return "Rewrites the image argument of a Testcontainers container constructor when the original image " +
                "starts with `oldImagePrefix`. Supports a string literal, `DockerImageName.parse(...)` (including " +
                "chained calls on the parsed instance), and the deprecated `new DockerImageName(...)` (one- and " +
                "two-arg) forms. Non-literal image arguments (e.g. references to constants) are left untouched. " +
                "When `comment` is set, a block comment with that text is inserted inline before each rewritten " +
                "constructor call.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(containerType, false),
                new JavaIsoVisitor<>() {
                    @Override
                    public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = super.visitNewClass(newClass, ctx);
                        if (!TypeUtils.isOfClassType(nc.getType(), containerType)) {
                            return nc;
                        }
                        List<Expression> args = nc.getArguments();
                        List<Expression> rewritten = ListUtils.mapFirst(args, this::rewriteImageArg);
                        if (rewritten == null || rewritten == args) {
                            return nc;
                        }
                        nc = nc.withArguments(rewritten);
                        return comment == null ? nc : withLeadingBlockComment(nc, comment);
                    }

                    private Expression rewriteImageArg(Expression arg) {
                        switch (arg) {
                            case J.Literal literal -> {
                                return rewriteImageLiteral(literal);
                            }
                            case J.MethodInvocation mi -> {
                                if (DOCKER_IMAGE_PARSE.matches(mi)) {
                                    List<Expression> mapped = ListUtils.mapFirst(mi.getArguments(), this::rewriteImageArg);
                                    return mapped == null ? mi : mi.withArguments(mapped);
                                }
                                if (mi.getSelect() != null && isOnDockerImageName(mi)) {
                                    return mi.withSelect(rewriteImageArg(mi.getSelect()));
                                }
                                return mi;
                            }
                            case J.NewClass nc when TypeUtils.isOfClassType(nc.getType(), DOCKER_IMAGE_NAME) -> {
                                return rewriteDockerImageNameConstruction(nc);
                            }
                            default -> {
                            }
                        }
                        return arg;
                    }

                    private Expression rewriteImageLiteral(J.Literal literal) {
                        String value = stringValueOf(literal);
                        if (value == null || !value.startsWith(oldImagePrefix)) {
                            return literal;
                        }
                        return withStringValue(literal, replacementFor(value));
                    }

                    private J.NewClass rewriteDockerImageNameConstruction(J.NewClass nc) {
                        List<Expression> args = nc.getArguments();
                        if (args.isEmpty() || !(args.getFirst() instanceof J.Literal first)) {
                            return nc;
                        }
                        String firstValue = stringValueOf(first);
                        if (firstValue == null || !firstValue.startsWith(oldImagePrefix)) {
                            return nc;
                        }
                        if (args.size() == 1) {
                            return nc.withArguments(List.of(withStringValue(first, replacementFor(firstValue))));
                        }
                        if (args.size() == 2) {
                            J.Literal newNameLit = withStringValue(first, newImageName);
                            Expression tagArg = args.get(1);
                            if (newImageTag == null) {
                                return nc.withArguments(List.of(newNameLit, tagArg));
                            }
                            if (tagArg instanceof J.Literal tagLit) {
                                return nc.withArguments(List.of(newNameLit, withStringValue(tagLit, newImageTag)));
                            }
                            // Non-literal tag with an explicit newImageTag would require synthesising
                            // a fresh literal; left untouched to avoid losing surrounding formatting.
                            return nc;
                        }
                        return nc;
                    }
                }
        );
    }

    private String replacementFor(String originalImage) {
        int colon = originalImage.indexOf(':');
        String originalTag = colon >= 0 && colon < originalImage.length() - 1
                ? originalImage.substring(colon + 1)
                : null;
        String tag = newImageTag != null ? newImageTag : originalTag;
        return tag == null ? newImageName : newImageName + ":" + tag;
    }

    private static boolean isOnDockerImageName(J.MethodInvocation mi) {
        return mi.getMethodType() != null
                && TypeUtils.isOfClassType(mi.getMethodType().getDeclaringType(), DOCKER_IMAGE_NAME);
    }

    private static J.NewClass withLeadingBlockComment(J.NewClass nc, String text) {
        Space prefix = nc.getPrefix();
        for (Comment existing : prefix.getComments()) {
            if (existing instanceof TextComment t && t.getText().contains(text)) {
                return nc;
            }
        }
        TextComment newComment = new TextComment(true, " " + text + " ", " ", Markers.EMPTY);
        return nc.withPrefix(prefix.withComments(ListUtils.concat(prefix.getComments(), newComment)));
    }

    private static @Nullable String stringValueOf(J.Literal literal) {
        return literal.getValue() instanceof String s ? s : null;
    }

    private static J.Literal withStringValue(J.Literal literal, String newValue) {
        // Assumes the original literal was a regular double-quoted string. A text-block
        // Docker image would be rewritten to a single-line string here — none exist in practice.
        return literal.withValue(newValue).withValueSource("\"" + newValue + "\"");
    }

}
