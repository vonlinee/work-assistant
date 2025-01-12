package io.devpl.fxui.components.table;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableViewModel {

    /**
     * @return 1 => CONSTRAINED, 0 -> UNCONSTRAINED
     * @see javafx.scene.control.TableView#CONSTRAINED_RESIZE_POLICY
     * @see javafx.scene.control.TableView#UNCONSTRAINED_RESIZE_POLICY
     */
    int resizePolicy() default 1;

    /**
     * 是否对字段按order进行排序
     *
     * @return 默认false，按字段声明顺序作为顺序
     */
    boolean orderFields() default false;

    /**
     * 是否可编辑
     *
     * @return 默认true
     */
    boolean editable() default true;

    /**
     * 选择模式，单选或多选
     *
     * @return 0-单选，默认 1-多选
     */
    int selectionModel() default 0;
}
