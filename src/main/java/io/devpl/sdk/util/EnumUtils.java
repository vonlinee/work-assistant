package io.devpl.sdk.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class EnumUtils {

    /**
     * 获得枚举类中各枚举对象下指定字段的值
     *
     * @param clazz     枚举类
     * @param fieldName 字段名，最终调用getXXX方法
     * @return 字段值列表
     */
    public static List<Object> getFieldValues(Class<? extends Enum<?>> clazz, String fieldName) {
        if (null == clazz || StrUtil.isBlank(fieldName)) {
            return null;
        }
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final List<Object> list = new ArrayList<>(enums.length);
        for (Enum<?> e : enums) {
            list.add(ReflectUtil.getFieldValue(e, fieldName));
        }
        return list;
    }
}
