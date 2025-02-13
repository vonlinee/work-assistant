package org.example.workassistant.utils.util;

import cn.hutool.core.util.RandomUtil;

public class RandomUtils {
    /**
     * 获得一个只包含数字的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomNumbers(final int length) {
        return RandomUtil.randomString(RandomUtil.BASE_NUMBER, length);
    }
}
