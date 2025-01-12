package io.devpl.sdk.util;

/**
 * StringEscaper ，数据库字符串转义
 */
public class StringEscaper {

    /**
     * <p>
     * 字符串是否需要转义
     * </p>
     *
     * @param str 原字符串
     * @param len 长度
     * @return 是否需要转义
     */
    private static boolean isEscapeNeededForString(String str, int len) {
        boolean needsHexEscape = false;
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (isNeedEscaped(c)) {
                // no need to scan more
                needsHexEscape = true;
                break;
            }
        }
        return needsHexEscape;
    }

    private static boolean isNeedEscaped(char c) {
        return switch (c) {
            /* Must be escaped for 'mysql' */
            case 0 -> true;
            /* Must be escaped for logs */
            case '\n' -> true;
            case '\r', '\\', '\'' -> true;
            /* Better safe than sorry */
            case '"' -> true;
            /* This gives problems on Win32 */
            case '\032' -> true;
            default -> false;
        };
    }

    /**
     * 转义字符串
     *
     * @param escapeStr 待转义字符串
     * @return 字符串
     */
    public static String escapeString(String escapeStr) {
        if (escapeStr.matches("'(.+)'")) {
            escapeStr = escapeStr.substring(1, escapeStr.length() - 1);
        }
        String parameterAsString = escapeStr;
        int stringLength = escapeStr.length();
        if (isEscapeNeededForString(escapeStr, stringLength)) {
            StringBuilder buf = new StringBuilder((int) (escapeStr.length() * 1.1));
            //
            // Note: buf.append(char) is _faster_ than appending in blocks,
            // because the block append requires a System.arraycopy().... go figure...
            //
            for (int i = 0; i < stringLength; ++i) {
                char c = escapeStr.charAt(i);
                switch (c) {
                    case 0: /* Must be escaped for 'mysql' */
                        buf.append('\\');
                        buf.append('0');
                        break;
                    case '\n': /* Must be escaped for logs */
                        buf.append('\\');
                        buf.append('n');
                        break;
                    case '\r':
                        buf.append('\\');
                        buf.append('r');
                        break;
                    case '\\':
                        buf.append('\\');
                        buf.append('\\');
                        break;
                    case '\'':
                        buf.append('\\');
                        buf.append('\'');
                        break;
                    case '"': /* Better safe than sorry */
                        buf.append('\\');
                        buf.append('"');
                        break;
                    case '\032': /* This gives problems on Win32 */
                        buf.append('\\');
                        buf.append('Z');
                        break;
                    default:
                        buf.append(c);
                }
            }
            parameterAsString = buf.toString();
        }
        return "'" + parameterAsString + "'";
    }
}
