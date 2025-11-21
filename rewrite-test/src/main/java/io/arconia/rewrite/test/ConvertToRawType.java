package io.arconia.rewrite.test;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Remove generic type arguments from a specified Java class, converting them to their raw types.
 * For example, {@code MyObject<?>} becomes {@code MyObject}.
 */
public class ConvertToRawType extends Recipe {

    @Option(displayName = "Fully qualified type name",
            description = "The fully qualified name of the Java class to convert to its raw type.",
            example = "org.testcontainers.containers.PostgreSQLContainer")
    private final String fullyQualifiedTypeName;

    @JsonCreator
    public ConvertToRawType(String fullyQualifiedTypeName) {
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    }

    @Override
    public String getDisplayName() {
        return "Remove generic type arguments from a Java class";
    }

    @Override
    public String getDescription() {
        return "Remove generic type arguments from a specified Java class. " +
                "This replaces parameterized types with their raw types.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(fullyQualifiedTypeName, false),
                new RemoveGenericsVisitor()
        );
    }

    private class RemoveGenericsVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext ctx) {
            J.VariableDeclarations v = super.visitVariableDeclarations(multiVariable, ctx);

            J.Identifier rawType = convertToRawTypeIfNeeded(v.getTypeExpression());
            if (rawType != null) {
                v = v.withTypeExpression(rawType);
            }

            return v;
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

            J.Identifier rawType = convertToRawTypeIfNeeded(m.getReturnTypeExpression());
            if (rawType != null) {
                m = m.withReturnTypeExpression(rawType);
            }

            return m;
        }

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass n = super.visitNewClass(newClass, ctx);

            J.Identifier rawType = convertToRawTypeIfNeeded(n.getClazz());
            if (rawType != null) {
                // Preserve prefix when replacing in new class expressions
                n = n.withClazz(rawType.withPrefix(((J.ParameterizedType) n.getClazz()).getPrefix()));
            }

            return n;
        }

        private J.@Nullable Identifier convertToRawTypeIfNeeded(@Nullable J typeExpression) {
            if (!(typeExpression instanceof J.ParameterizedType parameterizedType)) {
                return null;
            }

            JavaType.Parameterized type = TypeUtils.asParameterized(parameterizedType.getType());
            if (type == null || !isTargetClass(type)) {
                return null;
            }

            // Replace the parameterized type with just the class type (raw type)
            J.Identifier clazz = (J.Identifier) parameterizedType.getClazz();
            return clazz.withType(type.getType());
        }

        private boolean isTargetClass(JavaType.Parameterized type) {
            JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type.getType());
            return fullyQualified != null && fullyQualifiedTypeName.equals(fullyQualified.getFullyQualifiedName());
        }

    }

}
