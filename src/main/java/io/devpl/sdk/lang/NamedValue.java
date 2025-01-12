package io.devpl.sdk.lang;

/**
 * 枚举类型
 *
 * @see Enum
 */
public interface NamedValue<K, V> {

    /**
     * 名称
     *
     * @return 名称
     */
    K getName();

    V getValue();
}
