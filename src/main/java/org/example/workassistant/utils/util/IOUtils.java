package org.example.workassistant.utils.util;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;

public class IOUtils {

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 从Reader中读取String，读取完毕后关闭Reader
     *
     * @param reader Reader
     * @return String
     * @throws IORuntimeException IO异常
     */
    public static String readString(Reader reader) throws IORuntimeException {
        return IoUtil.read(reader, true);
    }

    /**
     * 获得一个Writer
     *
     * @param out     输入流
     * @param charset 字符集
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter getWriter(OutputStream out, Charset charset) {
        if (null == out) {
            return null;
        }

        if (null == charset) {
            return new OutputStreamWriter(out);
        } else {
            return new OutputStreamWriter(out, charset);
        }
    }

    /**
     * 将多部分内容写到流中，自动转换为字符串
     *
     * @param out        输出流
     * @param charset    写出的内容的字符集
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容，调用toString()方法，不包括不会自动换行
     * @throws IORuntimeException IO异常
     * @since 3.0.9
     */
    public static void write(OutputStream out, Charset charset, boolean isCloseOut, Object... contents) throws IORuntimeException {
        OutputStreamWriter osw = null;
        try {
            osw = getWriter(out, charset);
            for (Object content : contents) {
                if (content != null) {
                    osw.write(String.valueOf(content));
                }
            }
            osw.flush();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            if (isCloseOut) {
                close(osw);
            }
        }
    }

    /**
     * 将byte[]写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param content    写入的内容
     * @throws IORuntimeException IO异常
     */
    public static void write(OutputStream out, boolean isCloseOut, byte[] content) throws IORuntimeException {
        try {
            out.write(content);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            if (isCloseOut) {
                close(out);
            }
        }
    }

    /**
     * 从流中读取bytes
     *
     * @param in      {@link InputStream}
     * @param isClose 是否关闭输入流
     * @return bytes
     * @throws IORuntimeException IO异常
     * @since 5.0.4
     */
    public static byte[] readBytes(InputStream in, boolean isClose) throws IORuntimeException {
        return IoUtil.read(in, isClose).toByteArray();
    }

    public static byte[] readBytes(InputStream in) throws IORuntimeException {
        return IoUtil.readBytes(in);
    }

    /**
     * byte[] 转为{@link ByteArrayInputStream}
     *
     * @param content 内容bytes
     * @return 字节流
     * @since 4.1.8
     */
    public static ByteArrayInputStream toStream(byte[] content) {
        if (content == null) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * 从Reader中读取内容
     *
     * @param <T>        集合类型
     * @param reader     {@link Reader}
     * @param collection 返回集合
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static <T extends Collection<String>> T readLines(Reader reader, T collection) throws IORuntimeException {
        IoUtil.readLines(reader, (LineHandler) collection::add);
        return collection;
    }
}
