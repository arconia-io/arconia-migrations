package io.arconia.rewrite.openapi.swagger;

import java.util.List;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Updates the methods of {@code ObjectMapperProcessor} implementations to the Swagger Core 3 interface. Swagger Core 3
 * builds immutable Jackson 3 mappers, so each method returns the mapper to use instead of changing the one it receives.
 * The deprecated {@code processYamlObjectMapper} method, which Swagger Core no longer calls, is removed.
 */
public class ChangeObjectMapperProcessorMethods extends Recipe {

    private static final String OBJECT_MAPPER_PROCESSOR_FQN = "io.swagger.v3.oas.integration.api.ObjectMapperProcessor";

    private static final Set<String> PROCESSOR_METHODS = Set.of(
            "processJsonObjectMapper", "processOutputJsonObjectMapper", "processOutputYamlObjectMapper");

    private static final String REMOVED_PROCESSOR_METHOD = "processYamlObjectMapper";

    private static final JavaTemplate RETURN_MAPPER = JavaTemplate.builder("return #{any()};").contextSensitive().build();

    @Override
    public String getDisplayName() {
        return "Return the mapper from `ObjectMapperProcessor` methods";
    }

    @Override
    public String getDescription() {
        return "Change the `void` methods of `ObjectMapperProcessor` implementations to return the `ObjectMapper` they " +
                "receive, as the Swagger Core 3 interface expects, and remove the deprecated `processYamlObjectMapper` " +
                "method, which Swagger Core no longer calls. A method that configures the mapper in place still needs " +
                "to build a new one with `mapper.rebuild()`, since Jackson 3 mappers are immutable.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(OBJECT_MAPPER_PROCESSOR_FQN, true), new JavaIsoVisitor<>() {
            @Override
            public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
                J.Block b = super.visitBlock(block, ctx);
                List<Statement> statements = ListUtils.filter(b.getStatements(), statement ->
                        !(statement instanceof J.MethodDeclaration method && isProcessorMethod(method, Set.of(REMOVED_PROCESSOR_METHOD))));
                return statements == null ? b : b.withStatements(statements);
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);
                if (!isProcessorMethod(m, PROCESSOR_METHODS)
                        || !(m.getParameters().get(0) instanceof J.VariableDeclarations parameter)
                        || parameter.getTypeExpression() == null
                        || parameter.getType() == null
                        || m.getReturnTypeExpression() == null
                        || m.getMethodType() == null) {
                    return m;
                }
                TypeTree mapperType = parameter.getTypeExpression();
                J.Identifier mapper = parameter.getVariables().get(0).getName();

                m = m.withReturnTypeExpression(mapperType.<TypeTree>withId(Tree.randomId())
                                .withPrefix(m.getReturnTypeExpression().getPrefix()))
                        .withMethodType(m.getMethodType().withReturnType(parameter.getType()));
                m = (J.MethodDeclaration) new ReturnMapperVisitor(mapper).visitNonNull(m, ctx, getCursor().getParentOrThrow());

                J.Block body = m.getBody();
                if (body == null || endsAbruptly(body)) {
                    return m;
                }
                return RETURN_MAPPER.apply(updateCursor(m), body.getCoordinates().lastStatement(),
                        mapper.withId(Tree.randomId()).withPrefix(Space.EMPTY));
            }
        });
    }

    // Whether the method implements one of the given ObjectMapperProcessor methods in its Swagger Core 2 form.
    private static boolean isProcessorMethod(J.MethodDeclaration method, Set<String> names) {
        JavaType.Method methodType = method.getMethodType();
        return names.contains(method.getSimpleName())
                && method.getParameters().size() == 1
                && methodType != null
                && methodType.getReturnType() == JavaType.Primitive.Void
                && !TypeUtils.isOfClassType(methodType.getDeclaringType(), OBJECT_MAPPER_PROCESSOR_FQN)
                && TypeUtils.isAssignableTo(OBJECT_MAPPER_PROCESSOR_FQN, methodType.getDeclaringType());
    }

    private static boolean endsAbruptly(J.Block body) {
        List<Statement> statements = body.getStatements();
        if (statements.isEmpty()) {
            return false;
        }
        Statement last = statements.get(statements.size() - 1);
        return last instanceof J.Return || last instanceof J.Throw;
    }

    // Turns each bare return of the method into a return of the mapper, leaving nested lambdas and classes alone.
    private static class ReturnMapperVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final J.Identifier mapper;

        // Whether the visitor has entered the method whose returns it changes.
        private boolean inMethod;

        ReturnMapperVisitor(J.Identifier mapper) {
            this.mapper = mapper;
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDeclaration, ExecutionContext ctx) {
            if (inMethod) {
                return methodDeclaration;
            }
            inMethod = true;
            return super.visitMethodDeclaration(methodDeclaration, ctx);
        }

        @Override
        public J.Lambda visitLambda(J.Lambda lambda, ExecutionContext ctx) {
            return lambda;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            return classDecl;
        }

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            return newClass;
        }

        @Override
        public J.Return visitReturn(J.Return aReturn, ExecutionContext ctx) {
            if (aReturn.getExpression() != null) {
                return aReturn;
            }
            return aReturn.withExpression(mapper.withId(Tree.randomId()).withPrefix(Space.SINGLE_SPACE));
        }

    }

}
