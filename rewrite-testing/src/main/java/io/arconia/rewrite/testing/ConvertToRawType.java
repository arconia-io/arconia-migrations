package io.arconia.rewrite.testing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Converts parameterized types of a specified Java class to their raw type equivalents.
 * <p>
 * Removes generic type arguments from all usages of the specified class, converting
 * {@code PostgreSQLContainer<?>}, {@code PostgreSQLContainer<String>}, etc. to {@code PostgreSQLContainer}.
 * <p>
 * Applied to all contexts: field declarations, method parameters and return types,
 * local variables, type casts, new class instantiations, and qualified names.
 * <p>
 * Performs exact type matching based on the fully qualified name. Subclasses and
 * implementations are not affected.
 */
public class ConvertToRawType extends Recipe {

    @Option(displayName = "Fully qualified type name",
            description = "The fully qualified name of the Java class whose parameterized types should be converted to raw types. " +
                    "Only exact matches of this type will be converted; subclasses and implementations are not affected.",
            example = "org.testcontainers.postgresql.PostgreSQLContainer")
    private final String fullyQualifiedTypeName;

    @JsonCreator
    public ConvertToRawType(String fullyQualifiedTypeName) {
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    }

    @Override
    public String getDisplayName() {
        return "Convert parameterized types to raw types";
    }

    @Override
    public String getDescription() {
        return "Converts all parameterized usages of a specified Java class to their raw type. " +
                "Only exact type matches are converted; subclasses and implementations are not affected.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(fullyQualifiedTypeName, false),
                new JavaVisitor<>() {
                    @Override
                    public J visitParameterizedType(J.ParameterizedType type, ExecutionContext ctx) {
                        J.ParameterizedType pt = (J.ParameterizedType) super.visitParameterizedType(type, ctx);

                        JavaType.Parameterized paramType = TypeUtils.asParameterized(pt.getType());
                        if (paramType == null || !TypeUtils.isOfClassType(paramType.getType(), fullyQualifiedTypeName)) {
                            return pt;
                        }

                        J rawType;
                        J clz = pt.getClazz();
                        if (clz instanceof J.Identifier identifier) {
                            rawType = identifier.withPrefix(pt.getPrefix()).withType(paramType.getType());
                        } else if (clz instanceof J.FieldAccess fieldAccess) {
                            rawType = fieldAccess.withPrefix(pt.getPrefix()).withType(paramType.getType());
                        } else {
                            // Rare NameTree shapes (e.g. an annotated qualified name like
                            // `Outer.@Foo Inner<String>`) are left alone — we can't safely strip the
                            // type parameters without disturbing the qualified-name + annotation structure.
                            // Plain type-use and field-targeted annotations are handled fine because the
                            // annotation lives on an enclosing J.AnnotatedType / J.VariableDeclarations,
                            // not on pt.getClazz() itself.
                            return pt;
                        }

                        removeUnusedTypeArgumentImports(pt.getTypeParameters());
                        return rawType;
                    }

                    private void removeUnusedTypeArgumentImports(@Nullable List<Expression> typeArguments) {
                        if (typeArguments == null) {
                            return;
                        }
                        for (Expression typeArg : typeArguments) {
                            removeImportsForType(typeArg.getType());
                        }
                    }

                    private void removeImportsForType(@Nullable JavaType type) {
                        JavaType.FullyQualified fq = TypeUtils.asFullyQualified(type);
                        if (fq != null) {
                            maybeRemoveImport(fq.getFullyQualifiedName());
                        }
                        if (type instanceof JavaType.Parameterized parameterized) {
                            for (JavaType arg : parameterized.getTypeParameters()) {
                                removeImportsForType(arg);
                            }
                        }
                    }
                }
        );
    }

}
