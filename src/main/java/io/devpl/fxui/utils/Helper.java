package io.devpl.fxui.utils;

import io.devpl.sdk.util.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Helper {

    /**
     * 调用Object的toString方法，不管子类是否重写toString
     *
     * @param obj obj
     * @return obj toString
     * @see Object#toString()
     */
    public static String objectToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
    }

    public static <T> T whenNull(T val, T defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    public static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    public static void println(String msg, Object... args) {
        System.out.printf((msg) + "%n", args);
    }

    public static void println(Object... args) {
        for (Object arg : args) {
            System.out.print(arg);
            System.out.print(" ");
        }
        System.out.print("\n");
    }

    /**
     * getConstructor 方法入参是可变长参数列表，对应类中构造方法的入参类型，这里使用无参构造。
     * newInstance 返回的是泛型 T，取决于 clazz 的类型 Class<T>。这里直接用 Object 接收了。
     * 调用默认方法创建对象实例
     *
     * @param clazz Class对象
     * @return 创建的对象实例
     */
    public static <T> T instantiate(Class<T> clazz) throws RuntimeException {
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("failed to instantiate class " + clazz + " cause:", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("failed to instantiate class " + clazz + " cause: no default constructor in Class[" + clazz + "]", e);
        }
    }

    /**
     * 下划线字符
     */
    public static final char UNDERLINE = '_';

    private Helper() {
    }

    /**
     * convert string from slash style to camel style, such as my_course will convert to MyCourse
     *
     * @param str 数据库字符串
     * @return
     */
    public static String dbStringToCamelStyle(String str) {
        if (str == null) {
            return null;
        }
        if (str.contains("_")) {
            str = str.toLowerCase();
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(str.charAt(0)).toUpperCase());
            for (int i = 1; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c != '_') {
                    sb.append(c);
                } else {
                    if (i + 1 < str.length()) {
                        sb.append(String.valueOf(str.charAt(i + 1)).toUpperCase());
                        i++;
                    }
                }
            }
            return sb.toString();
        }
        String firstChar = String.valueOf(str.charAt(0)).toUpperCase();
        String otherChars = str.substring(1);
        return firstChar + otherChars;
    }

    /**
     * 字符串下划线转驼峰格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String underlineToCamel(String param) {
        if (StringUtils.hasText(param)) {
            return "";
        }
        String temp = param.toLowerCase();
        int len = temp.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = temp.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(temp.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 首字母大写(进行字母的ascii编码前移，效率是最高的)
     *
     * @param fieldName 需要转化的字符串
     */
    public static String upperFirst(String fieldName) {
        char[] chars = fieldName.toCharArray();
        chars[0] = CharacterUtils.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    /**
     * 调用notepad编辑器编辑文件
     *
     * @param file 文件
     */
    public static void edit(File file) {
        try {
            Runtime.getRuntime().exec("notepad " + file.getAbsolutePath());
        } catch (IOException e) {
            // ignore
        }
    }

    public static void show(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int defaults(Integer num, int defaultValue) {
        if (num == null) {
            return defaultValue;
        }
        return num;
    }
}
