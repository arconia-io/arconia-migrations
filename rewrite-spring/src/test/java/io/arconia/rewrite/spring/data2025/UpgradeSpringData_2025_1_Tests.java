package io.arconia.rewrite.spring.data2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

/**
 * Unit tests for "io.arconia.rewrite.spring.data2025.UpgradeSpringData_2025_1".
 */
class UpgradeSpringData_2025_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.data2025.UpgradeSpringData_2025_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-data-commons-3.5", "spring-data-jdbc-3.5", "spring-data-cassandra-4.5",
                        "spring-data-mongodb-4.5", "reactive-streams"));
    }

    @Test
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.data:spring-data-jpa:3.5.0"
                            implementation "org.springframework.data:spring-data-mongodb:4.5.0"
                            implementation "org.springframework.data:spring-data-rest-webmvc:4.5.0"
                            implementation "org.springframework.data:spring-data-neo4j:7.5.0"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .containsPattern("spring-data-jpa:4\\.0\\.\\d+")
                                    .containsPattern("spring-data-mongodb:5\\.0\\.\\d+")
                                    .containsPattern("spring-data-rest-webmvc:5\\.0\\.\\d+")
                                    .containsPattern("spring-data-neo4j:8\\.0\\.\\d+");
                            return actual;
                        })
                )
        );
    }

    @Test
    @DocumentExample
    void commonsTypesAndMethods() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.data.annotation.PersistenceConstructor;
                        import org.springframework.data.mapping.PersistentEntity;
                        import org.springframework.data.mapping.PersistentProperty;
                        import org.springframework.data.mapping.PersistentPropertyPath;
                        import org.springframework.data.mapping.PropertyPath;
                        import org.springframework.data.util.TypeInformation;

                        class Person {

                            @PersistenceConstructor
                            Person(String name) {
                            }

                            PropertyPath path = PropertyPath.from("name", Person.class);
                            TypeInformation<Person> type = null;

                            boolean creatorArgument(PersistentEntity<?, ?> entity, PersistentProperty<?> property) {
                                return entity.isConstructorArgument(property);
                            }

                            PersistentProperty<?> leaf(PersistentPropertyPath<PersistentProperty<?>> path) {
                                return path.getRequiredLeafProperty();
                            }
                        }
                        """,
                        """
                        import org.springframework.data.annotation.PersistenceCreator;
                        import org.springframework.data.core.PropertyPath;
                        import org.springframework.data.core.TypeInformation;
                        import org.springframework.data.mapping.PersistentEntity;
                        import org.springframework.data.mapping.PersistentProperty;
                        import org.springframework.data.mapping.PersistentPropertyPath;

                        class Person {

                            @PersistenceCreator
                            Person(String name) {
                            }

                            PropertyPath path = PropertyPath.from("name", Person.class);
                            TypeInformation<Person> type = null;

                            boolean creatorArgument(PersistentEntity<?, ?> entity, PersistentProperty<?> property) {
                                return entity.isCreatorArgument(property);
                            }

                            PersistentProperty<?> leaf(PersistentPropertyPath<PersistentProperty<?>> path) {
                                return path.getLeafProperty();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void jdbcAndCassandraTypes() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.data.cassandra.config.CassandraCqlSessionFactoryBean;
                        import org.springframework.data.cassandra.core.cql.legacy.AsyncCqlTemplate;
                        import org.springframework.data.cassandra.core.legacy.AsyncCassandraOperations;
                        import org.springframework.data.cassandra.core.mapping.BasicCassandraMappingContext;
                        import org.springframework.data.jdbc.repository.QueryMappingConfiguration;

                        class DataConfig {
                            QueryMappingConfiguration mapping = null;
                            AsyncCqlTemplate cqlTemplate = null;
                            AsyncCassandraOperations operations = null;
                            BasicCassandraMappingContext mappingContext = null;
                            CassandraCqlSessionFactoryBean sessionFactory = null;
                        }
                        """,
                        """
                        import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
                        import org.springframework.data.cassandra.core.AsyncCassandraOperations;
                        import org.springframework.data.cassandra.core.cql.AsyncCqlTemplate;
                        import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
                        import org.springframework.data.jdbc.core.convert.QueryMappingConfiguration;

                        class DataConfig {
                            QueryMappingConfiguration mapping = null;
                            AsyncCqlTemplate cqlTemplate = null;
                            AsyncCassandraOperations operations = null;
                            CassandraMappingContext mappingContext = null;
                            CqlSessionFactoryBean sessionFactory = null;
                        }
                        """
                )
        );
    }

    @Test
    void mongoMetaAllowDiskUse() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.List;

                        import org.springframework.data.mongodb.repository.Meta;
                        import org.springframework.data.repository.Repository;

                        interface PersonRepository extends Repository<Object, String> {

                            @Meta(allowDiskUse = true, maxExecutionTimeMs = 1000)
                            List<Object> findByName(String name);
                        }
                        """,
                        """
                        import java.util.List;

                        import org.springframework.data.mongodb.repository.Meta;
                        import org.springframework.data.repository.Repository;

                        interface PersonRepository extends Repository<Object, String> {

                            @Meta(allowDiskUse = "true", maxExecutionTimeMs = 1000)
                            List<Object> findByName(String name);
                        }
                        """
                )
        );
    }

    @Test
    void jpaNativeParserPropertyFlagged() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.data.jpa.query.native.parser=regex
                        """,
                        """
                        # Removed in Spring Data JPA 4.0. Configure the query enhancer through the queryEnhancerSelector attribute of @EnableJpaRepositories instead.
                        # spring.data.jpa.query.native.parser=regex
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
