package io.devpl.fxui.utils;

public class CharacterUtils {

    /**
     * 字符转成大写
     * @param c 需要转化的字符
     */
    public static char toUpperCase(char c) {
        if (97 <= c && c <= 122) {
            c ^= 32;
        }
        return c;
    }
}
