package io.arconia.rewrite.spring.framework.framework6;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework6.UpgradeSpringFramework_6_2".
 */
class UpgradeSpringFramework_6_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.framework6.UpgradeSpringFramework_6_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-beans-6.1", "spring-core-6.1", "spring-context-6.1", "spring-messaging-6.1", "spring-test-6.1",
                        "spring-web-6.1", "spring-webflux-6.1", "spring-webmvc-6.1", "spring-websocket-6.1", "jakarta.servlet-api"));
    }

    @Test
    void dependencies() {
        rewriteRun(
            spec -> spec.beforeRecipe(withToolingApi()),
            buildGradle(
                """
                plugins {
                    id "java-library"
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    implementation "org.webjars:webjars-locator-core"
                }
                """,
                """
                plugins {
                    id "java-library"
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    implementation "org.webjars:webjars-locator-lite"
                }
                """
            )
        );
    }

    @Test
    void springBeansModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.aot.BeanInstanceSupplier;

                class Demo {
                    void test(BeanInstanceSupplier<String> supplier) {
                        supplier.withShortcuts("banana");
                    }
                }
                """,
                """
                import org.springframework.beans.factory.aot.BeanInstanceSupplier;

                class Demo {
                    void test(BeanInstanceSupplier<String> supplier) {
                        supplier.withShortcut("banana");
                    }
                }
                """
            )
        );
    }

    @Test
    void springContextModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.validation.method.MethodValidationResult;

                class Demo {
                    void test(MethodValidationResult result) {
                        result.getAllValidationResults();
                    }
                }
                """,
                """
                import org.springframework.validation.method.MethodValidationResult;

                class Demo {
                    void test(MethodValidationResult result) {
                        result.getParameterValidationResults();
                    }
                }
                """
            )
        );
    }

    @Test
    void springMessagingModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.messaging.handler.invocation.AsyncHandlerMethodReturnValueHandler;
                import org.springframework.messaging.simp.stomp.ConnectionHandlingStompSession;
                import org.springframework.messaging.simp.stomp.ReactorNettyTcpStompClient;
                import org.springframework.messaging.tcp.TcpConnection;
                import org.springframework.messaging.tcp.TcpOperations;

                class Demo {
                    void test(AsyncHandlerMethodReturnValueHandler handler) {
                        handler.toListenableFuture(null, null);
                    }
                    void other(ConnectionHandlingStompSession session) {
                        session.getSessionFuture();
                    }
                    void reactor(ReactorNettyTcpStompClient client) {
                        client.connect(null);
                        client.connect(null, null);
                    }
                    void tcp(TcpConnection connection, TcpOperations operations) {
                        connection.send(null);
                        operations.connect(null);
                        operations.connect(null, null);
                        operations.shutdown();
                    }
                }
                """,
                """
                import org.springframework.messaging.handler.invocation.AsyncHandlerMethodReturnValueHandler;
                import org.springframework.messaging.simp.stomp.ConnectionHandlingStompSession;
                import org.springframework.messaging.simp.stomp.ReactorNettyTcpStompClient;
                import org.springframework.messaging.tcp.TcpConnection;
                import org.springframework.messaging.tcp.TcpOperations;

                class Demo {
                    void test(AsyncHandlerMethodReturnValueHandler handler) {
                        handler.toCompletableFuture(null, null);
                    }
                    void other(ConnectionHandlingStompSession session) {
                        session.getSession();
                    }
                    void reactor(ReactorNettyTcpStompClient client) {
                        client.connectAsync(null);
                        client.connectAsync(null, null);
                    }
                    void tcp(TcpConnection connection, TcpOperations operations) {
                        connection.sendAsync(null);
                        operations.connectAsync(null);
                        operations.connectAsync(null, null);
                        operations.shutdownAsync();
                    }
                }
                """
            )
        );
    }

    @Test
    void springTestModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.test.web.reactive.server.ExchangeResult;
                import org.springframework.test.web.reactive.server.WebTestClient;
                import org.springframework.test.web.servlet.result.StatusResultMatchers;
                import org.springframework.web.reactive.function.client.ExchangeStrategies;

                class Demo {
                    void exchange(ExchangeResult result) {
                        result.getRawStatusCode();
                    }
                    void client(WebTestClient client) {
                        client.mutate().exchangeStrategies(ExchangeStrategies.builder().build());
                        client.mutate().exchangeStrategies(s -> {});
                        client.post().syncBody(null);
                    }
                    void matchers(StatusResultMatchers matchers) {
                        matchers.isMovedTemporarily();
                        matchers.isRequestEntityTooLarge();
                        matchers.isRequestUriTooLong();
                        matchers.isCheckpoint();
                    }
                }
                """,
                """
                import org.springframework.test.web.reactive.server.ExchangeResult;
                import org.springframework.test.web.reactive.server.WebTestClient;
                import org.springframework.test.web.servlet.result.StatusResultMatchers;
                import org.springframework.web.reactive.function.client.ExchangeStrategies;

                class Demo {
                    void exchange(ExchangeResult result) {
                        result.getStatus();
                    }
                    void client(WebTestClient client) {
                        client.mutate().codecs(ExchangeStrategies.builder().build());
                        client.mutate().codecs(s -> {});
                        client.post().bodyValue(null);
                    }
                    void matchers(StatusResultMatchers matchers) {
                        matchers.isFound();
                        matchers.isPayloadTooLarge();
                        matchers.isUriTooLong();
                        matchers.isEarlyHints();
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.http.HttpStatus;
                import org.springframework.http.MediaType;
                import org.springframework.http.ResponseEntity;
                import org.springframework.http.client.ClientHttpResponse;
                import org.springframework.http.server.reactive.ServerHttpResponse;
                import org.springframework.web.client.RestClientResponseException;
                import org.springframework.web.client.UnknownContentTypeException;
                import org.springframework.web.server.MethodNotAllowedException;
                import org.springframework.web.server.NotAcceptableStatusException;
                import org.springframework.web.server.ResponseStatusException;
                import org.springframework.web.server.UnsupportedMediaTypeStatusException;
                import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
                import org.springframework.web.util.UriComponentsBuilder;

                class Demo {
                    void entity(ResponseEntity entity) {
                        entity.getStatusCodeValue();
                    }
                    void response(ClientHttpResponse response) {
                        response.getRawStatusCode();
                    }
                    void reactiveResponse(org.springframework.http.client.reactive.ClientHttpResponse response) {
                        response.getRawStatusCode();
                    }
                    void serverResponse(ServerHttpResponse response) {
                        response.getRawStatusCode();
                    }
                    void exception1(RestClientResponseException ex) {
                        ex.getRawStatusCode();
                    }
                    void unknownContentType(UnknownContentTypeException ex) {
                        ex.getRawStatusCode();
                    }
                    void methodNotAllowed(MethodNotAllowedException ex) {
                        ex.getResponseHeaders();
                    }
                    void notAcceptable(NotAcceptableStatusException ex) {
                        ex.getResponseHeaders();
                    }
                    void responseStatus(ResponseStatusException ex) {
                        ex.getResponseHeaders();
                    }
                    void unsupportedMediaType(UnsupportedMediaTypeStatusException ex) {
                        ex.getResponseHeaders();
                    }
                    void handler(ResponseStatusExceptionHandler handler) {
                        handler.determineRawStatusCode(null);
                    }
                    void uriBuilder(UriComponentsBuilder builder) {
                        builder.fromHttpUrl("http://example.com");
                        builder.fromOriginHeader("origin");
                    }
                    void mediaTypeConstants() {
                        String a = MediaType.APPLICATION_GRAPHQL;
                        String b = MediaType.APPLICATION_GRAPHQL_VALUE;
                        String c = MediaType.APPLICATION_JSON_UTF8;
                        String d = MediaType.APPLICATION_JSON_UTF8_VALUE;
                        String e = MediaType.APPLICATION_PROBLEM_JSON_UTF8;
                        String f = MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE;
                        String g = MediaType.APPLICATION_STREAM_JSON;
                        String h = MediaType.APPLICATION_STREAM_JSON_VALUE;
                    }
                    void httpStatusConstants() {
                        HttpStatus a = HttpStatus.MOVED_TEMPORARILY;
                        HttpStatus b = HttpStatus.REQUEST_ENTITY_TOO_LARGE;
                        HttpStatus c = HttpStatus.REQUEST_URI_TOO_LONG;
                    }
                }
                """,
                """
                import org.springframework.http.HttpStatus;
                import org.springframework.http.MediaType;
                import org.springframework.http.ResponseEntity;
                import org.springframework.http.client.ClientHttpResponse;
                import org.springframework.http.server.reactive.ServerHttpResponse;
                import org.springframework.web.client.RestClientResponseException;
                import org.springframework.web.client.UnknownContentTypeException;
                import org.springframework.web.server.MethodNotAllowedException;
                import org.springframework.web.server.NotAcceptableStatusException;
                import org.springframework.web.server.ResponseStatusException;
                import org.springframework.web.server.UnsupportedMediaTypeStatusException;
                import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
                import org.springframework.web.util.UriComponentsBuilder;

                class Demo {
                    void entity(ResponseEntity entity) {
                        entity.getStatusCode();
                    }
                    void response(ClientHttpResponse response) {
                        response.getStatusCode();
                    }
                    void reactiveResponse(org.springframework.http.client.reactive.ClientHttpResponse response) {
                        response.getStatusCode();
                    }
                    void serverResponse(ServerHttpResponse response) {
                        response.getStatusCode();
                    }
                    void exception1(RestClientResponseException ex) {
                        ex.getStatusCode();
                    }
                    void unknownContentType(UnknownContentTypeException ex) {
                        ex.getStatusCode();
                    }
                    void methodNotAllowed(MethodNotAllowedException ex) {
                        ex.getHeaders();
                    }
                    void notAcceptable(NotAcceptableStatusException ex) {
                        ex.getHeaders();
                    }
                    void responseStatus(ResponseStatusException ex) {
                        ex.getHeaders();
                    }
                    void unsupportedMediaType(UnsupportedMediaTypeStatusException ex) {
                        ex.getHeaders();
                    }
                    void handler(ResponseStatusExceptionHandler handler) {
                        handler.determineStatus(null);
                    }
                    void uriBuilder(UriComponentsBuilder builder) {
                        builder.fromUriString("http://example.com");
                        builder.fromUriString("origin");
                    }
                    void mediaTypeConstants() {
                        String a = MediaType.APPLICATION_GRAPHQL_RESPONSE;
                        String b = MediaType.APPLICATION_GRAPHQL_RESPONSE_VALUE;
                        String c = MediaType.APPLICATION_JSON;
                        String d = MediaType.APPLICATION_JSON_VALUE;
                        String e = MediaType.APPLICATION_PROBLEM_JSON;
                        String f = MediaType.APPLICATION_PROBLEM_JSON_VALUE;
                        String g = MediaType.APPLICATION_NDJSON;
                        String h = MediaType.APPLICATION_NDJSON_VALUE;
                    }
                    void httpStatusConstants() {
                        HttpStatus a = HttpStatus.FOUND;
                        HttpStatus b = HttpStatus.PAYLOAD_TOO_LARGE;
                        HttpStatus c = HttpStatus.URI_TOO_LONG;
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebFluxModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.http.HttpMethod;
                import org.springframework.web.reactive.function.BodyInserters;
                import org.springframework.web.reactive.function.client.ClientRequest;
                import org.springframework.web.reactive.function.client.ClientResponse;
                import org.springframework.web.reactive.function.client.ExchangeStrategies;
                import org.springframework.web.reactive.function.client.WebClient;
                import org.springframework.web.reactive.function.client.WebClientResponseException;
                import org.springframework.web.reactive.function.server.ServerRequest;
                import org.springframework.web.reactive.function.server.ServerResponse;
                import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
                import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

                import java.net.URI;

                class Demo {
                    void bodyInserters() {
                        BodyInserters.fromObject("test");
                    }
                    void clientRequest(ClientRequest request) {
                        request.method(HttpMethod.GET, URI.create("https://arconia.io"));
                    }
                    void response(ClientResponse clientResponse, ServerResponse serverResponse) {
                        clientResponse.rawStatusCode();
                        serverResponse.rawStatusCode();
                    }
                    void webClient(WebClient client) {
                        client.mutate().exchangeStrategies(ExchangeStrategies.builder().build());
                        client.mutate().exchangeStrategies(s -> {});
                        client.post().syncBody(null);
                    }
                    void exception(WebClientResponseException ex) {
                        ex.getRawStatusCode();
                    }
                    void serverRequest(ServerRequest request) {
                        request.methodName();
                        request.pathContainer();
                    }
                    void serverResponseSync(ServerResponse response) {
                        ServerResponse.accepted().syncBody("test");
                    }
                    void websocketClient(ReactorNettyWebSocketClient client) {
                        client.getMaxFramePayloadLength();
                        client.getHandlePing();
                    }
                    void websocketServer(ReactorNettyRequestUpgradeStrategy strategy) {
                        strategy.getMaxFramePayloadLength();
                        strategy.getHandlePing();
                    }
                }
                """,
                """
                import org.springframework.http.HttpMethod;
                import org.springframework.web.reactive.function.BodyInserters;
                import org.springframework.web.reactive.function.client.ClientRequest;
                import org.springframework.web.reactive.function.client.ClientResponse;
                import org.springframework.web.reactive.function.client.ExchangeStrategies;
                import org.springframework.web.reactive.function.client.WebClient;
                import org.springframework.web.reactive.function.client.WebClientResponseException;
                import org.springframework.web.reactive.function.server.ServerRequest;
                import org.springframework.web.reactive.function.server.ServerResponse;
                import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
                import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

                import java.net.URI;

                class Demo {
                    void bodyInserters() {
                        BodyInserters.fromValue("test");
                    }
                    void clientRequest(ClientRequest request) {
                        request.create(HttpMethod.GET, URI.create("https://arconia.io"));
                    }
                    void response(ClientResponse clientResponse, ServerResponse serverResponse) {
                        clientResponse.statusCode();
                        serverResponse.statusCode();
                    }
                    void webClient(WebClient client) {
                        client.mutate().codecs(ExchangeStrategies.builder().build());
                        client.mutate().codecs(s -> {});
                        client.post().bodyValue(null);
                    }
                    void exception(WebClientResponseException ex) {
                        ex.getStatusCode();
                    }
                    void serverRequest(ServerRequest request) {
                        request.method();
                        request.requestPath();
                    }
                    void serverResponseSync(ServerResponse response) {
                        ServerResponse.accepted().bodyValue("test");
                    }
                    void websocketClient(ReactorNettyWebSocketClient client) {
                        client.getWebsocketClientSpec();
                        client.getWebsocketClientSpec();
                    }
                    void websocketServer(ReactorNettyRequestUpgradeStrategy strategy) {
                        strategy.getWebsocketServerSpec();
                        strategy.getWebsocketServerSpec();
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebMvcModule() {
        rewriteRun(
            //language=java
            java(
                """
                import jakarta.servlet.http.HttpServletRequest;
                import org.springframework.web.servlet.function.ServerRequest;
                import org.springframework.web.servlet.function.ServerResponse;
                import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
                import org.springframework.web.servlet.handler.MappedInterceptor;
                import org.springframework.web.servlet.i18n.CookieLocaleResolver;
                import org.springframework.web.servlet.i18n.SessionLocaleResolver;

                import java.lang.reflect.Method;
                import java.util.Comparator;
                import java.util.List;

                class Demo {
                    void serverRequest(ServerRequest request) {
                        request.methodName();
                        request.pathContainer();
                    }
                    void response(ServerResponse response) {
                        response.rawStatusCode();
                    }
                    void interceptor(MappedInterceptor interceptor) {
                        interceptor.getPathPatterns();
                    }
                    void cookieLocale(CookieLocaleResolver resolver) {
                        resolver.determineDefaultLocale(null);
                        resolver.determineDefaultTimeZone(null);
                    }
                    void sessionLocale(SessionLocaleResolver resolver) {
                        resolver.determineDefaultLocale(null);
                        resolver.determineDefaultTimeZone(null);
                    }

                    static class MyHandlerMethodMapping extends AbstractHandlerMethodMapping<Object> {
                        @Override
                        protected boolean isHandler(Class beanType) {
                            return false;
                        }

                        @Override
                        protected Object getMappingForMethod(Method method, Class handlerType) {
                            return null;
                        }

                        @Override
                        protected Object getMatchingMapping(Object mapping, HttpServletRequest request) {
                            return getMappingPathPatterns(mapping);
                        }

                        @Override
                        protected Comparator getMappingComparator(HttpServletRequest request) {
                            return null;
                        }
                    }
                }
                """,
                """
                import jakarta.servlet.http.HttpServletRequest;
                import org.springframework.web.servlet.function.ServerRequest;
                import org.springframework.web.servlet.function.ServerResponse;
                import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
                import org.springframework.web.servlet.handler.MappedInterceptor;
                import org.springframework.web.servlet.i18n.CookieLocaleResolver;
                import org.springframework.web.servlet.i18n.SessionLocaleResolver;

                import java.lang.reflect.Method;
                import java.util.Comparator;
                import java.util.List;

                class Demo {
                    void serverRequest(ServerRequest request) {
                        request.method();
                        request.requestPath();
                    }
                    void response(ServerResponse response) {
                        response.statusCode();
                    }
                    void interceptor(MappedInterceptor interceptor) {
                        interceptor.getIncludePathPatterns();
                    }
                    void cookieLocale(CookieLocaleResolver resolver) {
                        resolver.setDefaultLocaleFunction(null);
                        resolver.setDefaultTimeZoneFunction(null);
                    }
                    void sessionLocale(SessionLocaleResolver resolver) {
                        resolver.setDefaultLocaleFunction(null);
                        resolver.setDefaultTimeZoneFunction(null);
                    }

                    static class MyHandlerMethodMapping extends AbstractHandlerMethodMapping<Object> {
                        @Override
                        protected boolean isHandler(Class beanType) {
                            return false;
                        }

                        @Override
                        protected Object getMappingForMethod(Method method, Class handlerType) {
                            return null;
                        }

                        @Override
                        protected Object getMatchingMapping(Object mapping, HttpServletRequest request) {
                            return getDirectPaths(mapping);
                        }

                        @Override
                        protected Comparator getMappingComparator(HttpServletRequest request) {
                            return null;
                        }
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebSocketModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.http.HttpHeaders;
                import org.springframework.web.socket.WebSocketExtension;
                import org.springframework.web.socket.WebSocketHandler;
                import org.springframework.web.socket.WebSocketSession;
                import org.springframework.web.socket.client.AbstractWebSocketClient;
                import org.springframework.web.socket.client.WebSocketClient;
                import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
                import org.springframework.web.socket.messaging.WebSocketStompClient;
                import org.springframework.web.socket.sockjs.client.Transport;

                import java.net.URI;
                import java.util.List;
                import java.util.Map;
                import java.util.concurrent.CompletableFuture;

                class Demo {
                    void handshake(WebSocketClient client) {
                        client.doHandshake(null, (String) null, null);
                    }
                    void stats() {
                        var stats = new WebSocketMessageBrokerStats();
                        stats.getWebSocketSessionStatsInfo();
                        stats.getStompSubProtocolStatsInfo();
                        stats.getStompBrokerRelayStatsInfo();
                    }
                    void stomp(WebSocketStompClient client) {
                        client.connect(null, null);
                    }
                    void sockjs(Transport transport) {
                        transport.connect(null, null);
                    }

                    static class MyWebSocketClient extends AbstractWebSocketClient {
                        @Override
                        protected CompletableFuture<WebSocketSession> doHandshakeInternal(WebSocketHandler webSocketHandler, HttpHeaders headers, URI uri, List<String> subProtocols, List<WebSocketExtension> extensions, Map<String, Object> attributes) {
                            return null;
                        }
                    }
                }
                """,
                """
                import org.springframework.http.HttpHeaders;
                import org.springframework.web.socket.WebSocketExtension;
                import org.springframework.web.socket.WebSocketHandler;
                import org.springframework.web.socket.WebSocketSession;
                import org.springframework.web.socket.client.AbstractWebSocketClient;
                import org.springframework.web.socket.client.WebSocketClient;
                import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
                import org.springframework.web.socket.messaging.WebSocketStompClient;
                import org.springframework.web.socket.sockjs.client.Transport;

                import java.net.URI;
                import java.util.List;
                import java.util.Map;
                import java.util.concurrent.CompletableFuture;

                class Demo {
                    void handshake(WebSocketClient client) {
                        client.execute(null, (String) null, null);
                    }
                    void stats() {
                        var stats = new WebSocketMessageBrokerStats();
                        stats.getWebSocketSessionStats();
                        stats.getStompSubProtocolStats();
                        stats.getStompBrokerRelayStats();
                    }
                    void stomp(WebSocketStompClient client) {
                        client.connectAsync(null, null);
                    }
                    void sockjs(Transport transport) {
                        transport.connectAsync(null, null);
                    }

                    static class MyWebSocketClient extends AbstractWebSocketClient {
                        @Override
                        protected CompletableFuture<WebSocketSession> executeInternal(WebSocketHandler webSocketHandler, HttpHeaders headers, URI uri, List<String> subProtocols, List<WebSocketExtension> extensions, Map<String, Object> attributes) {
                            return null;
                        }
                    }
                }
                """
            )
        );
    }

}
