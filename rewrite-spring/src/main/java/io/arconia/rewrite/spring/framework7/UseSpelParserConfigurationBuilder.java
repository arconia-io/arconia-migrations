package io.arconia.rewrite.spring.framework7;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Replaces the {@code SpelParserConfiguration} constructors, deprecated in Spring Framework 7.1, with the
 * builder API. Each constructor argument becomes the builder setter of the same name, an auto-grow flag
 * set to {@code true} becomes the corresponding opt-in method, and arguments matching a builder default
 * ({@code false} flags and {@code null} compiler settings) are dropped. The no-argument constructor
 * becomes {@code SpelParserConfiguration.withDefaults()}.
 *
 * <p>A constructor call is left unchanged when the builder cannot express it faithfully: an auto-grow flag
 * that is not a {@code boolean} literal, a compiler mode that is neither {@code null} nor a
 * {@code SpelCompilerMode} constant, or a negative maximum auto-grow size, which the builder rejects.
 */
public class UseSpelParserConfigurationBuilder extends Recipe {

    private static final String SPEL_PARSER_CONFIGURATION_FQN = "org.springframework.expression.spel.SpelParserConfiguration";

    private static final String SPEL_COMPILER_MODE_FQN = "org.springframework.expression.spel.SpelCompilerMode";

    private static final MethodMatcher CONSTRUCTOR = new MethodMatcher(SPEL_PARSER_CONFIGURATION_FQN + " <constructor>(..)");

    // Constructor parameters in declaration order, shared by all constructors except the boolean-only ones.
    private static final List<String> PARAMETERS = List.of("compilerMode", "compilerClassLoader", "autoGrowNullReferences",
            "autoGrowCollections", "maximumAutoGrowSize", "maximumExpressionLength", "maximumOperations",
            "maximumBigPowerBits", "maximumNestingDepth");

    @Override
    public String getDisplayName() {
        return "Use the `SpelParserConfiguration` builder";
    }

    @Override
    public String getDescription() {
        return "Replace the `SpelParserConfiguration` constructors, deprecated in Spring Framework 7.1, with " +
                "`SpelParserConfiguration.withDefaults()` or the builder API, setting each constructor argument through " +
                "its named builder method. Constructor calls whose auto-grow flags are not literals are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(CONSTRUCTOR), new JavaVisitor<>() {
            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass n = (J.NewClass) super.visitNewClass(newClass, ctx);
                JavaType.Method constructorType = n.getConstructorType();
                if (!CONSTRUCTOR.matches(n) || constructorType == null || n.getBody() != null) {
                    return n;
                }

                List<Expression> arguments = n.getArguments().stream()
                        .filter(argument -> !(argument instanceof J.Empty))
                        .toList();
                List<String> parameters = parameterNames(constructorType, arguments.size());
                StringBuilder code = new StringBuilder("SpelParserConfiguration.builder()");
                List<Object> templateArguments = new ArrayList<>();
                for (int i = 0; i < arguments.size(); i++) {
                    Expression argument = arguments.get(i);
                    String parameter = parameters.get(i);
                    switch (parameter) {
                        case "autoGrowNullReferences", "autoGrowCollections" -> {
                            Boolean value = booleanLiteral(argument);
                            if (value == null) {
                                return n;
                            }
                            if (value) {
                                code.append('.').append(parameter).append("()");
                            }
                        }
                        case "compilerMode" -> {
                            if (isNullLiteral(argument)) {
                                continue;
                            }
                            if (!isCompilerModeConstant(argument)) {
                                return n;
                            }
                            code.append(".compilerMode(#{any(" + SPEL_COMPILER_MODE_FQN + ")})");
                            templateArguments.add(argument);
                        }
                        case "compilerClassLoader" -> {
                            if (isNullLiteral(argument)) {
                                continue;
                            }
                            code.append(".compilerClassLoader(#{any(java.lang.ClassLoader)})");
                            templateArguments.add(argument);
                        }
                        default -> {
                            if ("maximumAutoGrowSize".equals(parameter) && isNegativeLiteral(argument)) {
                                return n;
                            }
                            code.append('.').append(parameter).append("(#{any(int)})");
                            templateArguments.add(argument);
                        }
                    }
                }
                code.append(".build()");

                String replacement = arguments.isEmpty() ? "SpelParserConfiguration.withDefaults()" : code.toString();
                return JavaTemplate.builder(replacement)
                        .imports(SPEL_PARSER_CONFIGURATION_FQN)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-expression-7.1", "spring-core-7.0"))
                        .build()
                        .apply(getCursor(), n.getCoordinates().replace(), templateArguments.toArray());
            }
        });
    }

    private static List<String> parameterNames(JavaType.Method constructorType, int argumentCount) {
        // The (boolean, boolean) and (boolean, boolean, int) constructors start from the auto-grow flags.
        List<JavaType> parameterTypes = constructorType.getParameterTypes();
        if (!parameterTypes.isEmpty() && parameterTypes.get(0) == JavaType.Primitive.Boolean) {
            return PARAMETERS.subList(2, 2 + argumentCount);
        }
        return PARAMETERS.subList(0, argumentCount);
    }

    private static @Nullable Boolean booleanLiteral(Expression expression) {
        if (expression instanceof J.Literal literal && literal.getValue() instanceof Boolean value) {
            return value;
        }
        return null;
    }

    private static boolean isNullLiteral(Expression expression) {
        return expression instanceof J.Literal literal && literal.getType() == JavaType.Primitive.Null;
    }

    private static boolean isNegativeLiteral(Expression expression) {
        if (expression instanceof J.Literal literal && literal.getValue() instanceof Number value) {
            return value.intValue() < 0;
        }
        return expression instanceof J.Unary unary && unary.getOperator() == J.Unary.Type.Negative
                && unary.getExpression() instanceof J.Literal;
    }

    private static boolean isCompilerModeConstant(Expression expression) {
        JavaType.Variable variable = switch (expression) {
            case J.FieldAccess fieldAccess -> fieldAccess.getName().getFieldType();
            case J.Identifier identifier -> identifier.getFieldType();
            default -> null;
        };
        return variable != null && TypeUtils.isOfClassType(variable.getOwner(), SPEL_COMPILER_MODE_FQN);
    }

}
