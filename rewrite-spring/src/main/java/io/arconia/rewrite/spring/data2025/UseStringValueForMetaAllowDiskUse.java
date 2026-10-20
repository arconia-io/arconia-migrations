package io.arconia.rewrite.spring.data2025;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Rewrites boolean {@code allowDiskUse} values on the Spring Data MongoDB {@code @Meta} annotation to
 * the string values the attribute accepts since Spring Data MongoDB 5.0.
 */
public class UseStringValueForMetaAllowDiskUse extends Recipe {

    private static final String FQN_META = "org.springframework.data.mongodb.repository.Meta";
    private static final String ATTRIBUTE = "allowDiskUse";

    @Override
    public String getDisplayName() {
        return "Use a string value for the `allowDiskUse` attribute of `@Meta`";
    }

    @Override
    public String getDescription() {
        return "Replace boolean `allowDiskUse` values on the Spring Data MongoDB `@Meta` annotation with the " +
                "equivalent string values, since the attribute changed from `boolean` to `String` in Spring Data MongoDB 5.0.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(FQN_META, false), new JavaIsoVisitor<>() {
            @Override
            public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
                J.Assignment a = super.visitAssignment(assignment, ctx);
                if (!(getCursor().getParentTreeCursor().getValue() instanceof J.Annotation annotation)
                        || !TypeUtils.isOfClassType(annotation.getType(), FQN_META)
                        || !(a.getVariable() instanceof J.Identifier name)
                        || !ATTRIBUTE.equals(name.getSimpleName())
                        || !(a.getAssignment() instanceof J.Literal literal)
                        || !(literal.getValue() instanceof Boolean value)) {
                    return a;
                }
                String text = value.toString();
                return a.withAssignment(literal
                        .withValue(text)
                        .withValueSource("\"" + text + "\"")
                        .withType(JavaType.Primitive.String));
            }
        });
    }

}
