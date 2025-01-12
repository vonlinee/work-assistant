package io.devpl.sdk.lang;

import java.lang.annotation.*;

/**
 * 声明泛型参数，应用于辅助判断泛型类型，解决类型擦除
 * 需要自定义对应的处理逻辑
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(value = RetentionPolicy.SOURCE)
public @interface GenericDefinition {

    /**
     * 泛型值
     *
     * @return 泛型类型
     */
    Class<?> value();

    /**
     * 辅助定义数组的泛型
     *
     * @return 位置索引
     */
    int index() default -1;

    /**
     * 辅助定义Map等泛型参数
     *
     * @return Map的Key
     */
    String key() default "";
}
