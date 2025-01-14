package org.example.workassistant.fxui.controller.fields;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 命名风格枚举
 */
public enum CaseFormat implements NamingStrategy {

    /**
     * 大写：所有字母全大写，非字母不变
     */
    UPPER() {

        /**
         * 是否为大写命名
         */
        final Pattern CAPITAL_MODE = Pattern.compile("^[0-9A-Z/_]+$");

        /**
         * 所有字母全大写
         * @param source 源字符串
         * @return 大写
         */
        @Override
        public String normalize(String source) {
            if (source == null) {
                return "";
            }
            return source.toUpperCase();
        }

        @Override
        public boolean matches(String source) {
            if (source == null || source.isBlank()) {
                return false;
            }
            char[] charArray = source.toCharArray();
            for (char c : charArray) {
                if (!Character.isUpperCase(c)) {
                    return false;
                }
            }
            return true;
        }
    },

    /**
     * 所有字母全小写，非字母不变
     */
    LOWER() {
        /**
         * Returns a copy of the input string in which all {@linkplain Character#isUpperCase(char) uppercase ASCII
         * characters} have been converted to lowercase. All other characters are copied without
         * modification.
         */
        @Override
        public String normalize(String source) {
            int length = source.length();
            for (int i = 0; i < length; i++) {
                if (Character.isUpperCase(source.charAt(i))) {
                    char[] chars = source.toCharArray();
                    for (; i < length; i++) {
                        char c = chars[i];
                        if (Character.isUpperCase(c)) {
                            chars[i] = (char) (c ^ CASE_MASK);
                        }
                    }
                    return String.valueOf(chars);
                }
            }
            return source;
        }

        /**
         * 所有字母全小写，非字母不变
         * @param source 字符串
         * @return 是否所有字母全小写
         */
        @Override
        public boolean matches(String source) {
            if (source == null) {
                return false;
            }
            char[] charArray = source.toCharArray();
            for (char c : charArray) {
                if (!Character.isLowerCase(c)) {
                    return false;
                }
            }
            return true;
        }
    },

    /**
     * 连字符形式
     * Hyphenated variable naming convention, e.g., "lower-hyphen".
     */
    LOWER_HYPHEN() {
        @Override
        public String normalize(String source) {
            return toDashCase(source);
        }

        @Override
        public boolean matches(String source) {
            return isDashCase(source);
        }

        @Override
        public String convert(CaseFormat fromStyle, String source) {
            if (fromStyle == LOWER_UNDERSCORE) {
                return source.replace('-', '_');
            }
            if (fromStyle == UPPER_UNDERSCORE) {
                return UPPER.normalize(source.replace('-', '_'));
            }
            return super.convert(fromStyle, source);
        }
    },

    /**
     * 首字母大写
     */
    CAPITAL_FIRST() {

        /**
         * 是否为大写命名
         */
        final Pattern CAPITAL_MODE = Pattern.compile("^[0-9A-Z/_]+$");

        @Override
        public String normalize(String source) {
            if (source == null || source.isEmpty()) {
                return source;
            }
            return Character.toUpperCase(source.charAt(0)) + source.substring(1);
        }

        @Override
        public boolean matches(String source) {
            if (source == null || source.isBlank()) {
                return false;
            }
            return CAPITAL_MODE.matcher(source).matches();
        }
    },

    /**
     * 首字母大写，其他均小写
     */
    CAPITAL_FIRST_ONLY() {
        @Override
        public String normalize(String source) {
            if (source == null || source.isBlank()) {
                return "";
            }
            return Character.toUpperCase(source.charAt(0)) + LOWER.normalize(source.substring(1));
        }

        @Override
        public boolean matches(String source) {
            // 判断字符串是否为空或者长度为0
            if (source == null || source.isEmpty()) {
                return false;
            }
            // 判断首字母是否大写
            if (!Character.isUpperCase(source.charAt(0))) {
                return false;
            }
            // 判断其余字母是否全部小写
            for (int i = 1; i < source.length(); i++) {
                if (!Character.isLowerCase(source.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    },

    /**
     * 驼峰形式(不管大小写)
     */
    CAMEL(Pattern.compile(".*[A-Z]+.*")) {
        /**
         * 首字母大写，则为大驼峰，首字母小写则为小驼峰
         * @param source 源字符串
         * @return 驼峰形式的字符串
         */
        @Override
        public String normalize(String source) {
            return toCamelCase(source);
        }

        @Override
        public boolean matches(String source) {
            if (this.pattern == null) {
                return false;
            }
            return this.pattern.matcher(source).matches();
        }
    },

    /**
     * 驼峰和下划线混合
     */
    CAMEL_UNDERLINE_MIXED() {
        @Override
        public String normalize(String source) {
            return toCamelSnakeCase(source);
        }

        /**
         * 驼峰或者下划线混合
         * @param source 字符串
         * @return 是否同时包含驼峰和下划线
         */
        @Override
        public boolean matches(String source) {
            return CAMEL.matches(source) && UNDERSCORE.matches(source);
        }
    },

    /**
     * 首字母小写
     */
    LOWER_FIRST() {
        @Override
        public String normalize(String source) {
            if (source == null) {
                return "";
            }
            return source.substring(0, 1).toLowerCase() + source.substring(1);
        }

        @Override
        public boolean matches(String source) {
            if (source == null || source.isBlank()) {
                return false;
            }
            return Character.isLowerCase(source.charAt(0));
        }
    },

    /**
     * 下划线，不管大小写
     */
    UNDERSCORE(Pattern.compile(".*[/_]+.*")) {
        @Override
        public String normalize(String source) {
            return toUnderscore(source);
        }

        @Override
        public boolean matches(String source) {
            if (this.pattern == null) {
                return false;
            }
            return this.pattern.matcher(source).matches();
        }
    },

    /**
     * 小写下划线
     */
    LOWER_UNDERSCORE() {
        @Override
        public String normalize(String source) {
            StringBuilder result = new StringBuilder();
            result.append(Character.toLowerCase(source.charAt(0)));
            for (int i = 1; i < source.length(); i++) {
                char c = source.charAt(i);
                if (Character.isUpperCase(c)) {
                    result.append('_').append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }

        /**
         * @param source 字符串
         * @return 字符串是否符合小写下划线风格
         */
        @Override
        public boolean matches(String source) {
            return isLowerCaseUnderscore(source);
        }
    },

    /**
     * 大写下划线
     */
    UPPER_UNDERSCORE() {
        @Override
        public String normalize(String source) {
            return LOWER_UNDERSCORE.normalize(source).toUpperCase(Locale.US);
        }

        @Override
        public boolean matches(String source) {
            return isUpperCaseUnderscore(source);
        }
    };

    /**
     * A bit mask which selects the bit encoding ASCII character case.
     */
    private static final char CASE_MASK = 0x20;
    /**
     * 正则匹配，可为null
     */
    final Pattern pattern;

    CaseFormat() {
        this.pattern = null;
    }

    CaseFormat(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
     * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isUpperCase(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    /**
     * 下划线转驼峰
     *
     * @param name 待转内容
     */
    public static String underlineToCamel(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String tempName = name;
        // 大写数字下划线组成转为小写 , 允许混合模式转为小写
        if (CAPITAL_FIRST.matches(name) || CAMEL_UNDERLINE_MIXED.matches(name)) {
            tempName = name.toLowerCase();
        }
        StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        String[] camels = tempName.split("_");
        // 跳过原始字符串中开头、结尾的下换线或双重下划线
        // 处理真正的驼峰片段
        Arrays.stream(camels).filter(camel -> !name.isBlank()).forEach(camel -> {
            if (result.isEmpty()) {
                // 第一个驼峰片段，首字母都小写
                result.append(LOWER_FIRST.normalize(camel));
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(capitalFirst(camel));
            }
        });
        return result.toString();
    }

    /**
     * 去掉指定的前缀
     *
     * @param name   表名
     * @param prefix 前缀
     * @return 转换后的字符串
     */
    public static String removePrefix(String name, Set<String> prefix) {
        if (name == null || name.isBlank()) {
            return "";
        }
        // 判断是否有匹配的前缀，然后截取前缀
        return prefix.stream().filter(pf -> name.toLowerCase().startsWith(pf.toLowerCase())).findFirst().map(pf -> name.substring(pf.length())).orElse(name);
    }

    /**
     * 去掉下划线前缀并转成驼峰格式
     *
     * @param name   表名
     * @param prefix 前缀
     * @return 转换后的字符串
     */
    public static String removePrefixAndCamel(String name, Set<String> prefix) {
        return underlineToCamel(removePrefix(name, prefix));
    }

    // ================================ 静态工具方法 ===============================================

    /**
     * 去掉指定的后缀
     *
     * @param name   表名
     * @param suffix 后缀
     * @return 转换后的字符串
     */
    public static String removeSuffix(String name, Set<String> suffix) {
        if (name == null || name.isBlank()) {
            return "";
        }
        // 判断是否有匹配的后缀，然后截取后缀
        return suffix.stream().filter(sf -> name.toLowerCase().endsWith(sf.toLowerCase())).findFirst().map(sf -> name.substring(0, name.length() - sf.length())).orElse(name);
    }

    /**
     * 去掉下划线后缀并转成驼峰格式
     *
     * @param name   表名
     * @param suffix 后缀
     * @return 转换后的字符串
     */
    public static String removeSuffixAndCamel(String name, Set<String> suffix) {
        return underlineToCamel(removeSuffix(name, suffix));
    }

    /**
     * 首字母大写
     *
     * @param name 待转换的字符串
     * @return 转换后的字符串
     */
    public static String capitalFirst(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 转换成驼峰形式
     *
     * @param str 字符串
     * @return 驼峰形式字符串
     */
    public static String toCamelCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean capitalize = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isWhitespace(ch) || ch == '_' || ch == '-') {
                capitalize = true;
            } else if (capitalize) {
                result.append(Character.toUpperCase(ch));
                capitalize = false;
            } else {
                result.append(Character.toLowerCase(ch));
            }
        }
        return result.toString();
    }

    /**
     * 转换成驼峰下划线形式
     *
     * @param str 字符串
     * @return 驼峰下划线形式混合字符串
     */
    public static String toCamelSnakeCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean capitalize = false;
        boolean addUnderscore = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isWhitespace(ch)) {
                capitalize = true;
                addUnderscore = true;
            } else if (ch == '_') {
                addUnderscore = true;
            } else if (addUnderscore) {
                result.append("_");
                result.append(Character.toUpperCase(ch));
                capitalize = false;
                addUnderscore = false;
            } else if (capitalize) {
                result.append(Character.toUpperCase(ch));
                capitalize = false;
            } else {
                result.append(Character.toLowerCase(ch));
            }
        }
        if (addUnderscore) {
            result.deleteCharAt(result.length() - 1); // 删除最后一个多余的下划线
        }
        return result.toString();
    }

    /**
     * 判断字符串是否为大写字母下划线形式
     * 函数遍历字符串的每个字符，如果遇到大写字母，则检查之前是否已经出现过下划线，如果出现过，则返回false。
     * 如果遇到下划线，则检查之前是否已经出现过下划线，如果出现过，则返回false。如果遇到其他字符，则返回false。
     * 最后，如果字符串没有返回false，则返回true，表示字符串为大写字母下划线形式。
     *
     * @param str 字符串
     * @return 是否为大写字母下划线形式
     */
    public static boolean isUpperCaseUnderscore(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean hasUnderscore = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (hasUnderscore) {
                    return false;
                }
            } else if (ch == '_') {
                if (hasUnderscore) {
                    return false;
                }
                hasUnderscore = true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 这个函数接受一个字符串参数str，并返回一个布尔值，表示该字符串是否为小写字母下划线形式。如果字符串为空或长度为0，则返回false。
     * 然后，函数遍历字符串的每个字符，如果遇到小写字母，则检查之前是否已经出现过下划线，如果出现过，则返回false。
     * 如果遇到下划线，则检查之前是否已经出现过下划线，如果出现过，则返回false。如果遇到其他字符，则返回false。
     * 最后，如果字符串没有返回false，则返回true，表示字符串为小写字母下划线形式。
     *
     * @param str 字符串
     * @return 是否小写下划线形式
     */
    public static boolean isLowerCaseUnderscore(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean hasUnderscore = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isLowerCase(ch)) {
                if (hasUnderscore) {
                    return false;
                }
            } else if (ch == '_') {
                if (hasUnderscore) {
                    return false;
                }
                hasUnderscore = true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 这个函数接受一个字符串参数str，并返回一个下划线形式的字符串。如果输入字符串为空或长度为0，则返回空字符串。
     * 然后，函数遍历输入字符串的每个字符，如果遇到大写字母，则将其转换为小写字母，并在其前面添加一个下划线。
     * 最后，将所有字符连接起来并返回结果字符串。
     *
     * @param str 源字符串
     * @return 下划线形式字符串
     */
    public static String toUnderscore(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean capitalize = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                capitalize = true;
            } else if (capitalize) {
                result.append("_");
                capitalize = false;
            }
            result.append(ch);
        }
        return result.toString();
    }

    /**
     * 这个函数接受一个字符串参数str，并返回一个连字符形式的字符串。如果输入字符串为空或长度为0，则返回空字符串。
     * 然后，函数遍历输入字符串的每个字符，如果遇到大写字母，则将其转换为小写字母，并在其前面添加一个连字符。
     * 如果遇到小写字母或空格，则不进行任何操作。如果遇到其他字符，则将其添加到结果字符串中。最后，返回结果字符串。
     *
     * @param str 源字符串
     * @return 连字符形式字符串
     */
    public static String toDashCase(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean capitalize = false;
        boolean isFirstLetter = true;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (!isFirstLetter) {
                    result.append("-");
                }
                isFirstLetter = false;
                capitalize = true;
            } else if (Character.isLowerCase(ch)) {
                if (capitalize) {
                    result.append("-");
                }
                capitalize = false;
            } else {
                if (isFirstLetter) {
                    result.append("-");
                }
                isFirstLetter = false;
            }
            result.append(ch);
        }
        return result.toString();
    }

    /**
     * 这个函数接受一个字符串参数str，并返回一个布尔值，表示该字符串是否为连字符形式。
     * 如果输入字符串为空或长度为0，则返回false。然后，函数遍历输入字符串的每个字符，如果遇到连字符，
     * 则检查之前是否已经出现过字母，如果出现过，则返回false。如果遇到字母，则检查之前是否已经出现过连字符，
     * 如果出现过，则返回false。如果遇到其他字符，则返回false。
     * 最后，如果字符串没有返回false，则返回true，表示字符串为连字符形式。
     *
     * @param str 字符串
     * @return 是否为连字符形式
     */
    public static boolean isDashCase(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean hasDash = false;
        boolean hasLetter = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (ch == '-') {
                if (hasLetter) {
                    return false;
                }
                hasDash = true;
            } else if (Character.isLetter(ch)) {
                if (hasDash) {
                    return false;
                }
                hasLetter = true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符串驼峰转下划线格式
     *
     * @param source 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String camelToUnderline(String source) {
        if (source == null || source.isBlank()) {
            return "_";
        }
        int len = source.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = source.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 将下划线方式命名的字符串转换为帕斯卡式。<br>
     * 规则为：
     * 单字之间不以空格或任何连接符断开
     * 第一个单字首字母采用大写字母
     * 后续单字的首字母亦用大写字母
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。<br>
     * 例如：hello_world=》HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toPascalCase(String name) {
        return upperFirst(toCamelCase(name));
    }

    /**
     * 大写首字母<br>
     * 例如：str = name, return Name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String upperFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (!str.isEmpty()) {
            char firstChar = str.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + str.subSequence(1, str.length()).toString();
            }
        }
        return str.toString();
    }

    /**
     * 将目标字符串转换成当前命名风格
     *
     * @param source 源字符串
     * @return 转换结果
     */
    public abstract String normalize(String source);

    /**
     * 判断是否符合指定风格的命名格式
     *
     * @param source 字符串
     * @return 是否匹配此命名风格
     */
    public abstract boolean matches(String source);

    /**
     * 将字符串转换为指定的命名风格 由于性能原因，其他枚举值可以重写此方法
     *
     * @param source 原字符串
     * @param format 目标格式
     * @return 转换结果
     */
    public String convert(CaseFormat format, String source) {
        return format.normalize(source);
    }

    @Override
    public String apply(String source) {
        return normalize(source);
    }
}
