package org.example.workassistant.sdk.util;

import cn.hutool.core.util.IdUtil;

import java.util.UUID;

/**
 * ID生成工具类
 */
public final class IdUtils {

    private IdUtils() {
    }

    /**
     * 带-的36位的UUID
     *
     * @return UUID
     */
    public static String simple36UUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 不带-的32位的UUID
     *
     * @param upper 是否大写
     * @return UUID
     */
    public static String simple32UUID(boolean upper) {
        String uuid = simple32UUID();
        return upper ? uuid.toUpperCase() : uuid;
    }

    /**
     * 不带-的32位的UUID
     *
     * @return UUID
     */
    public static String simple32UUID() {
        final String rawUUID = simple36UUID();
        return rawUUID.substring(0, 8) + rawUUID.substring(9, 13) + rawUUID.substring(14, 18) + rawUUID.substring(19, 23) + rawUUID.substring(24);
    }

    public static String fastSimpleUUID() {
        return IdUtil.fastSimpleUUID();
    }
}
