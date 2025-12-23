package io.arconia.rewrite.spring.ai;

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
import org.openrewrite.java.tree.TypeUtils;

/**
 * Replaces `new QuestionAnswerAdvisor(vectorStore)` with `QuestionAnswerAdvisor.builder(vectorStore).build()`.
 */
public class UseQuestionAnswerAdvisorBuilder extends Recipe {

    private static final String FQN_QUESTION_ANSWER_ADVISOR = "org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor";
    private static final String FQN_VECTOR_STORE = "org.springframework.ai.vectorstore.VectorStore";

    private static final MethodMatcher QUESTION_ANSWER_ADVISOR_CONSTRUCTOR_MATCHER
            = new MethodMatcher("org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor <constructor>(*)");

    @Override
    public String getDisplayName() {
        return "Use `QuestionAnswerAdvisor.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new QuestionAnswerAdvisor(vectorStore)` with `QuestionAnswerAdvisor.builder(vectorStore).build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(QUESTION_ANSWER_ADVISOR_CONSTRUCTOR_MATCHER),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (!QUESTION_ANSWER_ADVISOR_CONSTRUCTOR_MATCHER.matches(nc)) {
                            return nc;
                        }

                        if (nc.getArguments().size() != 1) {
                            return nc;
                        }

                        Expression arg = nc.getArguments().getFirst();

                        if (!TypeUtils.isOfClassType(arg.getType(), FQN_VECTOR_STORE)) {
                            return nc;
                        }

                        maybeAddImport(FQN_QUESTION_ANSWER_ADVISOR);

                        return JavaTemplate.builder("QuestionAnswerAdvisor.builder(#{any(org.springframework.ai.vectorstore.VectorStore)}).build()")
                                .imports(FQN_QUESTION_ANSWER_ADVISOR)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-advisors-vector-store-1.1.+"))
                                .build()
                                .apply(getCursor(), nc.getCoordinates().replace(), arg);
                    }
                }
        );
    }

}
