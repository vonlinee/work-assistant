package org.example.workassistant.sdk.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.example.workassistant.sdk.lang.Interpolations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * String工具类
 *
 * @since 17
 */
public abstract class StringUtils {

    /**
     * 字符串常量：{@code "null"} <br>
     * 注意：{@code "null" != null}
     */
    public static final String NULL = "null";
    public static final String SINGLE_QUOTATION = "'";
    public static final String NULL_STRING = "NULL";
    /**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";
    /**
     * The empty String {@code ""}.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";
    /**
     * A String for linefeed LF ("\n").
     *
     * @see <a href=
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">
     * JLF: Escape Sequences for Character and String Literals</a>
     * @since 3.2
     */
    public static final String LF = "\n";
    /**
     * A String for carriage return CR ("\r").
     *
     * @see <a href=
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF:
     * Escape Sequences for Character and String Literals</a>
     * @since 3.2
     */
    public static final String CR = "\r";
    /**
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;
    public static final String DEFAULT_SEPARATOR = ",";
    public static final String SLASH = "/";
    public static final String DOUBLE_DOT = "..";
    public static final String COLON = ":";
    public static final String SEPARATOR = ",";
    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final String FOLDER_SEPARATOR = "/";
    private static final char FOLDER_SEPARATOR_CHAR = '/';
    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    private static final String TOP_PATH = "..";
    private static final String CURRENT_PATH = ".";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final Pattern ALL_EN_WORDS = Pattern.compile("[a-zA-Z]+");
    private static final Pattern CONTAIN_EN_WORDS = Pattern.compile(".*[a-zA-z].*");
    private static final int STRING_BUILDER_SIZE = 256;
    private static final int DEFAULT_TRUNCATION_THRESHOLD = 100;
    private static final String TRUNCATION_SUFFIX = " (truncated)...";
    /**
     * <p>
     * The maximum size to which the padding(填充) constant(s) can expand.
     * </p>
     */
    private static final int PAD_LIMIT = 8192;

    private StringUtils() {
    }

    public static String substring(String s, String b, char c) {
        int i = b.lastIndexOf(c);
        return s.substring(i);
    }

    public static String substring(String src, Supplier<Integer> startIndexSupplier) {
        return src.substring(startIndexSupplier.get());
    }

    public static <T> String substring(String src, Function<T, Integer> startIndexSupplier, T startIndex) {
        return src.substring(startIndexSupplier.apply(startIndex));
    }

    /**
     * 截取开头到从右往左数第count个字符的位置
     *
     * @param sequence 字符串
     * @param count    从右往左数第count个字符，>= 0
     * @return 字符串
     */
    public static String lastSubstring(CharSequence sequence, int count) {
        if (sequence == null) {
            return EMPTY;
        }
        if (count <= 0) {
            return sequence.toString();
        }
        return sequence.subSequence(0, sequence.length() - count).toString();
    }

    /**
     * 字符串截取
     *
     * @param sb    StringBuilder
     * @param start 开始位置
     * @param end   结束位置
     * @return 截取后的子串
     */
    public static String substring(StringBuilder sb, int start, int end) {
        if (sb == null || start < 0 || end > sb.length()) {
            return EMPTY;
        }
        return sb.substring(start, end);
    }

    /**
     * 将字符串转换为同意最长的长度
     *
     * @param strings 数组
     * @return List<String>
     */
    public static List<String> uniformLength(List<String> strings) {
        int maxLen = strings.get(0).length();
        int size = strings.size();
        for (int i = 0; i < size; i++) {
            for (int j = 1; j < size; j++) {
                int nextLen = strings.get(j).length();
                if (nextLen > maxLen) {
                    maxLen = nextLen;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            strings.add(i, appendBlank(strings.remove(i), maxLen));
        }
        return strings;
    }

    /**
     * @param sequence 源字符串
     * @param c        追加的字符
     * @param len      长度
     * @return String
     */
    public static String endWith(String sequence, char c, int len) {
        int i = len - sequence.length();
        if (i > 0) {
            sequence = sequence + String.valueOf(c).repeat(i);
        }
        return sequence;
    }

    /**
     * @param sequence 字符串
     * @param len      空格数量
     * @return String
     */
    public static String appendBlank(String sequence, int len) {
        int i = len - sequence.length();
        if (i > 0) {
            sequence = sequence + " ".repeat(i);
        }
        return sequence;
    }

    /**
     * 分割字符串
     *
     * @param str       字符串
     * @param delimiter 分隔符
     * @return 分割后的字符串数组
     */
    public static String[] split1(String str, String delimiter) {
        StringTokenizer st = new StringTokenizer(str, delimiter);
        int i = st.countTokens();
        String[] strings = new String[i];
        while (st.hasMoreTokens()) {
            strings[i - (++i)] = st.nextToken();
        }
        return strings;
    }

    /**
     * Check whether the given object (possibly a {@code String}) is empty.
     * This is effectively a shortcut for {@code !hasLength(String)}.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will never return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     * <p><b>Note: If the object is typed to {@code String} upfront, prefer
     * {@link #hasText(String)} instead.</b>
     *
     * @param str the candidate object (possibly a {@code String})
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    public static boolean isAllEnWords(String str) {
        return ALL_EN_WORDS.matcher(str).matches();
    }

    public static boolean containEnWords(String str) {
        return CONTAIN_EN_WORDS.matcher(str).matches();
    }

    public static boolean isUpperCase(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isUpperCase(c)) return false;
        }
        return true;
    }

    /**
     * 大写首字母
     *
     * @param str 原字符串
     * @return 大写首字母后的字符串
     */
    public static String upperFirst(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(str.toCharArray()[0]) + str.substring(1);
    }

    /**
     * 小写首字母<br>
     * 例如：str = Name, return name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String lowerFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (!str.isEmpty()) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + sub(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 切割指定位置之后部分的字符串
     *
     * @param string    字符串
     * @param fromIndex 切割开始的位置（包括）
     * @return 切割后后剩余的后半部分字符串
     */
    public static String sub(CharSequence string, int fromIndex) {
        if (!hasLength(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    /**
     * 改进JDK subString<br>
     * index从0开始计算，最后一个字符为-1<br>
     * 如果from和to位置一样，返回 "" <br>
     * 如果from或to为负数，则按照length从后向前数位置，如果绝对值大于字符串长度，则from归到0，to归到length<br>
     * 如果经过修正的index中from大于to，则互换from和to example: <br>
     * abcdefgh 2 3 =》 c <br>
     * abcdefgh 2 -3 =》 cde <br>
     *
     * @param str              String
     * @param fromIndexInclude 开始的index（包括）
     * @param toIndexExclude   结束的index（不包括）
     * @return 字串
     */
    public static String sub(CharSequence str, int fromIndexInclude, int toIndexExclude) {
        if (isEmpty(str)) {
            return toString(str);
        }
        int len = str.length();

        if (fromIndexInclude < 0) {
            fromIndexInclude = len + fromIndexInclude;
            if (fromIndexInclude < 0) {
                fromIndexInclude = 0;
            }
        } else if (fromIndexInclude > len) {
            fromIndexInclude = len;
        }

        if (toIndexExclude < 0) {
            toIndexExclude = len + toIndexExclude;
            if (toIndexExclude < 0) {
                toIndexExclude = len;
            }
        } else if (toIndexExclude > len) {
            toIndexExclude = len;
        }

        if (toIndexExclude < fromIndexInclude) {
            int tmp = fromIndexInclude;
            fromIndexInclude = toIndexExclude;
            toIndexExclude = tmp;
        }

        if (fromIndexInclude == toIndexExclude) {
            return EMPTY;
        }

        return str.toString().substring(fromIndexInclude, toIndexExclude);
    }

    /**
     * 使用引号包裹
     *
     * @param str            字符串
     * @param doubleQuotaion 是否使用双引号
     * @return 引号包裹的字符串
     */
    public static String wrapQuotation(String str, boolean doubleQuotaion) {
        if (doubleQuotaion) {
            if (!str.contains("\"")) {
                return "\"" + str + "\"";
            } else {
                if (str.startsWith("\"") && !str.endsWith("\"")) return str + "\"";
                if (!str.startsWith("\"") && str.endsWith("\"")) return "\"" + str;
                String substring = str.substring(1, str.length() - 1);
                if (substring.contains("\"")) {
                    return "\"" + substring.replace("\"", "") + "\"";
                }
                return str;
            }
        } else {
            String c = SINGLE_QUOTATION;
            if (!str.contains("\"")) {
                return c + str + c;
            } else {
                if (str.startsWith(c) && !str.endsWith(c)) return str + c;
                if (!str.startsWith(c) && str.endsWith(c)) return str + c;
                String substring = str.substring(1, str.length() - 1);
                if (substring.contains(c)) {
                    return c + substring.replace(c, "") + c;
                }
                return str;
            }
        }
    }

    /**
     * 判断字符串是否在集合中，通过equals进行比较
     *
     * @param target 指定字符串
     * @param group  字符串集合
     * @return 是否包含
     */
    public static boolean contains(String target, String... group) {
        for (String s : group) {
            if (target.equals(s)) return true;
        }
        return false;
    }

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     *
     * @param str the {@code String} to check (maybe {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
     * @see Character#isWhitespace
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    /**
     * 判断是否所有字符串同时满足不为空条件
     *
     * @param strings 字符串数组
     * @return 是否所有字符串同时满足不为空
     */
    public static boolean hasText(String... strings) {
        if (strings == null) {
            return false;
        }
        if (strings.length == 1) {
            return hasText(strings[0]);
        }
        for (String string : strings) {
            if (!hasText(string)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * <p><pre class="code">
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (maybe {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see #hasText(String)
     * @see #hasLength(CharSequence)
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * <p><pre class="code">
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (maybe {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * 字符串是否包含文本，循环所有字符，不为空格
     *
     * @param str 字符串
     * @return 字符串是否包含文本
     */
    public static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterable} into a single String
     * containing the provided elements.
     * </p>
     *
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is
     * the same as an empty String ("").
     * </p>
     *
     * <p>
     * See the examples here: {@link # join(Object[], String)}.
     * </p>
     *
     * @param iterable  the {@code Iterable} providing the values to join together,
     *                  maybe null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null iterator input
     * @since 2.3
     */
    public static String join(final Iterable<?> iterable, final String separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    /**
     * 使用指定分隔符拼接字符串
     *
     * @param separator 分隔符
     * @param items     带拼接的字符串
     * @return 拼接后的字符串
     */
    public static String joinWithSeparator(final String separator, String... items) {
        return join(Arrays.asList(items), separator);
    }

    /**
     * 使用指定分隔符拼接字符串
     *
     * @param separator 分隔符
     * @param items     带拼接的字符串
     * @return 拼接后的字符串
     */
    public static String join(final char separator, String... items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(separator);
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 默认使用英文逗号拼接字符串
     *
     * @param items 字符串列表
     * @return 拼接后的字符串
     */
    public static String joinByComma(String... items) {
        return join(ArrayUtils.asArrayList(items).iterator(), DEFAULT_SEPARATOR);
    }

    public static String join(final String[] iterator, final String separator) {
        return join(Arrays.asList(iterator), separator);
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterator} into a single String
     * containing the provided elements.
     * </p>
     *
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is
     * the same as an empty String ("").
     * </p>
     *
     * <p>
     * See the examples here: {@link # join(Object[], String)}.
     * </p>
     *
     * @param iterator  the {@code Iterator} of values to join together, maybe null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, {@code null} if null iterator input
     */
    public static String join(final Iterator<?> iterator, final String separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        final Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, "");
        }
        // two or more elements
        final StringBuilder buf = new StringBuilder(STRING_BUILDER_SIZE); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            final Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * 指定编码的字符串长度
     */
    public static int length(String str, String charset) {
        int len = 0;
        int j = 0;
        byte[] bytes = str.getBytes(Charset.forName(charset));
        do {
            short tmpst = (short) (bytes[j] & 0xF0);
            if (tmpst >= 0xB0) {
                if (tmpst < 0xC0 || ((tmpst == 0xC0) || (tmpst == 0xD0))) {
                    j += 2;
                    len += 2;
                } else if (tmpst == 0xE0) {
                    j += 3;
                    len += 2;
                } else { // tmpst == 0xF0
                    short tmpst0 = (short) (((short) bytes[j]) & 0x0F);
                    if (tmpst0 == 0) {
                        j += 4;
                        len += 2;
                    } else if (tmpst0 < 12) { // tmpst0 > 0
                        j += 5;
                        len += 2;
                    } else { // tmpst0 > 11
                        j += 6;
                        len += 2;
                    }
                }
            } else {
                j += 1;
                len += 1;
            }
        } while (j <= bytes.length - 1);
        return len;
    }

    /**
     * 是否包含空格
     *
     * @param sequence CharSequence
     * @return 是否包含空格
     */
    public static boolean containWhiteSpace(CharSequence sequence) {
        return sequence == null || sequence.toString().contains(" ");
    }

    /**
     * 重复字符串指定次数，然后new一个新字符串
     *
     * @param string 字符串
     * @param count  重复次数
     * @return 字符串
     */
    public static String repeat(String string, int count) {
        if (!hasLength(string)) {
            return null;
        }
        if (count <= 1) {
            if (count >= 0) {
                return string;
            }
            return string;
        }
        final int len = string.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
        }
        final char[] array = new char[size];
        string.getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * Returns the given {@code template} string with each occurrence of {@code "%s"} replaced with
     * the corresponding argument value from {@code args}; or, if the placeholder and argument counts
     * do not match, returns a best-effort form of that string. Will not throw an exception under
     * normal conditions.
     *
     * <p><b>Note:</b> For most string-formatting needs, use {@link String#format String.format},
     * {@link java.io.PrintWriter#format PrintWriter.format}, and related methods. These support the
     * full range of <a
     * href="https://docs.oracle.com/javase/9/docs/api/java/util/Formatter.html#syntax">format
     * specifiers</a>, and alert you to usage errors by throwing {@link
     * IllegalFormatException}.
     *
     * <p>In certain cases, such as outputting debugging information or constructing a message to be
     * used for another unchecked exception, an exception during string formatting would serve little
     * purpose except to supplant the real information you were trying to provide. These are the cases
     * this method is made for; it instead generates a best-effort string with all supplied argument
     * values present. This method is also useful in environments such as GWT where {@code
     * String.format} is not available. As an example, method implementations of the
     * Preconditions class use this formatter, for both of the reasons just discussed.
     *
     * <p><b>Warning:</b> Only the exact two-character placeholder sequence {@code "%s"} is
     * recognized.
     *
     * @param template a string containing zero or more {@code "%s"} placeholder sequences. {@code
     *                 null} is treated as the four-character string {@code "null"}.
     * @param args     the arguments to be substituted into the message template. The first argument
     *                 specified is substituted for the first occurrence of {@code "%s"} in the template, and so
     *                 forth. A {@code null} argument is converted to the four-character string {@code "null"};
     *                 non-null values are converted to strings using {@link Object#toString()}.
     * @since 25.1
     */
    public static String format(String template, Object... args) {
        template = String.valueOf(template); // null -> "null"
        if (args == null) {
            args = new Object[]{"(Object[])null"};
        } else {
            for (int i = 0; i < args.length; i++) {
                args[i] = valueOf(args[i]);
            }
        }
        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template, templateStart, template.length());

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }

    /**
     * 会对输入参数的可能情况进行检测
     *
     * @param o object
     * @return 字符串
     * @see String#valueOf(Object)
     */
    public static String valueOf(Object o) {
        if (o == null) {
            return NULL_STRING;
        }
        try {
            return o.toString();
        } catch (Exception e) {
            // Default toString() behavior - see Object.toString()
            String objectToString = o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
            // Log is created inline with fixed name to avoid forcing Proguard to create another class.
            return "<" + objectToString + " threw " + e.getClass().getName() + ">";
        }
    }

    /**
     * 判断字符串是否为空字符串
     *
     * @param str 待校验的字符串
     * @return 字符串是否不为空字符串
     * @see StringUtils#hasText(String)
     * @see Character#isWhitespace(char)
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * 判断字符串是否为空字符串
     *
     * @param str 待校验的字符串
     * @return 字符串是否不为空
     * @see StringUtils#hasText(String)
     * @see Character#isWhitespace(char)
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * 通过kv形式进行字符串插值
     *
     * @param template 模式串
     * @param mappings 参数
     * @return 插值结果
     */
    public static String interpolate(String template, Map<String, Object> mappings) {
        return Interpolations.named(template, mappings);
    }

    /**
     * Check whether the given {@code CharSequence} contains any whitespace characters.
     *
     * @param str the {@code CharSequence} to check (maybe {@code null})
     * @return {@code true} if the {@code CharSequence} is not empty and
     * contains at least 1 whitespace character
     * @see Character#isWhitespace
     */
    private static boolean containsWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace((int) str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code String} contains any whitespace characters.
     *
     * @param str the {@code String} to check (maybe {@code null})
     * @return {@code true} if the {@code String} is not empty and
     * contains at least 1 whitespace character
     * @see #containsWhitespace(CharSequence)
     */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence) str);
    }

    /**
     * 去掉头部和尾部的空格
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int beginIndex = 0;
        int endIndex = str.length() - 1;
        while (beginIndex <= endIndex && Character.isWhitespace(str.charAt(beginIndex))) {
            beginIndex++;
        }
        while (endIndex > beginIndex && Character.isWhitespace(str.charAt(endIndex))) {
            endIndex--;
        }
        return str.substring(beginIndex, endIndex + 1);
    }

    /**
     * 遍历字符数组，去掉字符串中所有的空格，包括头部，尾部以及中间的所有空格
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 去掉开头的空格
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int beginIdx = 0;
        while (beginIdx < str.length() && Character.isWhitespace(str.charAt(beginIdx))) {
            beginIdx++;
        }
        return str.substring(beginIdx);
    }

    /**
     * 去除字符串首尾的空字符
     *
     * @param str 字符串
     * @return 如果参数为空，原样返回
     */
    public static String trim(String str) {
        if (str == null) {
            return EMPTY;
        }
        if (!str.isEmpty()) {
            str = str.trim();
        }
        return str;
    }

    /**
     * Trim trailing whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int endIdx = str.length() - 1;
        while (endIdx >= 0 && Character.isWhitespace(str.charAt(endIdx))) {
            endIdx--;
        }
        return str.substring(0, endIdx + 1);
    }

    /**
     * Trim all occurrences of the supplied leading character from the given {@code String}.
     *
     * @param str              the {@code String} to check
     * @param leadingCharacter the leading character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if (!hasLength(str)) {
            return str;
        }
        int beginIdx = 0;
        while (beginIdx < str.length() && leadingCharacter == str.charAt(beginIdx)) {
            beginIdx++;
        }
        return str.substring(beginIdx);
    }

    /**
     * Trim all occurrences of the supplied trailing character from the given {@code String}.
     *
     * @param str               the {@code String} to check
     * @param trailingCharacter the trailing character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimTrailingCharacter(String str, char trailingCharacter) {
        if (!hasLength(str)) {
            return str;
        }
        int endIdx = str.length() - 1;
        while (endIdx >= 0 && trailingCharacter == str.charAt(endIdx)) {
            endIdx--;
        }
        return str.substring(0, endIdx + 1);
    }

    /**
     * Test if the given {@code String} matches the given single character.
     *
     * @param str             the {@code String} to check
     * @param singleCharacter the character to compare to
     * @since 5.2.9
     */
    public static boolean matchesCharacter(String str, char singleCharacter) {
        return (str != null && str.length() == 1 && str.charAt(0) == singleCharacter);
    }

    /**
     * Test if the given {@code String} starts with the specified prefix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param prefix the prefix to look for
     * @see java.lang.String#startsWith
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() && str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    /**
     * Test if the given {@code String} ends with the specified suffix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param suffix the suffix to look for
     * @see java.lang.String#endsWith
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() && str.regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length()));
    }

    /**
     * Test whether the given string matches the given substring
     * at the given index.
     *
     * @param str       the original string (or StringBuilder)
     * @param index     the index in the original string to start matching against
     * @param substring the substring to match at the given index
     */
    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (str.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Count the occurrences of the substring {@code sub} in string {@code str}.
     *
     * @param str string to search in
     * @param sub string to search for
     */
    public static int countOccurrencesOf(String str, String sub) {
        if (!hasLength(str) || !hasLength(sub)) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     *
     * @param inString   {@code String} to examine
     * @param oldPattern {@code String} to replace
     * @param newPattern {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }
        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0;  // our position in the old string
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        // append any characters to the right of a match
        sb.append(inString, pos, inString.length());
        return sb.toString();
    }

    /**
     * Delete all occurrences of the given substring.
     *
     * @param inString the original {@code String}
     * @param pattern  the pattern to delete all occurrences of
     * @return the resulting {@code String}
     */
    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    /**
     * Delete any character in a given {@code String}.
     *
     * @param inString      the original {@code String}
     * @param charsToDelete a set of characters to delete.
     *                      E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }
        int lastCharIndex = 0;
        char[] result = new char[inString.length()];
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                result[lastCharIndex++] = c;
            }
        }
        if (lastCharIndex == inString.length()) {
            return inString;
        }
        return new String(result, 0, lastCharIndex);
    }

    /**
     * Quote the given {@code String} with single quotes.
     *
     * @param str the input {@code String} (e.g. "myString")
     * @return the quoted {@code String} (e.g. "'myString'"),
     * or {@code null} if the input was {@code null}
     */
    public static String quote(String str) {
        return (str != null ? "'" + str + "'" : null);
    }

    /**
     * Turn the given Object into a {@code String} with single quotes
     * if it is a {@code String}; keeping the Object as-is else.
     *
     * @param obj the input Object (e.g. "myString")
     * @return the quoted {@code String} (e.g. "'myString'"),
     * or the input object as-is if not a {@code String}
     */
    public static Object quote(Object obj) {
        return (obj instanceof String str ? quote(str) : obj);
    }

    /**
     * Unqualify a string qualified by a '.' dot character. For example,
     * "this.name.is.qualified", returns "qualified".
     *
     * @param qualifiedName the qualified name
     */
    public static String unqualify(String qualifiedName) {
        return unqualify(qualifiedName, '.');
    }

    /**
     * Unqualify a string qualified by a separator character. For example,
     * "this:name:is:qualified" returns "qualified" if using a ':' separator.
     *
     * @param qualifiedName the qualified name
     * @param separator     the separator
     */
    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    /**
     * Capitalize a {@code String}, changing the first letter to
     * upper case as per {@link Character#toUpperCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to capitalize
     * @return the capitalized {@code String}
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a {@code String}, changing the first letter to
     * lower case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to uncapitalize
     * @return the uncapitalized {@code String}
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }
        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        } else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars);
    }

    /**
     * Extract the filename from the given Java resource path,
     * e.g. {@code "mypath/myfile.txt" &rarr; "myfile.txt"}.
     *
     * @param path the file path (maybe {@code null})
     * @return the extracted filename, or {@code null} if none
     */
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }

    /**
     * Extract the filename extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" &rarr; "txt".
     *
     * @param path the file path (maybe {@code null})
     * @return the extracted filename extension, or {@code null} if none
     */
    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }
        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        if (folderIndex > extIndex) {
            return null;
        }
        return path.substring(extIndex + 1);
    }

    /**
     * Strip the filename extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" &rarr; "mypath/myfile".
     *
     * @param path the file path
     * @return the path with stripped filename extension
     */
    public static String stripFilenameExtension(String path) {
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return path;
        }
        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        if (folderIndex > extIndex) {
            return path;
        }
        return path.substring(0, extIndex);
    }

    /**
     * Apply the given relative path to the given Java resource path,
     * assuming standard Java folder separation (i.e. "/" separators).
     *
     * @param path         the path to start from (usually a full file path)
     * @param relativePath the relative path to apply
     *                     (relative to the full file path above)
     * @return the full file path that results from applying the relative path
     */
    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR_CHAR;
            }
            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and
     * inner simple dots.
     * <p>The result is convenient for path comparison. For other uses,
     * notice that Windows separators ("\") are replaced by simple slashes.
     * <p><strong>NOTE</strong> that {@code cleanPath} should not be depended
     * upon in a security context. Other mechanisms should be used to prevent
     * path-traversal issues.
     *
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        }

        String normalizedPath = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);
        String pathToUse = normalizedPath;

        // Shortcut if there is no work to do
        if (pathToUse.indexOf('.') == -1) {
            return pathToUse;
        }

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the "." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(':');
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains(FOLDER_SEPARATOR)) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        // we never require more elements than pathArray and in the common case the same number
        Deque<String> pathElements = new ArrayDeque<>(pathArray.length);
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.addFirst(element);
                }
            }
        }

        // All path elements stayed the same - shortcut
        if (pathArray.length == pathElements.size()) {
            return normalizedPath;
        }
        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.addFirst(TOP_PATH);
        }
        // If nothing else left, at least explicitly point to current path.
        if (pathElements.size() == 1 && pathElements.getLast().isEmpty() && !prefix.endsWith(FOLDER_SEPARATOR)) {
            pathElements.addFirst(CURRENT_PATH);
        }

        final String joined = collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
        // avoid string concatenation with empty prefix
        return prefix.isEmpty() ? joined : prefix + joined;
    }

    /**
     * Compare two paths after normalization of them.
     *
     * @param path1 first path for comparison
     * @param path2 second path for comparison
     * @return whether the two paths are equivalent after normalization
     */
    public static boolean pathEquals(String path1, String path2) {
        return cleanPath(path1).equals(cleanPath(path2));
    }

    /**
     * Decode the given encoded URI component value. Based on the following rules:
     * <ul>
     * <li>Alphanumeric characters {@code "a"} through {@code "z"}, {@code "A"} through {@code "Z"},
     * and {@code "0"} through {@code "9"} stay the same.</li>
     * <li>Special characters {@code "-"}, {@code "_"}, {@code "."}, and {@code "*"} stay the same.</li>
     * <li>A sequence "{@code %<i>xy</i>}" is interpreted as a hexadecimal representation of the character.</li>
     * </ul>
     *
     * @param source  the encoded String
     * @param charset the character set
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see java.net.URLDecoder#decode(String, String)
     */
    public static String uriDecode(String source, Charset charset) {
        int length = source.length();
        if (length == 0) {
            return source;
        }
        Assert.notNull(charset, "Charset must not be null");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(length)) {
            boolean changed = false;
            for (int i = 0; i < length; i++) {
                int ch = source.charAt(i);
                if (ch == '%') {
                    if (i + 2 < length) {
                        char hex1 = source.charAt(i + 1);
                        char hex2 = source.charAt(i + 2);
                        int u = Character.digit(hex1, 16);
                        int l = Character.digit(hex2, 16);
                        if (u == -1 || l == -1) {
                            throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                        }
                        baos.write((char) ((u << 4) + l));
                        i += 2;
                        changed = true;
                    } else {
                        throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                    }
                } else {
                    baos.write(ch);
                }
            }
            return (changed ? baos.toString(charset) : source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse the given {@code String} value into a {@link Locale}, accepting
     * the {@link Locale#toString} format as well as BCP 47 language tags as
     * specified by {@link Locale#forLanguageTag}.
     *
     * @param localeValue the locale value: following either {@code Locale's}
     *                    {@code toString()} format ("en", "en_UK", etc.), also accepting spaces as
     *                    separators (as an alternative to underscores), or BCP 47 (e.g. "en-UK")
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     * @see #parseLocaleString
     * @see Locale#forLanguageTag
     * @since 5.0.4
     */
    public static Locale parseLocale(String localeValue) {
        String[] tokens = tokenizeLocaleSource(localeValue);
        if (tokens.length == 1) {
            validateLocalePart(localeValue);
            Locale resolved = Locale.forLanguageTag(localeValue);
            if (!resolved.getLanguage().isEmpty()) {
                return resolved;
            }
        }
        return parseLocaleTokens(localeValue, tokens);
    }

    /**
     * Parse the given {@code String} representation into a {@link Locale}.
     * <p>For many parsing scenarios, this is an inverse operation of
     * {@link Locale#toString Locale's toString}, in a lenient sense.
     * This method does not aim for strict {@code Locale} design compliance;
     * it is rather specifically tailored for typical Spring parsing needs.
     * <p><b>Note: This delegate does not accept the BCP 47 language tag format.
     * Please use {@link #parseLocale} for lenient parsing of both formats.</b>
     *
     * @param localeString the locale {@code String}: following {@code Locale's}
     *                     {@code toString()} format ("en", "en_UK", etc.), also accepting spaces as
     *                     separators (as an alternative to underscores)
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     */

    public static Locale parseLocaleString(String localeString) {
        return parseLocaleTokens(localeString, tokenizeLocaleSource(localeString));
    }

    private static String[] tokenizeLocaleSource(String localeSource) {
        return tokenizeToStringArray(localeSource, "_ ", false, false);
    }


    private static Locale parseLocaleTokens(String localeString, String[] tokens) {
        String language = (tokens.length > 0 ? tokens[0] : "");
        String country = (tokens.length > 1 ? tokens[1] : "");
        validateLocalePart(language);
        validateLocalePart(country);

        String variant = "";
        if (tokens.length > 2) {
            // There is definitely a variant, and it is everything after the country
            // code sans the separator between the country code and the variant.
            int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
            // Strip off any leading '_' and whitespace, what's left is the variant.
            variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
            if (variant.startsWith("_")) {
                variant = trimLeadingCharacter(variant, '_');
            }
        }

        if (variant.isEmpty() && country.startsWith("#")) {
            variant = country;
            country = "";
        }

        return (!language.isEmpty() ? new Locale(language, country, variant) : null);
    }

    private static void validateLocalePart(String localePart) {
        for (int i = 0; i < localePart.length(); i++) {
            char ch = localePart.charAt(i);
            if (ch != ' ' && ch != '_' && ch != '-' && ch != '#' && !Character.isLetterOrDigit(ch)) {
                throw new IllegalArgumentException("Locale part \"" + localePart + "\" contains invalid characters");
            }
        }
    }

    /**
     * Determine the RFC 3066 compliant language tag,
     * as used for the HTTP "Accept-Language" header.
     *
     * @param locale the Locale to transform to a language tag
     * @return the RFC 3066 compliant language tag as {@code String}
     * in favor of {@link Locale#toLanguageTag()}
     * @see Locale#toLanguageTag()
     */
    public static String toLanguageTag(Locale locale) {
        return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
    }

    /**
     * Parse the given {@code timeZoneString} value into a {@link TimeZone}.
     *
     * @param timeZoneString the time zone {@code String}, following {@link TimeZone#getTimeZone(String)}
     *                       but throwing {@link IllegalArgumentException} in case of an invalid time zone specification
     * @return a corresponding {@link TimeZone} instance
     * @throws IllegalArgumentException in case of an invalid time zone specification
     */
    public static TimeZone parseTimeZoneString(String timeZoneString) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        if ("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT")) {
            // We don't want that GMT fallback...
            throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
        }
        return timeZone;
    }

    //---------------------------------------------------------------------
    // Convenience methods for working with String arrays
    //---------------------------------------------------------------------

    /**
     * Copy the given {@link Collection} into a {@code String} array.
     * <p>The {@code Collection} must contain {@code String} elements only.
     *
     * @param collection the {@code Collection} to copy
     *                   (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Collection<String> collection) {
        return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
    }

    /**
     * Copy the given {@link Enumeration} into a {@code String} array.
     * <p>The {@code Enumeration} must contain {@code String} elements only.
     *
     * @param enumeration the {@code Enumeration} to copy
     *                    (potentially {@code null} or empty)
     * @return the resulting {@code String} array
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return (enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY);
    }

    /**
     * Append the given {@code String} to the given {@code String} array,
     * returning a new array consisting of the input array contents plus
     * the given {@code String}.
     *
     * @param array the array to append to (can be {@code null})
     * @param str   the {@code String} to append
     * @return the new array (never {@code null})
     */
    public static String[] addStringToArray(String[] array, String str) {
        if (ObjectUtils.isEmpty(array)) {
            return new String[]{str};
        }
        String[] newArr = new String[array.length + 1];
        System.arraycopy(array, 0, newArr, 0, array.length);
        newArr[array.length] = str;
        return newArr;
    }

    /**
     * Concatenate the given {@code String} arrays into one,
     * with overlapping array elements included twice.
     * <p>The order of elements in the original arrays is preserved.
     *
     * @param array1 the first array (can be {@code null})
     * @param array2 the second array (can be {@code null})
     * @return the new array ({@code null} if both given arrays were {@code null})
     */
    public static String[] concatenateStringArrays(String[] array1, String[] array2) {
        if (ObjectUtils.isEmpty(array1)) {
            return array2;
        }
        if (ObjectUtils.isEmpty(array2)) {
            return array1;
        }
        String[] newArr = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, newArr, 0, array1.length);
        System.arraycopy(array2, 0, newArr, array1.length, array2.length);
        return newArr;
    }

    /**
     * Merge the given {@code String} arrays into one, with overlapping
     * array elements only included once.
     * <p>The order of elements in the original arrays is preserved
     * (except overlapping elements, which are only
     * included on their first occurrence).
     *
     * @param array1 the first array (can be {@code null})
     * @param array2 the second array (can be {@code null})
     * @return the new array ({@code null} if both given arrays were {@code null})
     * @deprecated in favor of manual merging via {@link LinkedHashSet}
     * (with every entry included at most once, even entries within the first array)
     */
    public static String[] mergeStringArrays(String[] array1, String[] array2) {
        if (ObjectUtils.isEmpty(array1)) {
            return array2;
        }
        if (ObjectUtils.isEmpty(array2)) {
            return array1;
        }
        List<String> result = new ArrayList<>(Arrays.asList(array1));
        for (String str : array2) {
            if (!result.contains(str)) {
                result.add(str);
            }
        }
        return toStringArray(result);
    }

    /**
     * Sort the given {@code String} array if necessary.
     *
     * @param array the original array (potentially empty)
     * @return the array in sorted form (never {@code null})
     */
    public static String[] sortStringArray(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        Arrays.sort(array);
        return array;
    }

    /**
     * Trim the elements of the given {@code String} array, calling
     * {@code String.trim()} on each non-null element.
     *
     * @param array the original {@code String} array (potentially empty)
     * @return the resulting array (of the same size) with trimmed elements
     */
    public static String[] trimArrayElements(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String element = array[i];
            result[i] = (element != null ? element.trim() : null);
        }
        return result;
    }

    /**
     * Remove duplicate strings from the given array.
     * <p>As of 4.2, it preserves the original order, as it uses a {@link LinkedHashSet}.
     *
     * @param array the {@code String} array (potentially empty)
     * @return an array without duplicates, in natural sort order
     */
    public static String[] removeDuplicateStrings(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }
        Set<String> set = new LinkedHashSet<>(ArrayUtils.asArrayList(array));
        return toStringArray(set);
    }

    /**
     * Split a {@code String} at the first occurrence of the delimiter.
     * Does not include the delimiter in the result.
     *
     * @param toSplit   the string to split (potentially {@code null} or empty)
     * @param delimiter to split the string up with (potentially {@code null} or empty)
     * @return a two element array with index 0 being before the delimiter, and
     * index 1 being after the delimiter (neither element includes the delimiter);
     * or {@code null} if the delimiter wasn't found in the given input {@code String}
     */
    public static String[] split(String toSplit, String delimiter) {
        if (!hasLength(toSplit) || !hasLength(delimiter)) {
            return EMPTY_STRING_ARRAY;
        }
        return toSplit.split(delimiter, 0);
    }

    public static List<String> splitToList(String toSplit) {
        return splitToList(toSplit, ",");
    }

    public static List<String> splitToList(String toSplit, String delimiter) {
        return Arrays.asList(split(toSplit, delimiter));
    }

    public static boolean containsAny(final CharSequence cs, final CharSequence... searchCharSequences) {
        for (CharSequence searchCharSequence : searchCharSequences) {
            if (cs.toString().contains(searchCharSequence)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the delimiter
     * providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the {@code Properties}.
     *
     * @param array     the array to process
     * @param delimiter to split each element using (typically the equals symbol)
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */
    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
        return splitArrayElementsIntoProperties(array, delimiter, null);
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the
     * delimiter providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the
     * {@code Properties} instance.
     *
     * @param array         the array to process
     * @param delimiter     to split each element using (typically the equals symbol)
     * @param charsToDelete one or more characters to remove from each element
     *                      prior to attempting the split operation (typically the quotation mark
     *                      symbol), or {@code null} if no removal should occur
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */
    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {
        if (ObjectUtils.isEmpty(array)) {
            return null;
        }
        Properties result = new Properties();
        for (String element : array) {
            if (charsToDelete != null) {
                element = deleteAny(element, charsToDelete);
            }
            String[] splittedElement = split(element, delimiter);
            if (splittedElement == null) {
                continue;
            }
            result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
        }
        return result;
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>Trims tokens and omits empty tokens.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str        the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters the delimiter characters, assembled as a {@code String}
     *                   (each of the characters is individually considered as a delimiter)
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str               the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters        the delimiter characters, assembled as a {@code String}
     *                          (each of the characters is individually considered as a delimiter)
     * @param trimTokens        trim the tokens via {@link String#trim()}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || !token.isEmpty()) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a
     * {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as a bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str       the input {@code String} (potentially {@code null} or empty)
     * @param delimiter the delimiter between elements (this is a single delimiter,
     *                  rather than a bunch individual delimiter characters)
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into
     * a {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as a bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str           the input {@code String} (potentially {@code null} or empty)
     * @param delimiter     the delimiter between elements (this is a single delimiter,
     *                      rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted
     *                      line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        if (delimiter == null) {
            return new String[]{str};
        }
        List<String> result = new ArrayList<>();
        if (delimiter.isEmpty()) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (!str.isEmpty() && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into an
     * array of strings.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return an array of strings, or the empty array in case of empty input
     */
    public static String[] commaDelimitedListToStringArray(String str) {
        return delimitedListToStringArray(str, ",");
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into a set.
     * <p>Note that this will suppress duplicates, and as of 4.2, the elements in
     * the returned set will preserve the original order in a {@link LinkedHashSet}.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return a set of {@code String} entries in the list
     * @see #removeDuplicateStrings(String[])
     */
    public static Set<String> commaDelimitedListToSet(String str) {
        String[] tokens = commaDelimitedListToStringArray(str);
        return new LinkedHashSet<>(ArrayUtils.asArrayList(tokens));
    }

    /**
     * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll   the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim  the delimiter to use (typically a ",")
     * @param prefix the {@code String} to start each element with
     * @param suffix the {@code String} to end each element with
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        if (CollectionUtils.isEmpty(coll)) {
            return "";
        }
        int totalLength = coll.size() * (prefix.length() + suffix.length()) + (coll.size() - 1) * delim.length();
        for (Object element : coll) {
            totalLength += String.valueOf(element).length();
        }

        StringBuilder sb = new StringBuilder(totalLength);
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll  the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String collectionToCommaDelimitedString(Collection<?> coll) {
        return collectionToDelimitedString(coll, ",");
    }

    /**
     * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr   the array to display (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String arrayToDelimitedString(Object[] arr, String delim) {
        if (ObjectUtils.isEmpty(arr)) {
            return "";
        }
        if (arr.length == 1) {
            return String.valueOf(arr[0]);
        }
        StringJoiner sj = new StringJoiner(delim);
        for (Object elem : arr) {
            sj.add(String.valueOf(elem));
        }
        return sj.toString();
    }

    /**
     * Convert a {@code String} array into a comma delimited {@code String}
     * (i.e., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr the array to display (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String arrayToCommaDelimitedString(Object[] arr) {
        return arrayToDelimitedString(arr, ",");
    }

    /**
     * 比较两个字符串是否相等，规则如下
     * <ul>
     *     <li>str1和str2都为{@code null}</li>
     *     <li>忽略大小写使用{@link String#equalsIgnoreCase(String)}判断相等</li>
     *     <li>不忽略大小写使用{@link String#contentEquals(CharSequence)}判断相等</li>
     * </ul>
     *
     * @param str1       要比较的字符串1
     * @param str2       要比较的字符串2
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同，或者都是{@code null}，则返回{@code true}
     */
    public static boolean equals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        if (null == str1) {
            // 只有两个都为null才判断相等
            return str2 == null;
        }
        if (null == str2) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }
        if (ignoreCase) {
            return str1.toString().equalsIgnoreCase(str2.toString());
        } else {
            return str1.toString().contentEquals(str2);
        }
    }

    public static boolean equalsAny(CharSequence str1, CharSequence... strs) {
        return equalsAny(str1, false, strs);
    }

    public static boolean equalsAny(CharSequence str1, Collection<String> strs) {
        return equalsAny(str1, false, strs.toArray(String[]::new));
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false}<br>
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param str1       给定需要检查的字符串
     * @param ignoreCase 是否忽略大小写
     * @param strs       需要参与比对的字符串列表
     * @return 是否相同
     * @since 4.3.2
     */
    public static boolean equalsAny(CharSequence str1, boolean ignoreCase, CharSequence... strs) {
        if (strs == null) {
            return false;
        }
        for (CharSequence str : strs) {
            if (equals(str1, str, ignoreCase)) {
                return true;
            }
        }
        return false;
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
                return Character.toUpperCase(firstChar) + substring(str, 1, str.length());
            }
        }
        return str.toString();
    }

    public static String substring(CharSequence str, int from) {
        return substring(str, from, str.length());
    }

    /**
     * 改进JDK subString<br>
     * index从0开始计算，最后一个字符为-1<br>
     * 如果from和to位置一样，返回 "" <br>
     * 如果from或to为负数，则按照length从后向前数位置，如果绝对值大于字符串长度，则from归到0，to归到length<br>
     * 如果经过修正的index中from大于to，则互换from和to example: <br>
     * abcdefgh 2 3 =》 c <br>
     * abcdefgh 2 -3 =》 cde <br>
     *
     * @param str              String
     * @param fromIndexInclude 开始的index（包括）
     * @param toIndexExclude   结束的index（不包括）
     * @return 字串
     */
    public static String substring(CharSequence str, int fromIndexInclude, int toIndexExclude) {
        if (!hasLength(str)) {
            return EMPTY;
        }
        int len = str.length();

        if (fromIndexInclude < 0) {
            fromIndexInclude = len + fromIndexInclude;
            if (fromIndexInclude < 0) {
                fromIndexInclude = 0;
            }
        } else if (fromIndexInclude > len) {
            fromIndexInclude = len;
        }

        if (toIndexExclude < 0) {
            toIndexExclude = len + toIndexExclude;
            if (toIndexExclude < 0) {
                toIndexExclude = len;
            }
        } else if (toIndexExclude > len) {
            toIndexExclude = len;
        }

        if (toIndexExclude < fromIndexInclude) {
            int tmp = fromIndexInclude;
            fromIndexInclude = toIndexExclude;
            toIndexExclude = tmp;
        }

        if (fromIndexInclude == toIndexExclude) {
            return EMPTY;
        }
        return str.toString().substring(fromIndexInclude, toIndexExclude);
    }

    /**
     * 转为字符串，null安全
     *
     * @param obj 对象
     * @return 字符串，不为null
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return EMPTY;
        }
        if (obj instanceof CharSequence cs) {
            return cs.toString();
        }
        return obj.toString();
    }

    /**
     * 字符串是否以给定字符结尾
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否结尾
     */
    public static boolean endWith(CharSequence str, char c) {
        if (!hasLength(str)) {
            return false;
        }
        return c == str.charAt(str.length() - 1);
    }

    /**
     * 以特定的字符串结尾
     *
     * @param source 原字符串
     * @param end    结尾的字符串
     * @return 拼接后的字符串
     */
    public static String withEnd(String source, String end) {
        if (source == null || source.isEmpty()) {
            return end;
        }
        if (source.endsWith(end)) {
            return source;
        }
        return source + end;
    }

    /**
     * 截取分隔字符串之后的字符串，不包括分隔字符串<br>
     * 如果给定的字符串为空串（null或""），返回原字符串<br>
     * 如果分隔字符串为空串（null或""），则返回空串，如果分隔字符串未找到，返回空串，举例如下：
     *
     * <pre>
     * StrUtil.subAfter(null, *, false)      = null
     * StrUtil.subAfter("", *, false)        = ""
     * StrUtil.subAfter(*, null, false)      = ""
     * StrUtil.subAfter("abc", "a", false)   = "bc"
     * StrUtil.subAfter("abcba", "b", false) = "cba"
     * StrUtil.subAfter("abc", "c", false)   = ""
     * StrUtil.subAfter("abc", "d", false)   = ""
     * StrUtil.subAfter("abc", "", false)    = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     * @since 3.1.1
     */
    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : EMPTY;
        }
        if (separator == null) {
            return EMPTY;
        }
        final String str = string.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos || (string.length() - 1) == pos) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串，若前缀不是 preffix， 返回原字符串
     */
    public static String removePrefix(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return toString(str);
        }
        final String str2 = str.toString();
        if (str2.startsWith(prefix.toString())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 切割指定位置之后部分的字符串
     *
     * @param string    字符串
     * @param fromIndex 切割开始的位置（包括）
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSuf(CharSequence string, int fromIndex) {
        if (isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }


    /**
     * 去掉指定后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffix(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        if (str2.endsWith(suffix.toString())) {
            return subPre(str2, str2.length() - suffix.length());// 截取前半段
        }
        return str2;
    }

    /**
     * 切割指定位置之前部分的字符串
     *
     * @param string         字符串
     * @param toIndexExclude 切割到的位置（不包括）
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(CharSequence string, int toIndexExclude) {
        return sub(string, 0, toIndexExclude);
    }

    /**
     * 切割指定位置之前部分的字符串
     *
     * @param sb             字符串 StringBuilder
     * @param toIndexExclude 切割到的位置（不包括）
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(StringBuilder sb, int toIndexExclude) {
        return subPre(sb.toString(), toIndexExclude);
    }

    /**
     * 将字符串组转为字符串
     *
     * @param chars     字符数组
     * @param fromIndex 开始索引
     * @param endIndex  结束索引
     * @return 结果
     */
    public static String toString(char[] chars, int fromIndex, int endIndex) {
        return String.valueOf(Arrays.copyOfRange(chars, fromIndex, endIndex));
    }

    /**
     * Truncate the supplied {@link CharSequence}.
     * <p>Delegates to {@link #truncate(CharSequence, int)}, supplying {@code 100}
     * as the threshold.
     *
     * @param charSequence the {@code CharSequence} to truncate
     * @return a truncated string, or a string representation of the original
     * {@code CharSequence} if its length does not exceed the threshold
     * @since 5.3.27
     */
    public static String truncate(CharSequence charSequence) {
        return truncate(charSequence, DEFAULT_TRUNCATION_THRESHOLD);
    }

    /**
     * Truncate the supplied {@link CharSequence}.
     * <p>If the length of the {@code CharSequence} is greater than the threshold,
     * this method returns a {@linkplain CharSequence#subSequence(int, int)
     * subsequence} of the {@code CharSequence} (up to the threshold) appended
     * with the suffix {@code " (truncated)..."}. Otherwise, this method returns
     * {@code charSequence.toString()}.
     *
     * @param charSequence the {@code CharSequence} to truncate
     * @param threshold    the maximum length after which to truncate; must be a
     *                     positive number
     * @return a truncated string, or a string representation of the original
     * {@code CharSequence} if its length does not exceed the threshold
     * @since 5.3.27
     */
    public static String truncate(CharSequence charSequence, int threshold) {
        Assert.isTrue(threshold > 0, () -> "Truncation threshold must be a positive number: " + threshold);
        if (charSequence.length() > threshold) {
            return charSequence.subSequence(0, threshold) + TRUNCATION_SUFFIX;
        }
        return charSequence.toString();
    }

    /**
     * 如果指定字符串为空，则返回指定的值
     *
     * @param str         判断的字符串
     * @param placeholder 指定的值
     * @return 如果指定字符串为空，则返回指定的值
     */
    public static String whenBlank(String str, String placeholder) {
        return isBlank(str) ? placeholder : str;
    }

    /**
     * 对象转为字符串去除左右空格
     *
     * @param o 带转换对象
     * @return 去掉开头和末尾的空格
     */
    public static String trim(Object o) {
        if (o == null) {
            return EMPTY;
        }
        return o.toString().trim();
    }

    /**
     * 字符串是否为空，长度为0
     *
     * @param cs 字符序列
     * @return 是否为空
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToLowerCase(String param) {
        if (!StringUtils.hasText(param)) {
            return param;
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    /**
     * 正则表达式匹配
     *
     * @param regex 正则表达式字符串
     * @param input 要匹配的字符串
     * @return 如果 input 符合 regex 正则表达式格式, 返回true, 否则返回 false;
     */
    public static boolean matches(String regex, String input) {
        if (null == regex || null == input) {
            return false;
        }
        return Pattern.matches(regex, input);
    }

    public static boolean matches(String regex, String... inputs) {
        if (null == regex || null == inputs || inputs.length == 0) {
            return false;
        }
        Pattern p = Pattern.compile(regex);
        for (String input : inputs) {
            if (p.matcher(input).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String regex, Collection<String> inputs) {
        if (null == regex || null == inputs || inputs.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile(regex);
        for (String input : inputs) {
            if (p.matcher(input).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拼接字符串第二个字符串第一个字母大写
     */
    public static String concatCapitalize(String concatStr, final String str) {
        if (hasText(concatStr)) {
            concatStr = "";
        }
        if (str == null || str.isEmpty()) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // already capitalized
            return str;
        }

        return concatStr + Character.toTitleCase(firstChar) + str.substring(1);
    }

    /**
     * 包含大写字母
     *
     * @param word 待判断字符串
     * @return ignore
     */
    public static boolean containsUpperCase(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否以某个字符串结尾（区分大小写）
     * Check if a String ends with a specified suffix.
     * <p>
     * <code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case-sensitive.
     * </p>
     * <p>
     * <pre>
     * StringUtils.endsWith(null, null)      = true
     * StringUtils.endsWith(null, "abcdef")  = false
     * StringUtils.endsWith("def", null)     = false
     * StringUtils.endsWith("def", "abcdef") = true
     * StringUtils.endsWith("def", "ABCDEF") = false
     * </pre>
     * </p>
     *
     * @param str    the String to check, may be null
     * @param suffix the suffix to find, may be null
     * @return <code>true</code> if the String ends with the suffix, case-sensitive, or both <code>null</code>
     * @see String#endsWith(String)
     * @since 2.4
     */
    public static boolean endsWith(String str, String suffix) {
        return endsWith(str, suffix, false);
    }

    /**
     * Check if a String ends with a specified suffix (optionally case-insensitive).
     *
     * @param str        the String to check, may be null
     * @param suffix     the suffix to find, may be null
     * @param ignoreCase indicates whether the compare should ignore case (case-insensitive) or not.
     * @return <code>true</code> if the String starts with the prefix or both
     * <code>null</code>
     * @see String#endsWith(String)
     */
    private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }

    /**
     * 是否为CharSequence类型
     *
     * @param clazz class
     * @return true 为是 CharSequence 类型
     */
    public static boolean isCharSequence(Class<?> clazz) {
        return clazz != null && CharSequence.class.isAssignableFrom(clazz);
    }

    /**
     * 前n个首字母小写,之后字符大小写的不变
     *
     * @param rawString 需要处理的字符串
     * @param index     多少个字符(从左至右)
     * @return ignore
     */
    public static String prefixToLower(String rawString, int index) {
        return rawString.substring(0, index).toLowerCase() + rawString.substring(index);
    }

    /**
     * 删除字符前缀之后,首字母小写,之后字符大小写的不变
     * <p>StringUtils.removePrefixAfterPrefixToLower( "isUser", 2 )     = user</p>
     * <p>StringUtils.removePrefixAfterPrefixToLower( "isUserInfo", 2 ) = userInfo</p>
     *
     * @param rawString 需要处理的字符串
     * @param index     删除多少个字符(从左至右)
     * @return ignore
     */
    public static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return prefixToLower(rawString.substring(index), 1);
    }

    /**
     * <p>比较两个字符串，相同则返回true。字符串可为null</p>
     *
     * <p>对字符串大小写敏感</p>
     *
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @param cs1 第一个字符串, 可为 {@code null}
     * @param cs2 第二个字符串, 可为 {@code null}
     * @return {@code true} 如果两个字符串相同, 或者都为 {@code null}
     * @see Object#equals(Object)
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去除不可见的字符，例如 \t, \n, \r
     *
     * @param str 原字符串
     * @return 去掉\t, \n, \r之后的结果
     */
    public static String trimInvisibleCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        int left = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\n' || str.charAt(i) != '\t') {
                break;
            }
            left++;
        }
        int right = str.length() - 1;
        for (int i = str.length() - 1; i > 0; i--) {
            if (i < left || str.charAt(i) != '\n') {
                break;
            }
            right--;
        }
        return str.substring(left, right + 1);
    }

    /**
     * 去掉首尾换行符
     *
     * @param str 字符串
     * @return 去掉首尾换行符
     */
    public static String trimWrapCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        int left = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\n') {
                break;
            }
            left++;
        }
        int right = str.length() - 1;
        for (int i = str.length() - 1; i > 0; i--) {
            if (i < left || str.charAt(i) != '\n') {
                break;
            }
            right--;
        }
        return str.substring(left, right + 1);
    }

    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     *
     * <pre>
     * StringUtils.removeEnd(null, *)      = null
     * StringUtils.removeEnd("", *)        = ""
     * StringUtils.removeEnd(*, null)      = *
     * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
     * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     * @since 2.1
     */
    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }


    public static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        if (!isEmpty(text) && !isEmpty(searchString) && replacement != null && max != 0) {
            int start = 0;
            int end = text.indexOf(searchString, start);
            if (end == -1) {
                return text;
            } else {
                int replLength = searchString.length();
                int increase = replacement.length() - replLength;
                increase = Math.max(increase, 0);
                increase *= max < 0 ? 16 : (Math.min(max, 64));

                StringBuilder buf;
                for (buf = new StringBuilder(text.length() + increase); end != -1; end = text.indexOf(searchString, start)) {
                    buf.append(text, start, end).append(replacement);
                    start = end + replLength;
                    --max;
                    if (max == 0) {
                        break;
                    }
                }

                buf.append(text.substring(start));
                return buf.toString();
            }
        } else {
            return text;
        }
    }

    /**
     * 比较两个字符串的相识度
     * 核心算法：用一个二维数组记录每个字符串是否相同，如果相同记为0，不相同记为1，每行每列相同个数累加
     * 则数组最后一个数为不相同的总数，从而判断这两个字符的相识度
     * Levenshtein Distance，又称编辑距离，指的是两个字符串之间，由一个转换成另一个所需的最少编辑操作次数。
     * 许可的编辑操作包括将一个字符替换成另一个字符，插入一个字符，删除一个字符。
     * <p>
     * 该算法的解决是基于动态规划的思想，具体如下：
     * 设 s 的长度为 n，t 的长度为 m。如果 n = 0，则返回 m 并退出；如果 m=0，则返回 n 并退出。否则构建一个数组 d[0..m, 0..n]。
     * 将第0行初始化为 0..n，第0列初始化为0..m。
     * 依次检查 s 的每个字母(i=1..n)。
     * 依次检查 t 的每个字母(j=1..m)。
     * 如果 s[i]=t[j]，则 cost=0；如果 s[i]!=t[j]，则 cost=1。将 d[i,j] 设置为以下三个值中的最小值：
     * 紧邻当前格上方的格的值加一，即 d[i-1,j]+1
     * 紧邻当前格左方的格的值加一，即 d[i,j-1]+1
     * 当前格左上方的格的值加cost，即 d[i-1,j-1]+cost
     * 重复3-6步直到循环结束。d[n,m]即为莱茵斯坦距离。
     * </p>
     *
     * @param left  字符序列1
     * @param right 字符序列2
     * @return 莱茵斯坦距离
     */
    private static int compare(CharSequence left, CharSequence right) {
        int[][] d;              // 矩阵
        int n = left.length();
        int m = right.length();
        int i;                  // 遍历left的
        int j;                  // 遍历right的
        char ch1;               // left的
        char ch2;               // right的
        int temp;               // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        // 初始化第一列
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        // 初始化第一行
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) {
            // 遍历left
            ch1 = left.charAt(i - 1);
            // 去匹配right
            for (j = 1; j <= m; j++) {
                ch2 = right.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = NumberUtils.min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }

    /**
     * 计算两字符串的相似度
     */
    public static float calculateSimilarity(String str, String target) {
        int max = Math.max(str.length(), target.length());
        return 1 - (float) compare(str, target) / max;
    }

    /**
     * 在字符串的指定位置插入子字符串
     *
     * @param original 原始字符串
     * @param toInsert 要插入的子字符串
     * @param position 插入的位置（索引从0开始）
     * @return 插入子字符串后的新字符串
     */
    public static String insertAt(String original, int position, String toInsert) {
        // 检查插入位置是否有效
        if (position < 0 || position > original.length()) {
            throw new IllegalArgumentException("illegal position to insert");
        }
        String part1 = original.substring(0, position);
        String part2 = original.substring(position);
        // 将这两部分和要插入的子字符串拼接起来
        return part1 + toInsert + part2;
    }

    /**
     * 使用正则表达式检查字符串是否只包含英文字母
     *
     * @param str the str to check
     * @return
     */
    public static boolean isAlphabetic(String str) {
        return str.matches("[a-zA-Z]+");
    }

    public static boolean endsWithAny(String str, String... targets) {
        if (targets == null) {
            return false;
        }
        for (String target : targets) {
            if (str.endsWith(target)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlankChar(Character character) {
        return CharUtil.isBlankChar(character);
    }

    public static boolean startWith(String pathToUse, char c) {
        return StrUtil.startWith(pathToUse, c);
    }

    public static String removePrefixIgnoreCase(String pathToUse, String s) {
        return StrUtil.removePrefixIgnoreCase(pathToUse, s);
    }

    public static String trimStart(String pathToUse) {
        return StrUtil.trim(pathToUse);
    }

    public static String trim(CharSequence str, int mode, Predicate<Character> predicate) {
        return StrUtil.trim(str, mode, predicate);
    }

    public static boolean contains(CharSequence charSequence, CharSequence charSequence1) {
        return StrUtil.contains(charSequence, charSequence1);
    }

    public static boolean contains(CharSequence charSequence, char c) {
        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    public static String toStringOrEmpty(Object o) {
        return StrUtil.toStringOrEmpty(o);
    }

    public static boolean containsIgnoreCase(CharSequence charSequence, CharSequence charSequence1) {
        return StrUtil.containsIgnoreCase(charSequence, charSequence1);
    }

    public static String[] splitToArray(String propertyName, String s) {
        return split(propertyName, s);
    }

    public static <T> List<T> splitToList(String str, Function<String, T> mapper) {
        String[] substrings = splitToArray(str, ",");
        List<T> list = new ArrayList<>();
        for (String substring : substrings) {
            list.add(mapper.apply(substring));
        }
        return list;
    }

    public static boolean equalsAnyIgnoreCase(CharSequence str1, CharSequence... strs) {
        return StrUtil.equalsAnyIgnoreCase(str1, strs);
    }

    /**
     * 指定范围内查找字符串，忽略大小写<br>
     *
     * <pre>
     * CharSequenceUtil.indexOfIgnoreCase(null, *, *)          = -1
     * CharSequenceUtil.indexOfIgnoreCase(*, null, *)          = -1
     * CharSequenceUtil.indexOfIgnoreCase("", "", 0)           = 0
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * CharSequenceUtil.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * CharSequenceUtil.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @return 位置
     * @since 3.2.1
     */
    public static int indexOfIgnoreCase(String str, String searchStr) {
        return StrUtil.indexOfIgnoreCase(str, searchStr);
    }

    public static List<String> splitTrim(CharSequence str, char separator) {
        return StrUtil.splitTrim(str, separator);
    }

    public static String blankToDefault(String from, String from1) {
        return StrUtil.blankToDefault(from, from1);
    }

    public static boolean isWrap(CharSequence str, char prefixChar, char suffixChar) {
        return StrUtil.isWrap(str, prefixChar, suffixChar);
    }

    public static String rightPad(String str, int numberLength, char c) {
        return str + String.valueOf(c).repeat(Math.max(0, numberLength));
    }

    public static String subWithLength(String input, int fromIndex, int length) {
        return StrUtil.subWithLength(input, fromIndex, length);
    }

    public static String stripEnd(String string, String separator) {
        return org.apache.commons.lang3.StringUtils.stripEnd(string, separator);
    }

    public static String toCamelCase(String s) {
        return StrUtil.toCamelCase(s);
    }

    public static String substringBetween(String columnType, String s, String s1) {
        return org.apache.commons.lang3.StringUtils.substringBetween(columnType, s, s1);
    }

    public static String substringBefore(String columnType, String s) {
        return org.apache.commons.lang3.StringUtils.substringBefore(columnType, s);
    }

    public static int indexOf(String columnType, String s) {
        return org.apache.commons.lang3.StringUtils.indexOf(columnType, s);
    }

    public static String lowerCase(String tableName) {
        return org.apache.commons.lang3.StringUtils.lowerCase(tableName);
    }

    public static boolean containsAnyIgnoreCase(String name, String[] tableIgnore) {
        return org.apache.commons.lang3.StringUtils.containsAnyIgnoreCase(name, tableIgnore);
    }

    public static boolean startsWith(String message, String s) {
        return StrUtil.startWith(message, s);
    }

    public static String remove(String code, String s) {
        return org.apache.commons.lang3.StringUtils.remove(code, s);
    }

    public static String removeStart(String key, String cmdstat) {
        return org.apache.commons.lang3.StringUtils.removeStart(key, cmdstat);
    }

    public static String substringAfterLast(String key, String s) {
        return org.apache.commons.lang3.StringUtils.substringAfterLast(key, s);
    }

    public static boolean ishttp(String path) {
        return HttpUtil.isHttp(path);
    }

    public static byte[] utf8Bytes(String xml) {
        return StrUtil.utf8Bytes(xml);
    }

    public static String utf8Str(Object xml) {
        return StrUtil.utf8Str(xml);
    }

    public static String replaceEach(String path, String[] strings, String[] strings1) {
        return org.apache.commons.lang3.StringUtils.replaceEach(path, strings, strings1);
    }

    public static String[] splitPreserveAllTokens(String mapperPackage, String configLocationDelimiters) {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(mapperPackage, configLocationDelimiters);
    }

    public static String toUnderScoreCase(String orderBy) {
        return StrUtil.toUnderlineCase(orderBy);
    }

    public static boolean isAnyEmpty(CharSequence... css) {
        return org.apache.commons.lang3.StringUtils.isAnyEmpty(css);
    }

    public static String trimToEmpty(String header) {
        return StrUtil.trimToEmpty(header);
    }

    /**
     * 兼容使用习惯
     *
     * @param str 字符串
     * @return 是否为null或者为空
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean parseBoolean(String str, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Throwable throwable) {
            return defaultValue;
        }
    }
}
