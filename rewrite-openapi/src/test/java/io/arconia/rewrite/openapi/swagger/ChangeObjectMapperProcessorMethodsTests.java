package io.arconia.rewrite.openapi.swagger;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ChangeObjectMapperProcessorMethodsTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeObjectMapperProcessorMethods())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "swagger-integration-jakarta-2.2", "jackson-databind-2.20", "jackson-core-2.20"));
    }

    @Test
    @DocumentExample
    void returnsMapper() {
        rewriteRun(
                //language=java
                java(
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import com.fasterxml.jackson.databind.SerializationFeature;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class IndentingProcessor implements ObjectMapperProcessor {
                            @Override
                            public void processJsonObjectMapper(ObjectMapper mapper) {
                                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                            }

                            @Override
                            public void processOutputJsonObjectMapper(ObjectMapper mapper) {
                            }

                            @Override
                            public void processOutputYamlObjectMapper(ObjectMapper mapper) {
                                if (mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                                    return;
                                }
                                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                            }
                        }
                        """,
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import com.fasterxml.jackson.databind.SerializationFeature;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class IndentingProcessor implements ObjectMapperProcessor {
                            @Override
                            public ObjectMapper processJsonObjectMapper(ObjectMapper mapper) {
                                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                                return mapper;
                            }

                            @Override
                            public ObjectMapper processOutputJsonObjectMapper(ObjectMapper mapper) {
                                return mapper;
                            }

                            @Override
                            public ObjectMapper processOutputYamlObjectMapper(ObjectMapper mapper) {
                                if (mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                                    return mapper;
                                }
                                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                                return mapper;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removesYamlProcessorMethod() {
        rewriteRun(
                //language=java
                java(
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class LegacyProcessor implements ObjectMapperProcessor {
                            @Override
                            public void processJsonObjectMapper(ObjectMapper mapper) {
                                mapper.findAndRegisterModules();
                            }

                            @Override
                            public void processYamlObjectMapper(ObjectMapper mapper) {
                                mapper.findAndRegisterModules();
                            }
                        }
                        """,
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class LegacyProcessor implements ObjectMapperProcessor {
                            @Override
                            public ObjectMapper processJsonObjectMapper(ObjectMapper mapper) {
                                mapper.findAndRegisterModules();
                                return mapper;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void anonymousProcessor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class Processors {
                            ObjectMapperProcessor modules() {
                                return new ObjectMapperProcessor() {
                                    @Override
                                    public void processJsonObjectMapper(ObjectMapper mapper) {
                                        Runnable register = () -> {
                                            mapper.findAndRegisterModules();
                                            return;
                                        };
                                        register.run();
                                    }
                                };
                            }
                        }
                        """,
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class Processors {
                            ObjectMapperProcessor modules() {
                                return new ObjectMapperProcessor() {
                                    @Override
                                    public ObjectMapper processJsonObjectMapper(ObjectMapper mapper) {
                                        Runnable register = () -> {
                                            mapper.findAndRegisterModules();
                                            return;
                                        };
                                        register.run();
                                        return mapper;
                                    }
                                };
                            }
                        }
                        """
                )
        );
    }

    @Test
    void methodsOutsideProcessorUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import com.fasterxml.jackson.databind.ObjectMapper;
                        import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                        class Configurer {
                            ObjectMapperProcessor processor;

                            public void processJsonObjectMapper(ObjectMapper mapper) {
                                mapper.findAndRegisterModules();
                            }
                        }
                        """
                )
        );
    }

}
