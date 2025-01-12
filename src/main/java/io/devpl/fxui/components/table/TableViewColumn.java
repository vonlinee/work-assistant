package io.devpl.fxui.components.table;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableViewColumn {

    String field() default "";

    String title();

    int order() default 0;

    boolean sortable() default false;

    double width() default -1;

    double minWidth() default -1;

    double maxWidth() default -1;

    boolean editable() default false;
}
