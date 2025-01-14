package org.example.workassistant.sdk.util;

import org.springframework.cglib.core.ReflectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mapstruct 工具类
 * <p>参考文档：<a href="https://mapstruct.plus/introduction/quick-start.html">mapstruct-plus</a></p>
 *

 */
public abstract class MapstructUtils {

    /**
     * 将 T 类型对象，转换为 desc 类型的对象并返回
     *
     * @param source 数据来源实体
     * @param desc   描述对象 转换后的对象
     * @return desc
     */
    @SuppressWarnings("unchecked")
    public static <T, V> V convert(T source, Class<V> desc) {
        if (source == null || desc == null) {
            return null;
        }
        Object target = ReflectUtils.newInstance(desc);
        BeanUtils.copyProperties(source, target);
        return (V) target;
    }

    /**
     * 将 T 类型对象，按照配置的映射字段规则，给 desc 类型的对象赋值并返回 desc 对象
     *
     * @param source 数据来源实体
     * @param desc   转换后的对象
     * @return desc
     */
    public static <T, V> V convert(T source, V desc) {
        if (source == null || desc == null) {
            return null;
        }
        BeanUtils.copyProperties(source, desc);
        return desc;
    }

    /**
     * 将 T 类型的集合，转换为 desc 类型的集合并返回
     *
     * @param sourceList 数据来源实体列表
     * @param desc       描述对象 转换后的对象
     * @return desc
     */
    @SuppressWarnings("unchecked")
    public static <T, V> List<V> convert(List<T> sourceList, Class<V> desc) {
        if (sourceList == null || desc == null) {
            return Collections.emptyList();
        }
        List<V> targets = new ArrayList<>(sourceList.size());
        for (T source : sourceList) {
            Object target = ReflectUtils.newInstance(desc);
            BeanUtils.copyProperties(source, target);
            targets.add((V) target);
        }
        return targets;
    }

    /**
     * 将 Map 转换为 beanClass 类型的集合并返回
     *
     * @param map       数据来源
     * @param beanClass bean类
     * @return bean对象
     */
    public static <T> T convert(Map<String, Object> map, Class<T> beanClass) {
        if (CollectionUtils.isEmpty(map) || beanClass == null) {
            return null;
        }
        return BeanUtils.toBean(map, beanClass);
    }
}
