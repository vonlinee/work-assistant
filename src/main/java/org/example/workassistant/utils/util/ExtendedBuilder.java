package org.example.workassistant.utils.util;

/**
 * 对Builder的扩展
 *
 * @param <T> Builder最终build的类型
 * @param <B> Builder的子类型
 */
public interface ExtendedBuilder<T, B extends Builder<T>> extends Builder<T> {

    /**
     * 构造指定的子类型，为了提供一种向下转型的能力，不通过强制转换
     * 由于泛型边界的限制，需要由子类实现进行层层变换
     *
     * @param type Class<V> 类型T的子类型对应的Class对象
     * @param <V>  类型T的子类型V
     * @return V
     */
    @SuppressWarnings("unchecked")
    default <V extends T> V build(Class<V> type) {
        return (V) build();
    }

    /**
     * 获取Builder的实际类型，即Builder的子类
     *
     * @return Builder子类
     */
    @SuppressWarnings("unchecked")
    default B getThis() {
        return (B) this;
    }
}
