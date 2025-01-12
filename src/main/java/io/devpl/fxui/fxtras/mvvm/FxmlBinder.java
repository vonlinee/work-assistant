package io.devpl.fxui.fxtras.mvvm;

import java.lang.annotation.*;

/**
 * 绑定FXML和Controller
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FxmlBinder {

    /**
     * 样式文件
     *
     * @return 样式文件路径
     */
    String style() default "";

    /**
     * FXML路径，如果使用IDEA，右键Copy Path/References Path -> From Source Root即为该值
     *
     * @return FXML相对路径
     */
    String location();

    /**
     * if this view is used in root Node of a stage, then the label value
     * will be used to set the title of the Stage
     *
     * @return the label of this View
     */
    String label() default "";
}
