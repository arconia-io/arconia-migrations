package io.arconia.rewrite.test;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link ConvertToRawType}.
 */
class ConvertToRawTypeTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertToRawType("java.util.ArrayList"));
    }

    @Test
    @DocumentExample
    void convertParameterizedFieldToRawType() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<String> items;
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void convertWildcardTypeArgument() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<?> items;
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void preserveFinalModifierOnField() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            final ArrayList<?> items = new ArrayList<>();
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            final ArrayList items = new ArrayList();
                        }
                        """
                )
        );
    }

    @Test
    void convertDiamondInstantiation() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items = new ArrayList<>();
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items = new ArrayList();
                        }
                        """
                )
        );
    }

    @Test
    void convertExplicitInstantiation() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items = new ArrayList<String>();
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items = new ArrayList();
                        }
                        """
                )
        );
    }

    @Test
    void convertNestedParameterizedType() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<ArrayList<String>> nested;
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList nested;
                        }
                        """
                )
        );
    }

    @Test
    void convertMethodParameter() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            void process(ArrayList<?> items) {
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            void process(ArrayList items) {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertConstructorParameter() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            Demo(ArrayList<Long> list) {
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            Demo(ArrayList list) {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertMethodReturnType() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<?> getItems() {
                                return new ArrayList<>();
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList getItems() {
                                return new ArrayList();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void preserveStaticModifierOnMethod() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            static ArrayList<?> getItems() {
                                return new ArrayList<>();
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            static ArrayList getItems() {
                                return new ArrayList();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertLocalVariable() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            void process() {
                                ArrayList<?> items = new ArrayList<>();
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            void process() {
                                ArrayList items = new ArrayList();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertTypeCast() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<String> cast() {
                                return (ArrayList<String>) new ArrayList();
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList cast() {
                                return (ArrayList) new ArrayList();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertQualifiedName() {
        rewriteRun(
                //language=java
                java(
                        """
                        class Demo {
                            java.util.ArrayList<String> items;
                        }
                        """,
                        """
                        class Demo {
                            java.util.ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void removeUnusedTypeArgumentImport() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;
                        import java.util.Date;

                        class Demo {
                            ArrayList<Date> items;
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void keepImportWhenTypeArgumentStillUsedElsewhere() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;
                        import java.util.Date;

                        class Demo {
                            ArrayList<Date> items;
                            Date timestamp;
                        }
                        """,
                        """
                        import java.util.ArrayList;
                        import java.util.Date;

                        class Demo {
                            ArrayList items;
                            Date timestamp;
                        }
                        """
                )
        );
    }

    @Test
    void doNotConvertAlreadyRawType() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void doNotConvertOtherParameterizedTypes() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.LinkedList;
                        import java.util.List;

                        class Demo {
                            List<String> items;
                            LinkedList<Integer> numbers;
                        }
                        """
                )
        );
    }

    @Test
    void preservesFieldTargetedAnnotationWhenRawifying() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.lang.annotation.ElementType;
                        import java.lang.annotation.Target;
                        import java.util.ArrayList;

                        @Target(ElementType.FIELD)
                        @interface Container {}

                        class Demo {
                            @Container
                            ArrayList<String> items;
                        }
                        """,
                        """
                        import java.lang.annotation.ElementType;
                        import java.lang.annotation.Target;
                        import java.util.ArrayList;

                        @Target(ElementType.FIELD)
                        @interface Container {}

                        class Demo {
                            @Container
                            ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void preservesTypeUseAnnotationWhenRawifying() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.lang.annotation.ElementType;
                        import java.lang.annotation.Target;
                        import java.util.ArrayList;

                        @Target(ElementType.TYPE_USE)
                        @interface NonNull {}

                        class Demo {
                            @NonNull ArrayList<String> items;
                        }
                        """,
                        """
                        import java.lang.annotation.ElementType;
                        import java.lang.annotation.Target;
                        import java.util.ArrayList;

                        @Target(ElementType.TYPE_USE)
                        @interface NonNull {}

                        class Demo {
                            @NonNull ArrayList items;
                        }
                        """
                )
        );
    }

    @Test
    void rawifiesTargetTypeInSubclassExtendsClauseButLeavesSubclassReferencesAlone() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        public class SpecialList<T> extends ArrayList<T> {
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        public class SpecialList<T> extends ArrayList {
                        }
                        """
                ),
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            SpecialList<String> specialItems;
                            ArrayList<String> rawItems;
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            SpecialList<String> specialItems;
                            ArrayList rawItems;
                        }
                        """
                )
        );
    }

}
