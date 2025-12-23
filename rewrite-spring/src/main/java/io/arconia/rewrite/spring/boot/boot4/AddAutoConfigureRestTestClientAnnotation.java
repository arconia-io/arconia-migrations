package io.arconia.rewrite.spring.boot.boot4;

import java.util.Comparator;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * In Spring Boot 4, projects using RestTestClient in @SpringBootTest or @WebMvcTest
 * integration tests will need to explicitly add the @AutoConfigureTestRestTemplate
 * annotation.
 */
public class AddAutoConfigureRestTestClientAnnotation extends Recipe {

    private static final String AUTO_CONFIGURE_REST_TEST_CLIENT = "AutoConfigureRestTestClient";
    private static final String FQN_AUTO_CONFIGURE_REST_TEST_CLIENT = "org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient";
    private static final String FQN_SPRING_BOOT_TEST = "org.springframework.boot.test.context.SpringBootTest";
    private static final String FQN_WEB_MVC_TEST = "org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest";
    private static final String FQN_REST_TEST_CLIENT = "org.springframework.test.web.servlet.client.RestTestClient";

    private static final AnnotationMatcher AUTO_CONFIGURE_REST_TEST_CLIENT_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_AUTO_CONFIGURE_REST_TEST_CLIENT, true);
    private static final AnnotationMatcher SPRING_BOOT_TEST_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_SPRING_BOOT_TEST, true);
    private static final AnnotationMatcher WEB_MVC_TEST_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_WEB_MVC_TEST, true);

    @Override
    public String getDisplayName() {
        return "Add missing `@AutoConfigureRestTestClient` annotation";
    }

    @Override
    public String getDescription() {
        return "Add missing `@AutoConfigureRestTestClient` annotation to classes annotated with " +
                "`@SpringBootTest` or `@WebMvcTest` that use `RestTestClient`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(FQN_REST_TEST_CLIENT, false),
                new JavaIsoVisitor<>() {
                    @Override
                    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                        J.ClassDeclaration c = super.visitClassDeclaration(classDecl, ctx);
                        if (isApplicableClass(c)) {
                            c = addMissingAnnotation(c, ctx);
                        }
                        return c;
                    }

                    private J.ClassDeclaration addMissingAnnotation(J.ClassDeclaration c, ExecutionContext ctx) {
                        maybeAddImport(FQN_AUTO_CONFIGURE_REST_TEST_CLIENT);
                        return JavaTemplate.builder("@" + AUTO_CONFIGURE_REST_TEST_CLIENT)
                                .imports(FQN_AUTO_CONFIGURE_REST_TEST_CLIENT)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-boot-resttestclient-4.0"))
                                .build()
                                .apply(
                                        getCursor(),
                                        c.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName))
                                );
                    }

                    private boolean isApplicableClass(J.ClassDeclaration classDeclaration) {
                        // Check if the class is already annotated with @AutoConfigureRestTestClient
                        for (J.Annotation annotation : classDeclaration.getLeadingAnnotations()) {
                            JavaType.FullyQualified annotationType = TypeUtils.asFullyQualified(annotation.getType());
                            if (annotationType != null && AUTO_CONFIGURE_REST_TEST_CLIENT_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(annotationType)) {
                                // Already annotated with @AutoConfigureRestTestClient. No changes are needed.
                                return false;
                            }
                        }

                        // Check if the class is annotated with @SpringBootTest
                        for (J.Annotation annotation : classDeclaration.getLeadingAnnotations()) {
                            JavaType.FullyQualified aType = TypeUtils.asFullyQualified(annotation.getType());
                            if (aType != null) {
                                if (SPRING_BOOT_TEST_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(aType) ||
                                        WEB_MVC_TEST_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(aType)) {
                                    // Annotated, so we need to add @AutoConfigureRestTestClient.
                                    return true;
                                }
                            }
                        }

                        // Not annotated with @SpringBootTest or @WebMvcTest. No changes are needed.
                        return false;
                    }
                }
        );
    }

}
