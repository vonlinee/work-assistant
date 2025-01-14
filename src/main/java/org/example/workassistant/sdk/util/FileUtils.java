package org.example.workassistant.sdk.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.visitor.DelVisitor;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.apache.commons.lang3.stream.LangCollectors;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.util.AntPathMatcher;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 文件处理工具类
 *

 */
public class FileUtils extends FileUtil {

    public static final String SEPARATOR = ",";

    public static final String SLASH = "/";
    public static final String EMPTY = " ";

    /**
     * 获取临时文件路径（绝对路径）
     *
     * @return 临时文件路径
     * @since 4.0.6
     */
    public static String getTmpDirPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 获取临时文件目录
     *
     * @return 临时文件目录
     * @since 4.0.6
     */
    public static File getTmpDir() {
        return file(getTmpDirPath());
    }

    /**
     * 从URL对象中获取不被编码的路径Path<br>
     * 对于本地路径，URL对象的getPath方法对于包含中文或空格时会被编码，导致本读路径读取错误。<br>
     * 此方法将URL转为URI后获取路径用于解决路径被编码的问题
     *
     * @param url {@link URL}
     * @return 路径
     * @since 3.0.8
     */
    public static String getDecodedPath(URL url) {
        if (null == url) {
            return null;
        }
        String path = null;
        try {
            // URL对象的getPath方法对于包含中文或空格的问题
            path = new URI(url.toString().trim()).getPath();
        } catch (URISyntaxException e) {
            // ignore
        }
        return (null != path) ? path : url.getPath();
    }

    /**
     * 修复路径<br>
     * 如果原路径尾部有分隔符，则保留为标准分隔符（/），否则不保留
     * <ol>
     * <li>1. 统一用 /</li>
     * <li>2. 多个 / 转换为一个 /</li>
     * <li>3. 去除左边空格</li>
     * <li>4. .. 和 . 转换为绝对路径，当..多于已有路径时，直接返回根路径</li>
     * </ol>
     * <p>
     * 栗子：
     *
     * <pre>
     * "/foo//" =》 "/foo/"
     * "/foo/./" =》 "/foo/"
     * "/foo/../bar" =》 "/bar"
     * "/foo/../bar/" =》 "/bar/"
     * "/foo/../bar/../baz" =》 "/baz"
     * "/../" =》 "/"
     * "foo/bar/.." =》 "foo"
     * "foo/../bar" =》 "bar"
     * "foo/../../bar" =》 "bar"
     * "//server/foo/../bar" =》 "/server/bar"
     * "//server/../bar" =》 "/bar"
     * "C:\\foo\\..\\bar" =》 "C:/bar"
     * "C:\\..\\bar" =》 "C:/bar"
     * "~/foo/../bar/" =》 "~/bar/"
     * "~/../bar" =》 普通用户运行是'bar的home目录'，ROOT用户运行是'/bar'
     * </pre>
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }

        //兼容Windows下的共享目录路径（原始路径如果以\\开头，则保留这种路径）
        if (path.startsWith("\\\\")) {
            return path;
        }
        // 兼容Spring风格的ClassPath路径，去除前缀，不区分大小写
        String pathToUse = StringUtils.removePrefixIgnoreCase(path, "classpath:");
        // 去除file:前缀
        pathToUse = StringUtils.removePrefixIgnoreCase(pathToUse, "file:");

        // 识别home目录形式，并转换为绝对路径
        if (StringUtils.startWith(pathToUse, '~')) {
            pathToUse = getUserHomePath() + pathToUse.substring(1);
        }

        // 统一使用斜杠
        pathToUse = pathToUse.replaceAll("[/\\\\]+", StringUtils.SLASH);
        // 去除开头空白符，末尾空白符合法，不去除
        pathToUse = StringUtils.trimStart(pathToUse);
        // issue#IAB65V 去除尾部的换行符
        pathToUse = StringUtils.trim(pathToUse, 1, (c) -> c == '\n' || c == '\r');

        String prefix = StringUtils.EMPTY;
        int prefixIndex = pathToUse.indexOf(StringUtils.COLON);
        if (prefixIndex > -1) {
            // 可能Windows风格路径
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.startsWith("/")) {
                // 去除类似于/C:这类路径开头的斜杠
                prefix = prefix.substring(1);
            }
            if (!prefix.contains("/")) {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            } else {
                // 如果前缀中包含/,说明非Windows风格path
                prefix = StringUtils.EMPTY;
            }
        }
        if (pathToUse.startsWith(StringUtils.SLASH)) {
            prefix += StringUtils.SLASH;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathList = StringUtils.split(pathToUse, "/");
        List<String> pathElements = new LinkedList<>();
        int tops = 0;
        String element;
        for (int i = pathList.length - 1; i >= 0; i--) {
            element = pathList[i];
            // 只处理非.的目录，即只处理非当前目录
            if (!".".equals(element)) {
                if ("..".equals(element)) {
                    tops++;
                } else {
                    if (tops > 0) {
                        // 有上级目录标记时按照个数依次跳过
                        tops--;
                    } else {
                        // Normal path element found.
                        pathElements.add(0, element);
                    }
                }
            }
        }

        // issue#1703@Github
        if (tops > 0 && StringUtils.isEmpty(prefix)) {
            // 只有相对路径补充开头的..，绝对路径直接忽略之
            while (tops-- > 0) {
                //遍历完节点发现还有上级标注（即开头有一个或多个..），补充之
                // Normal path element found.
                pathElements.add(0, StringUtils.DOUBLE_DOT);
            }
        }
        return prefix + StringUtils.join(pathElements, "/");
    }

    /**
     * 删除文件或者文件夹，不追踪软链<br>
     * 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹<br>
     * 某个文件删除失败会终止删除操作
     *
     * @param path 文件对象
     * @return 成功与否
     * @throws IORuntimeException IO异常
     * @since 4.4.2
     */
    public static boolean del(Path path) throws IORuntimeException {
        if (Files.notExists(path)) {
            return true;
        }
        try {
            if (isDirectory(path)) {
                Files.walkFileTree(path, DelVisitor.INSTANCE);
            } else {
                delFile(path);
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return true;
    }

    /**
     * 如果字符串是{@code null}或者&quot;&quot;或者空白，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * blankToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * blankToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;default&quot;
     * blankToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;default&quot;
     * blankToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     * @since 4.1.0
     */
    public static String blankToDefault(CharSequence str, String defaultStr) {
        return isBlank(str) ? defaultStr : str.toString();
    }

    /**
     * <p>字符串是否为空，空的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isEmpty(null)     // true}</li>
     *     <li>{@code CharSequenceUtil.isEmpty("")       // true}</li>
     *     <li>{@code CharSequenceUtil.isEmpty(" \t\n")  // false}</li>
     *     <li>{@code CharSequenceUtil.isEmpty("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议用于工具类或任何可以预期的方法参数的校验中。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasEmpty(CharSequence...)} 或 {@link #isAllEmpty(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 是否为空
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.hasEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "")         // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去空格
     */
    public static String trim(String str) {
        return (null == str) ? null : trim(str, 0);
    }

    /**
     * 除去字符串头尾部的空白符，如果字符串是{@code null}，依然返回{@code null}。
     *
     * @param str  要处理的字符串
     * @param mode {@code -1}表示trimStart，{@code 0}表示trim全部， {@code 1}表示trimEnd
     * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(CharSequence str, int mode) {
        return trim(str, mode, StringUtils::isBlankChar);
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start) {
        return substring(str, start, str.length());
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @param end   结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end) {
        return sub(str, start, end);
    }

    /**
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is {} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params   参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean ishttp(String link) {
        return isUrl(link);
    }

    public static boolean isUrl(CharSequence value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            new java.net.URL(value.toString());
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * 字符串转set
     *
     * @param str 字符串
     * @param sep 分隔符
     * @return set集合
     */
    public static Set<String> str2Set(String str, String sep) {
        return new HashSet<>(str2List(str, sep, true, false));
    }

    /**
     * 字符串转list
     *
     * @param str         字符串
     * @param sep         分隔符
     * @param filterBlank 过滤纯空白
     * @param trim        去掉首尾空白
     * @return list集合
     */
    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList<>();
        if (isEmpty(str)) {
            return list;
        }

        // 过滤空白字符串
        if (filterBlank && isBlank(str)) {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split) {
            if (filterBlank && isBlank(string)) {
                continue;
            }
            if (trim) {
                string = trim(string);
            }
            list.add(string);
        }

        return list;
    }

    /**
     * <p>字符串是否为空白，空白的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isBlank(null)     // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("")       // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank(" \t\n")  // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢。</p>
     * <br>
     *
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasBlank(CharSequence...)} 或 {@link #isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(CharSequence str) {
        final int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否空白符<br>
     * 空白符包括空格、制表符、全角空格和不间断空格<br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     * @since 4.0.10
     */
    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c)
               || Character.isSpaceChar(c)
               || c == '\ufeff'
               || c == '\u202a'
               || c == '\u0000'
               // issue#I5UGSQ，Hangul Filler
               || c == '\u3164'
               // Braille Pattern Blank
               || c == '\u2800'
               // MONGOLIAN VOWEL SEPARATOR
               || c == '\u180e';
    }


    /**
     * <p>指定字符串数组中，是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.hasBlank()                  // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("", null, " ")     // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("123", " ")        // true}</li>
     *     <li>{@code CharSequenceUtil.hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasBlank(CharSequence...)            等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str) {
        return StrUtil.toUnderlineCase(str);
    }


    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllBlank()                  // true}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("", null, " ")     // true}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("123", " ")        // false}</li>
     *     <li>{@code CharSequenceUtil.isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasBlank(CharSequence...)}   等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>isAllBlank(CharSequence...)          等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strs 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... strs) {
        if (ArrayUtil.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     *     <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isNotBlank(null)     // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("")       // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢。</p>
     * <p>建议：仅对于客户端（或第三方接口）传入的参数使用该方法。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isNotEmpty(null)     // false}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty("")       // false}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code CharSequenceUtil.isNotEmpty("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：该方法建议用于工具类或任何可以预期的方法参数的校验中。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 是否包含字符串
     *
     * @param str  验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        return equalsAnyIgnoreCase(str, strs);
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        return upperFirst(StrUtil.toCamelCase(name));
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
                return Character.toUpperCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 驼峰式命名法 例如：user_name->userName
     */
    public static String toCamelCase(String s) {
        return StrUtil.toCamelCase(s);
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     *
     * @param str  指定字符串
     * @param strs 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strs) {
        if (isEmpty(str) || CollectionUtils.isEmpty(strs)) {
            return false;
        }
        for (String pattern : strs) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则配置:
     * ? 表示单个字符;
     * * 表示一层路径内的任意字符串，不可跨层级;
     * ** 表示任意层路径;
     *
     * @param pattern 匹配规则
     * @param url     需要匹配的url
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 数字左边补齐0，使之达到指定长度。注意，如果数字转换为字符串后，长度大于size，则只保留 最后size个字符。
     *
     * @param num  数字对象
     * @param size 字符串指定长度
     * @return 返回数字的字符串格式，该字符串为指定长度。
     */
    public static String padl(final Number num, final int size) {
        return padl(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
     *
     * @param s    原始字符串
     * @param size 字符串指定长度
     * @param c    用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
     */
    public static String padl(final String s, final int size, final char c) {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null) {
            final int len = s.length();
            if (s.length() <= size) {
                sb.append(String.valueOf(c).repeat(size - len));
                sb.append(s);
            } else {
                return s.substring(len - size, len);
            }
        } else {
            sb.append(String.valueOf(c).repeat(Math.max(0, size)));
        }
        return sb.toString();
    }

    /**
     * 切分字符串(分隔符默认逗号)
     *
     * @param str 被切分的字符串
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str) {
        return splitTo(str, Convert::toStr);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str, String separator) {
        return splitTo(str, separator, Convert::toStr);
    }

    /**
     * 切分字符串自定义转换(分隔符默认逗号)
     *
     * @param str    被切分的字符串
     * @param mapper 自定义转换
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, Function<String, T> mapper) {
        return splitTo(str, SEPARATOR, mapper);
    }

    /**
     * 切分字符串自定义转换
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @param mapper    自定义转换
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, String separator, Function<String, T> mapper) {
        if (isBlank(str)) {
            return new ArrayList<>(0);
        }
        return StringUtils.splitToList(str, separator)
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toList());
    }


    /**
     * 切分字符串，如果分隔符不存在则返回原字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     * @since 5.7.1
     */
    public static List<String> split(CharSequence str, CharSequence separator) {
        return split(str, separator, false, false);
    }

    public static String[] splitToArray(CharSequence str, CharSequence separator) {
        return split(str, separator, false, false).toArray(new String[0]);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 5.6.7
     */
    public static List<String> split(CharSequence str, CharSequence separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, 0, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     * @since 3.2.0
     */
    public static List<String> split(CharSequence str, CharSequence separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        final String separatorStr = (null == separator) ? null : separator.toString();
        return StrSplitter.split(str, separatorStr, limit, isTrim, ignoreEmpty);
    }

    /**
     * 忽略大小写去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串，若前缀不是 prefix， 返回原字符串
     */
    public static String removePrefixIgnoreCase(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return "";
        }
        final String str2 = str.toString();
        if (startWithIgnoreCase(str, prefix)) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 是否以指定字符串开头，忽略大小写
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
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
            return "";
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
            return "";
        }

        return str.toString().substring(fromIndexInclude, toIndexExclude);
    }

    /**
     * 字符串是否以给定字符开始
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否开始
     */
    public static boolean startWith(CharSequence str, char c) {
        if (isEmpty(str)) {
            return false;
        }
        return c == str.charAt(0);
    }

    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param str        被监测字符串
     * @param prefix     开头字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     * @since 5.4.3
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        return startWith(str, prefix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false<br>
     * <pre>
     *     CharSequenceUtil.startWith("123", "123", false, true);   -- false
     *     CharSequenceUtil.startWith("ABCDEF", "abc", true, true); -- true
     *     CharSequenceUtil.startWith("abc", "abc", true, true);    -- false
     * </pre>
     *
     * @param str          被监测字符串
     * @param prefix       开头字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串开头
     * @since 5.4.3
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == prefix) {
            if (ignoreEquals) {
                return false;
            }
            return null == str && null == prefix;
        }
        boolean isStartWith = str.toString()
            .regionMatches(ignoreCase, 0, prefix.toString(), 0, prefix.length());

        if (isStartWith) {
            return !ignoreEquals || !equals(str, prefix, ignoreCase);
        }
        return false;
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

    /**
     * 按照断言，除去字符串头尾部的断言为真的字符，如果字符串是{@code null}，依然返回{@code null}。
     *
     * @param str       要处理的字符串
     * @param mode      {@code -1}表示trimStart，{@code 0}表示trim全部， {@code 1}表示trimEnd
     * @param predicate 断言是否过掉字符，返回{@code true}表述过滤掉，{@code false}表示不过滤
     * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(CharSequence str, int mode, Predicate<Character> predicate) {
        String result;
        if (str == null) {
            result = null;
        } else {
            int length = str.length();
            int start = 0;
            int end = length;// 扫描字符串头部
            if (mode <= 0) {
                while ((start < end) && (predicate.test(str.charAt(start)))) {
                    start++;
                }
            }// 扫描字符串尾部
            if (mode >= 0) {
                while ((start < end) && (predicate.test(str.charAt(end - 1)))) {
                    end--;
                }
            }
            if ((start > 0) || (end < length)) {
                result = str.toString().substring(start, end);
            } else {
                result = str.toString();
            }
        }
        return result;
    }

    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        return replace(text, searchString, replacement, max, false);
    }

    public static int indexOf(CharSequence seq, CharSequence searchSeq) {
        return seq != null && searchSeq != null ? CharSequenceUtils.indexOf(seq, searchSeq, 0) : -1;
    }

    public static int indexOf(CharSequence seq, CharSequence searchSeq, int startPos) {
        return seq != null && searchSeq != null ? CharSequenceUtils.indexOf(seq, searchSeq, startPos) : -1;
    }

    public static int indexOfAny(CharSequence cs, char... searchChars) {
        if (!isEmpty(cs) && !ArrayUtils.isEmpty(searchChars)) {
            int csLen = cs.length();
            int csLast = csLen - 1;
            int searchLen = searchChars.length;
            int searchLast = searchLen - 1;

            for (int i = 0; i < csLen; ++i) {
                char ch = cs.charAt(i);

                for (int j = 0; j < searchLen; ++j) {
                    if (searchChars[j] == ch) {
                        if (i >= csLast || j >= searchLast || !Character.isHighSurrogate(ch)) {
                            return i;
                        }

                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
                            return i;
                        }
                    }
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    private static String replace(String text, String searchString, String replacement, int max, boolean ignoreCase) {
        if (!isEmpty(text) && !isEmpty(searchString) && replacement != null && max != 0) {
            if (ignoreCase) {
                searchString = searchString.toLowerCase();
            }

            int start = 0;
            int end = ignoreCase ? indexOfIgnoreCase(text, searchString, start) : indexOf(text, searchString, start);
            if (end == -1) {
                return text;
            } else {
                int replLength = searchString.length();
                int increase = Math.max(replacement.length() - replLength, 0);
                increase *= max < 0 ? 16 : Math.min(max, 64);

                StringBuilder buf;
                for (buf = new StringBuilder(text.length() + increase); end != -1; end = ignoreCase ? indexOfIgnoreCase(text, searchString, start) : indexOf(text, searchString, start)) {
                    buf.append(text, start, end).append(replacement);
                    start = end + replLength;
                    --max;
                    if (max == 0) {
                        break;
                    }
                }

                buf.append(text, start, text.length());
                return buf.toString();
            }
        } else {
            return text;
        }
    }

    public static int indexOfIgnoreCase(CharSequence str, CharSequence searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    public static int indexOfIgnoreCase(CharSequence str, CharSequence searchStr, int startPos) {
        if (str != null && searchStr != null) {
            if (startPos < 0) {
                startPos = 0;
            }

            int endLimit = str.length() - searchStr.length() + 1;
            if (startPos > endLimit) {
                return -1;
            } else if (searchStr.isEmpty()) {
                return startPos;
            } else {
                for (int i = startPos; i < endLimit; ++i) {
                    if (regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
                        return i;
                    }
                }
                return -1;
            }
        } else {
            return -1;
        }
    }


    static boolean regionMatches(CharSequence cs, boolean ignoreCase, int thisStart, CharSequence substring, int start, int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
        } else {
            int index1 = thisStart;
            int index2 = start;
            int tmpLen = length;
            int srcLen = cs.length() - thisStart;
            int otherLen = substring.length() - start;
            if (thisStart >= 0 && start >= 0 && length >= 0) {
                if (srcLen >= length && otherLen >= length) {
                    while (tmpLen-- > 0) {
                        char c1 = cs.charAt(index1++);
                        char c2 = substring.charAt(index2++);
                        if (c1 != c2) {
                            if (!ignoreCase) {
                                return false;
                            }

                            char u1 = Character.toUpperCase(c1);
                            char u2 = Character.toUpperCase(c2);
                            if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                                return false;
                            }
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }


    public static String join(boolean[] array, char delimiter) {
        return array == null ? null : join((boolean[]) array, delimiter, 0, array.length);
    }

    public static String join(boolean[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder(array.length * 5 + array.length - 1);

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(byte[] array, char delimiter) {
        return array == null ? null : join((byte[]) array, delimiter, 0, array.length);
    }

    public static String join(byte[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(char[] array, char delimiter) {
        return array == null ? null : join((char[]) array, delimiter, 0, array.length);
    }

    public static String join(char[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder(array.length * 2 - 1);

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(double[] array, char delimiter) {
        return array == null ? null : join((double[]) array, delimiter, 0, array.length);
    }

    public static String join(double[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(float[] array, char delimiter) {
        return array == null ? null : join((float[]) array, delimiter, 0, array.length);
    }

    public static String join(float[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(int[] array, char separator) {
        return array == null ? null : join((int[]) array, separator, 0, array.length);
    }

    public static String join(int[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(Iterable<?> iterable, char separator) {
        return iterable != null ? join(iterable.iterator(), separator) : null;
    }

    public static String join(Iterable<?> iterable, String separator) {
        return iterable != null ? join(iterable.iterator(), separator) : null;
    }

    public static String join(Iterator<?> iterator, char separator) {
        if (iterator == null) {
            return null;
        } else {
            return !iterator.hasNext() ? "" : Streams.of(iterator).collect(LangCollectors.joining(toStringOrEmpty(String.valueOf(separator)), "", "", StringUtils::toStringOrEmpty));
        }
    }

    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        } else {
            return !iterator.hasNext() ? "" : Streams.of(iterator).collect(LangCollectors.joining(toStringOrEmpty(separator), "", "", StringUtils::toStringOrEmpty));
        }
    }

    public static String join(List<?> list, char separator, int startIndex, int endIndex) {
        if (list == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                List<?> subList = list.subList(startIndex, endIndex);
                return join(subList.iterator(), separator);
            }
        }
    }

    public static String join(List<?> list, String separator, int startIndex, int endIndex) {
        if (list == null) {
            return null;
        } else {
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0) {
                return "";
            } else {
                List<?> subList = list.subList(startIndex, endIndex);
                return join(subList.iterator(), separator);
            }
        }
    }

    public static String join(long[] array, char separator) {
        return array == null ? null : join((long[]) array, separator, 0, array.length);
    }

    public static String join(long[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    public static String join(Object[] array, char delimiter) {
        return array == null ? null : join((Object[]) array, delimiter, 0, array.length);
    }

    public static String join(Object[] array, char delimiter, int startIndex, int endIndex) {
        return join(array, String.valueOf(delimiter), startIndex, endIndex);
    }

    public static String join(Object[] array, String delimiter) {
        return array != null ? join((Object[]) array, toStringOrEmpty(delimiter), 0, array.length) : null;
    }

    public static String join(Object[] array, String delimiter, int startIndex, int endIndex) {
        return array != null ? Streams.of(array).skip(startIndex).limit(Math.max(0, endIndex - startIndex)).collect(LangCollectors.joining(delimiter, "", "", StringUtils::toStringOrEmpty)) : null;
    }

    public static String join(short[] array, char delimiter) {
        return array == null ? null : join((short[]) array, delimiter, 0, array.length);
    }

    public static String join(short[] array, char delimiter, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        } else if (endIndex - startIndex <= 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = startIndex; i < endIndex; ++i) {
                stringBuilder.append(array[i]).append(delimiter);
            }

            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }

    @SafeVarargs
    public static <T> String join(T... elements) {
        return join((Object[]) elements, (String) null);
    }

    public static String joinWith(String delimiter, Object... array) {
        if (array == null) {
            throw new IllegalArgumentException("Object varargs must not be null");
        } else {
            return join(array, delimiter);
        }
    }

    private static String toStringOrEmpty(Object obj) {
        return Objects.toString(obj, "");
    }

    public static boolean endsWith(CharSequence str, CharSequence suffix) {
        return endsWith(str, suffix, false);
    }

    private static boolean endsWith(CharSequence str, CharSequence suffix, boolean ignoreCase) {
        if (str != null && suffix != null) {
            if (suffix.length() > str.length()) {
                return false;
            } else {
                int strOffset = str.length() - suffix.length();
                return CharSequenceUtils.regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
            }
        } else {
            return str == suffix;
        }
    }

    public static boolean endsWithAny(CharSequence sequence, CharSequence... searchStrings) {
        if (!isEmpty(sequence) && !ArrayUtils.isEmpty(searchStrings)) {
            for (CharSequence searchString : searchStrings) {
                if (endsWith(sequence, searchString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean endsWithIgnoreCase(CharSequence str, CharSequence suffix) {
        return endsWith(str, suffix, true);
    }

    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        } else if (cs1 != null && cs2 != null) {
            if (cs1.length() != cs2.length()) {
                return false;
            } else if (cs1 instanceof String && cs2 instanceof String) {
                return cs1.equals(cs2);
            } else {
                int length = cs1.length();

                for (int i = 0; i < length; ++i) {
                    if (cs1.charAt(i) != cs2.charAt(i)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean equalsAny(CharSequence string, CharSequence... searchStrings) {
        if (ArrayUtils.isNotEmpty(searchStrings)) {
            for (CharSequence next : searchStrings) {
                if (equals(string, next)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean equalsAnyIgnoreCase(CharSequence string, CharSequence... searchStrings) {
        if (ArrayUtils.isNotEmpty(searchStrings)) {
            for (CharSequence next : searchStrings) {
                if (equalsIgnoreCase(string, next)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean equalsIgnoreCase(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        } else if (cs1 != null && cs2 != null) {
            return cs1.length() == cs2.length() && CharSequenceUtils.regionMatches(cs1, true, 0, cs2, 0, cs1.length());
        } else {
            return false;
        }
    }

    @SafeVarargs
    public static <T extends CharSequence> T firstNonBlank(T... values) {
        if (values != null) {
            int var2 = values.length;
            for (int var3 = 0; var3 < var2; ++var3) {
                @SuppressWarnings("unchecked")
                T val = (T) ((CharSequence[]) values)[var3];
                if (isNotBlank(val)) {
                    return val;
                }
            }
        }
        return null;
    }

    public static String truncate(String str, int offset, int maxWidth) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        } else if (maxWidth < 0) {
            throw new IllegalArgumentException("maxWith cannot be negative");
        } else if (str == null) {
            return null;
        } else if (offset > str.length()) {
            return "";
        } else if (str.length() > maxWidth) {
            int ix = Math.min(offset + maxWidth, str.length());
            return str.substring(offset, ix);
        } else {
            return str.substring(offset);
        }
    }

    public static String uncapitalize(String str) {
        int strLen = CharSequenceUtils.length(str);
        if (strLen == 0) {
            return str;
        } else {
            int firstCodePoint = str.codePointAt(0);
            int newCodePoint = Character.toLowerCase(firstCodePoint);
            if (firstCodePoint == newCodePoint) {
                return str;
            } else {
                int[] newCodePoints = new int[strLen];
                int outOffset = 0;
                newCodePoints[outOffset++] = newCodePoint;

                int codePoint;
                for (int inOffset = Character.charCount(firstCodePoint); inOffset < strLen; inOffset += Character.charCount(codePoint)) {
                    codePoint = str.codePointAt(inOffset);
                    newCodePoints[outOffset++] = codePoint;
                }

                return new String(newCodePoints, 0, outOffset);
            }
        }
    }

    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static String capitalize(String str) {
        int strLen = length(str);
        if (strLen == 0) {
            return str;
        } else {
            int firstCodepoint = str.codePointAt(0);
            int newCodePoint = Character.toTitleCase(firstCodepoint);
            if (firstCodepoint == newCodePoint) {
                return str;
            } else {
                int[] newCodePoints = new int[strLen];
                int outOffset = 0;
                newCodePoints[outOffset++] = newCodePoint;

                int codePoint;
                for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; inOffset += Character.charCount(codePoint)) {
                    codePoint = str.codePointAt(inOffset);
                    newCodePoints[outOffset++] = codePoint;
                }

                return new String(newCodePoints, 0, outOffset);
            }
        }
    }

    public static String[] splitPreserveAllTokens(String str) {
        return splitWorker(str, (String) null, -1, true);
    }

    public static String[] splitPreserveAllTokens(String str, char separatorChar) {
        return splitWorker(str, separatorChar, true);
    }

    public static String[] splitPreserveAllTokens(String str, String separatorChars) {
        return splitWorker(str, separatorChars, -1, true);
    }

    public static String[] splitPreserveAllTokens(String str, String separatorChars, int max) {
        return splitWorker(str, separatorChars, max, true);
    }

    private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
        if (str == null) {
            return null;
        } else {
            int len = str.length();
            if (len == 0) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            } else {
                List<String> list = new ArrayList<>();
                int i = 0;
                int start = 0;
                boolean match = false;
                boolean lastMatch = false;

                while (true) {
                    while (i < len) {
                        if (str.charAt(i) == separatorChar) {
                            if (match || preserveAllTokens) {
                                list.add(str.substring(start, i));
                                match = false;
                                lastMatch = true;
                            }

                            ++i;
                            start = i;
                        } else {
                            lastMatch = false;
                            match = true;
                            ++i;
                        }
                    }

                    if (match || preserveAllTokens) {
                        list.add(str.substring(start, i));
                    }
                    return list.toArray(new String[0]);
                }
            }
        }
    }

    private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        if (str == null) {
            return null;
        } else {
            int len = str.length();
            if (len == 0) {
                return new String[0];
            } else {
                List<String> list = new ArrayList<>();
                int sizePlus1 = 1;
                int i = 0;
                int start = 0;
                boolean match = false;
                boolean lastMatch = false;
                if (separatorChars != null) {
                    if (separatorChars.length() == 1) {
                        char sep = separatorChars.charAt(0);
                    }
                    label87:
                    while (true) {
                        while (true) {
                            if (i >= len) {
                                break label87;
                            }

                            if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                                if (match || preserveAllTokens) {
                                    lastMatch = true;
                                    if (sizePlus1++ == max) {
                                        i = len;
                                        lastMatch = false;
                                    }

                                    list.add(str.substring(start, i));
                                    match = false;
                                }

                                ++i;
                                start = i;
                            } else {
                                lastMatch = false;
                                match = true;
                                ++i;
                            }
                        }
                    }
                } else {
                    label103:
                    while (true) {
                        while (true) {
                            if (i >= len) {
                                break label103;
                            }

                            if (Character.isWhitespace(str.charAt(i))) {
                                if (match || preserveAllTokens) {
                                    lastMatch = true;
                                    if (sizePlus1++ == max) {
                                        i = len;
                                        lastMatch = false;
                                    }

                                    list.add(str.substring(start, i));
                                    match = false;
                                }

                                ++i;
                                start = i;
                            } else {
                                lastMatch = false;
                                match = true;
                                ++i;
                            }
                        }
                    }
                }

                if (match || preserveAllTokens && lastMatch) {
                    list.add(str.substring(start, i));
                }

                return list.toArray(new String[0]);
            }
        }
    }

    public static boolean startsWith(CharSequence str, CharSequence prefix) {
        return startsWith(str, prefix, false);
    }

    private static boolean startsWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        if (str != null && prefix != null) {
            int preLen = prefix.length();
            return preLen <= str.length() && CharSequenceUtils.regionMatches(str, ignoreCase, 0, prefix, 0, preLen);
        } else {
            return str == prefix;
        }
    }

    public static boolean startsWithAny(CharSequence sequence, CharSequence... searchStrings) {
        if (!isEmpty(sequence) && !ArrayUtils.isEmpty(searchStrings)) {
            for (CharSequence searchString : searchStrings) {
                if (startsWith(sequence, searchString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int compareIgnoreCase(String str1, String str2) {
        return compareIgnoreCase(str1, str2, true);
    }

    public static int compareIgnoreCase(String str1, String str2, boolean nullIsLess) {
        if (Objects.equals(str1, str2)) {
            return 0;
        } else if (str1 == null) {
            return nullIsLess ? -1 : 1;
        } else if (str2 == null) {
            return nullIsLess ? 1 : -1;
        } else {
            return str1.compareToIgnoreCase(str2);
        }
    }

    public static boolean contains(CharSequence seq, CharSequence searchSeq) {
        if (seq != null && searchSeq != null) {
            return CharSequenceUtils.indexOf(seq, searchSeq, 0) >= 0;
        } else {
            return false;
        }
    }

    public static boolean contains(CharSequence seq, int searchChar) {
        if (isEmpty(seq)) {
            return false;
        } else {
            return CharSequenceUtils.indexOf(seq, searchChar, 0) >= 0;
        }
    }

    public static boolean containsAny(CharSequence cs, char... searchChars) {
        if (!isEmpty(cs) && !ArrayUtils.isEmpty(searchChars)) {
            int csLength = cs.length();
            int searchLength = searchChars.length;
            int csLast = csLength - 1;
            int searchLast = searchLength - 1;

            for (int i = 0; i < csLength; ++i) {
                char ch = cs.charAt(i);

                for (int j = 0; j < searchLength; ++j) {
                    if (searchChars[j] == ch) {
                        if (!Character.isHighSurrogate(ch)) {
                            return true;
                        }

                        if (j == searchLast) {
                            return true;
                        }

                        if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    public static boolean containsAny(CharSequence cs, CharSequence searchChars) {
        return searchChars != null && containsAny(cs, CharSequenceUtils.toCharArray(searchChars));
    }

    public static boolean containsAny(CharSequence cs, CharSequence... searchCharSequences) {
        return containsAny(StringUtils::contains, cs, searchCharSequences);
    }

    private static boolean containsAny(ToBooleanBiFunction<CharSequence, CharSequence> test, CharSequence cs, CharSequence... searchCharSequences) {
        if (!isEmpty(cs) && !ArrayUtils.isEmpty(searchCharSequences)) {
            for (CharSequence searchCharSequence : searchCharSequences) {
                if (test.applyAsBoolean(cs, searchCharSequence)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串同时串忽略大小写
     *
     * @param cs                  指定字符串
     * @param searchCharSequences 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences) {
        return containsAny(StringUtils::containsIgnoreCase, cs, searchCharSequences);
    }

    public static boolean containsIgnoreCase(CharSequence str, CharSequence searchStr) {
        if (str != null && searchStr != null) {
            int len = searchStr.length();
            int max = str.length() - len;

            for (int i = 0; i <= max; ++i) {
                if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean startsWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startsWith(str, prefix, true);
    }


    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        return replaceEach(text, searchList, replacementList, false, 0);
    }

    private static String replaceEach(String text, String[] searchList, String[] replacementList, boolean repeat, int timeToLive) {
        if (timeToLive < 0) {
            Set<String> searchSet = new HashSet<>(Arrays.asList(searchList));
            Set<String> replacementSet = new HashSet<>(Arrays.asList(replacementList));
            searchSet.retainAll(replacementSet);
            if (!searchSet.isEmpty()) {
                throw new IllegalStateException("Aborting to protect against StackOverflowError - output of one loop is the input of another");
            }
        }

        if (isEmpty(text) || ArrayUtils.isEmpty(searchList) || ArrayUtils.isEmpty(replacementList) || ArrayUtils.isNotEmpty(searchList) && timeToLive == -1) {
            return text;
        } else {
            int searchLength = searchList.length;
            int replacementLength = replacementList.length;
            if (searchLength != replacementLength) {
                throw new IllegalArgumentException("Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength);
            } else {
                boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
                int textIndex = -1;
                int replaceIndex = -1;

                int tempIndex;
                int start;
                for (start = 0; start < searchLength; ++start) {
                    if (!noMoreMatchesForReplIndex[start] && !isEmpty(searchList[start]) && replacementList[start] != null) {
                        tempIndex = text.indexOf(searchList[start]);
                        if (tempIndex == -1) {
                            noMoreMatchesForReplIndex[start] = true;
                        } else if (textIndex == -1 || tempIndex < textIndex) {
                            textIndex = tempIndex;
                            replaceIndex = start;
                        }
                    }
                }

                if (textIndex == -1) {
                    return text;
                } else {
                    start = 0;
                    int increase = 0;

                    for (int i = 0; i < searchList.length; ++i) {
                        if (searchList[i] != null && replacementList[i] != null) {
                            i = replacementList[i].length() - searchList[i].length();
                            if (i > 0) {
                                increase += 3 * i;
                            }
                        }
                    }

                    increase = Math.min(increase, text.length() / 5);
                    StringBuilder buf = new StringBuilder(text.length() + increase);

                    while (textIndex != -1) {
                        for (int i = start; i < textIndex; ++i) {
                            buf.append(text.charAt(i));
                        }

                        buf.append(replacementList[replaceIndex]);
                        start = textIndex + searchList[replaceIndex].length();
                        textIndex = -1;
                        replaceIndex = -1;

                        for (int i = 0; i < searchLength; ++i) {
                            if (!noMoreMatchesForReplIndex[i] && !isEmpty(searchList[i]) && replacementList[i] != null) {
                                tempIndex = text.indexOf(searchList[i], start);
                                if (tempIndex == -1) {
                                    noMoreMatchesForReplIndex[i] = true;
                                } else if (textIndex == -1 || tempIndex < textIndex) {
                                    textIndex = tempIndex;
                                    replaceIndex = i;
                                }
                            }
                        }
                    }
                    String result = buf.toString();
                    if (!repeat) {
                        return result;
                    } else {
                        return replaceEach(result, searchList, replacementList, true, timeToLive - 1);
                    }
                }
            }
        }
    }

    public static String lowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    public static String lowerCase(String str, Locale locale) {
        return str == null ? null : str.toLowerCase(LocaleUtils.toLocale(locale));
    }


    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        } else {
            int pads = size - str.length();
            if (pads <= 0) {
                return str;
            } else {
                return pads > 8192 ? rightPad(str, size, String.valueOf(padChar)) : str.concat(repeat(padChar, pads));
            }
        }
    }


    public static String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        } else {
            char[] buf = new char[repeat];
            Arrays.fill(buf, ch);
            return new String(buf);
        }
    }

    public static String repeat(String str, int repeat) {
        if (str == null) {
            return null;
        } else if (repeat <= 0) {
            return "";
        } else {
            int inputLength = str.length();
            if (repeat != 1 && inputLength != 0) {
                if (inputLength == 1 && repeat <= 8192) {
                    return repeat(str.charAt(0), repeat);
                } else {
                    int outputLength = inputLength * repeat;
                    switch (inputLength) {
                        case 1:
                            return repeat(str.charAt(0), repeat);
                        case 2:
                            char ch0 = str.charAt(0);
                            char ch1 = str.charAt(1);
                            char[] output2 = new char[outputLength];

                            for (int i = repeat * 2 - 2; i >= 0; --i) {
                                output2[i] = ch0;
                                output2[i + 1] = ch1;
                                --i;
                            }

                            return new String(output2);
                        default:
                            return str.repeat(repeat);
                    }
                }
            } else {
                return str;
            }
        }
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        } else {
            if (isEmpty(padStr)) {
                padStr = " ";
            }
            int padLen = padStr.length();
            int strLen = str.length();
            int pads = size - strLen;
            if (pads <= 0) {
                return str;
            } else if (padLen == 1 && pads <= 8192) {
                return rightPad(str, size, padStr.charAt(0));
            } else if (pads == padLen) {
                return str.concat(padStr);
            } else if (pads < padLen) {
                return str.concat(padStr.substring(0, pads));
            } else {
                char[] padding = new char[pads];
                char[] padChars = padStr.toCharArray();

                for (int i = 0; i < pads; ++i) {
                    padding[i] = padChars[i % padLen];
                }
                return str.concat(new String(padding));
            }
        }
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static String stripEnd(String str, String stripChars) {
        int end = length(str);
        if (end == 0) {
            return str;
        } else {
            if (stripChars == null) {
                while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                    --end;
                }
            } else {
                if (stripChars.isEmpty()) {
                    return str;
                }
                while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                    --end;
                }
            }
            return str.substring(0, end);
        }
    }

    public static boolean isAnyEmpty(CharSequence... css) {
        if (!ArrayUtils.isEmpty(css)) {
            for (CharSequence cs : css) {
                if (isEmpty(cs)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String substringBetween(String str, String open, String close) {
        if (ObjectUtils.allNotNull(str, open, close)) {
            int start = str.indexOf(open);
            if (start != -1) {
                int end = str.indexOf(close, start + open.length());
                if (end != -1) {
                    return str.substring(start + open.length(), end);
                }
            }

        }
        return null;
    }

    public static String substringBefore(String str, int separator) {
        if (isEmpty(str)) {
            return str;
        } else {
            int pos = str.indexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        }
    }

    public static String substringBefore(String str, String separator) {
        if (!isEmpty(str) && separator != null) {
            if (separator.isEmpty()) {
                return "";
            } else {
                int pos = str.indexOf(separator);
                return pos == -1 ? str : str.substring(0, pos);
            }
        } else {
            return str;
        }
    }


    public static String substringAfterLast(String str, int separator) {
        if (isEmpty(str)) {
            return str;
        } else {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - 1 ? str.substring(pos + 1) : "";
        }
    }

    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (isEmpty(separator)) {
            return "";
        } else {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
        }
    }

    public static String removeStart(String str, char remove) {
        if (isEmpty(str)) {
            return str;
        } else {
            return str.charAt(0) == remove ? str.substring(1) : str;
        }
    }

    public static String removeStart(String str, String remove) {
        if (!isEmpty(str) && !isEmpty(remove)) {
            return str.startsWith(remove) ? str.substring(remove.length()) : str;
        } else {
            return str;
        }
    }

    public static String remove(String str, char remove) {
        if (!isEmpty(str) && str.indexOf(remove) != -1) {
            char[] chars = str.toCharArray();
            int pos = 0;

            for (int i = 0; i < chars.length; ++i) {
                if (chars[i] != remove) {
                    chars[pos++] = chars[i];
                }
            }

            return new String(chars, 0, pos);
        } else {
            return str;
        }
    }

    public static String remove(String str, String remove) {
        return !isEmpty(str) && !isEmpty(remove) ? replace(str, remove, "", -1) : str;
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     * @since 3.1.2
     */
    public static List<String> splitTrim(CharSequence str, char separator) {
        return StrUtil.splitTrim(str, separator, -1);
    }

    /**
     * 编码字符串，编码为UTF-8
     *
     * @param str 字符串
     * @return 编码后的字节码
     */
    public static byte[] utf8Bytes(CharSequence str) {
        return StrUtil.bytes(str, CharsetUtil.CHARSET_UTF_8);
    }

    public static String utf8Str(Object obj) {
        return StrUtil.utf8Str(obj);
    }

    /**
     * 通过正则表达式验证
     *
     * @param regex 正则
     * @param value 值
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(String regex, CharSequence value) {
        return ReUtil.isMatch(regex, value);
    }

    /**
     * 截取字符串,从指定位置开始,截取指定长度的字符串<br>
     * 如果fromIndex为正数，则向后截取指定length长度，如果为负数，则向前截取length长度。
     *
     * @param input     原始字符串
     * @param fromIndex 开始的index,包括
     * @param length    要截取的长度
     * @return 截取后的字符串
     * @author weibaohui
     */
    public static String subWithLength(String input, int fromIndex, int length) {
        final int toIndex;
        if (fromIndex < 0) {
            toIndex = fromIndex - length;
        } else {
            toIndex = fromIndex + length;
        }
        return sub(input, fromIndex, toIndex);
    }

    /**
     * 特殊后缀
     */
    private static final CharSequence[] SPECIAL_SUFFIX = {"tar.bz2", "tar.Z", "tar.gz", "tar.xz"};

    /**
     * 获得文件的扩展名（后缀名），扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getExtensionName(String fileName) {
        if (fileName == null) {
            return null;
        }
        final int index = fileName.lastIndexOf(StrUtil.DOT);
        if (index == -1) {
            return StrUtil.EMPTY;
        } else {
            final int secondToLastIndex = fileName.substring(0, index).lastIndexOf(StrUtil.DOT);
            final String substr = fileName.substring(secondToLastIndex == -1 ? index : secondToLastIndex + 1);
            if (StringUtils.containsAny(substr, SPECIAL_SUFFIX)) {
                return substr;
            }
            final String ext = fileName.substring(index + 1);
            // 扩展名中不能包含路径相关的符号
            // unix: /
            // windows: \\
            return StrUtil.containsAny(ext, "/", "\\") ? StrUtil.EMPTY : ext;
        }
    }
}
