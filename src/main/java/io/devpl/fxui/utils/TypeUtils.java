package io.devpl.fxui.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 类型判断工具类
 */
public class TypeUtils {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern PATTERN_INTEGER = Pattern.compile("^[-+]?[\\d]*$");
    private static final Pattern PATTERN_DOUBLE = Pattern.compile("^[-+]?[.\\d]*$");
    private static final String[] parsePatterns = {"yyyy-MM-dd", "yyyy年MM月dd日", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyyMMdd"};

    /**
     * 将输入字符串转换为响应的数据类型
     * @param input        输入
     * @param requiredType 目标数据类型
     * @return 目标数据类型对应的值
     */
    public static Object parseString(String input, Class<?> requiredType) throws Exception {
        boolean nullOrEmpty = input == null || input.length() == 0;
        if (double.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Double.parseDouble(input);
        } else if (long.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Long.parseLong(input);
        } else if (int.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Integer.parseInt(input);
        } else if (float.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Float.parseFloat(input);
        } else if (short.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Short.parseShort(input);
        } else if (boolean.class.equals(requiredType)) {
            return nullOrEmpty ? 0 : Boolean.parseBoolean(input);
        } else if (Number.class.isAssignableFrom(requiredType)) {
            return requiredType.getConstructor(String.class).newInstance(nullOrEmpty ? "0" : input);
        } else {
            return nullOrEmpty ? "" : requiredType.getConstructor(String.class).newInstance(input);
        }
    }

    public static boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        return PATTERN_INTEGER.matcher(str).matches();
    }

    public static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        return PATTERN_DOUBLE.matcher(str).matches();
    }

    public static Date parseDate(String string) {
        if (string == null) {
            return null;
        }
        for (String parsePattern : parsePatterns) {
            try {
                return new SimpleDateFormat(parsePattern).parse(string);
            } catch (ParseException e) {
                System.out.println(e.getErrorOffset());
            }
        }
        return null;
    }

    /**
     * 判断输入字符串是否为整数或者浮点数，涵盖负数的情况
     * @param str 字符串
     * @return 是否是数字
     */
    public static boolean isNumeric(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 字符串是否是日期格式
     * @param str 输入
     * @return 是否是日期格式
     */
    public static boolean isDateTime(String str) {
        DateFormat df = new SimpleDateFormat();
        df.setLenient(false);
        try {
            df.parse(str);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * 字符串是否是布尔值
     * @param input 输入
     * @return 是否是布尔值
     */
    public static boolean isBoolean(String input) {
        if (input == null) {
            return false;
        }
        return "true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input);
    }
}

