package io.arconia.rewrite.spring.boot4;

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

/**
 * Replaces the {@code setUseForwardHeaders(boolean)} method of the Jetty and Reactor Netty web server factories,
 * deprecated in Spring Boot 4.2, with {@code setForwardedRequestHeaders(ForwardedRequestHeaders)}. Calls passing
 * an argument that is not a boolean literal are left unchanged.
 */
public class UseForwardedRequestHeaders extends Recipe {

    private static final String FORWARDED_REQUEST_HEADERS_FQN = "org.springframework.boot.web.server.ForwardedRequestHeaders";

    private static final MethodMatcher JETTY_USE_FORWARD_HEADERS = new MethodMatcher(
            "org.springframework.boot.jetty.ConfigurableJettyWebServerFactory setUseForwardHeaders(boolean)", true);

    private static final MethodMatcher NETTY_USE_FORWARD_HEADERS = new MethodMatcher(
            "org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory setUseForwardHeaders(boolean)", true);

    @Override
    public String getDisplayName() {
        return "Use `setForwardedRequestHeaders` on web server factories";
    }

    @Override
    public String getDescription() {
        return "Replace the `setUseForwardHeaders(boolean)` method of the Jetty and Reactor Netty web server factories, " +
                "deprecated in Spring Boot 4.2, with `setForwardedRequestHeaders(ForwardedRequestHeaders)`. " +
                "Calls passing an argument that is not a boolean literal are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(new UsesMethod<>(JETTY_USE_FORWARD_HEADERS), new UsesMethod<>(NETTY_USE_FORWARD_HEADERS)),
                new JavaVisitor<>() {
                    @Override
                    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                        if (!JETTY_USE_FORWARD_HEADERS.matches(m) && !NETTY_USE_FORWARD_HEADERS.matches(m)) {
                            return m;
                        }

                        Expression select = m.getSelect();
                        if (select == null || !(m.getArguments().getFirst() instanceof J.Literal literal)
                                || !(literal.getValue() instanceof Boolean useForwardHeaders)) {
                            return m;
                        }

                        String forwardedRequestHeaders = useForwardHeaders
                                ? "ForwardedRequestHeaders.X_FORWARDED_WITHOUT_PREFIX"
                                : "null";
                        if (useForwardHeaders) {
                            maybeAddImport(FORWARDED_REQUEST_HEADERS_FQN);
                        }
                        return JavaTemplate.builder("#{any(org.springframework.boot.web.server.ConfigurableWebServerFactory)}" +
                                        ".setForwardedRequestHeaders(" + forwardedRequestHeaders + ")")
                                .imports(FORWARDED_REQUEST_HEADERS_FQN)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-boot-4.2", "spring-boot-web-server-4.2"))
                                .build()
                                .apply(getCursor(), m.getCoordinates().replace(), select);
                    }
                });
    }

}
