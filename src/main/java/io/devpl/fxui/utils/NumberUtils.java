package io.devpl.fxui.utils;

/**
 * 数字工具类
 */
public class NumberUtils {

    /**
     * <p>Convert a {@code String} to a {@code Integer}, handling
     * hex (0xhhhh) and octal (0dddd) notations.
     * N.B. a leading zero means octal; spaces are not trimmed.</p>
     *
     * <p>Returns {@code null} if the string is {@code null}.</p>
     * @param str a {@code String} to convert, may be null
     * @return converted {@code Integer} (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Integer decode(final String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
        try {
            return Integer.decode(str);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    /**
     * 解析字符串为int类型,解析失败返回默认值
     * @param str          字符串
     * @param defaultValue 默认值
     * @return Integer
     */
    public static Integer parseInteger(final String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 解析字符串为int类型,解析失败返回默认值
     * @param str          字符串
     * @param defaultValue 默认值
     * @return int
     */
    public static int parseInt(final String str, int defaultValue) {
        return parseInteger(str, defaultValue);
    }
}
