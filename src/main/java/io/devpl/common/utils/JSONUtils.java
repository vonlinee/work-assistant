package io.devpl.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 */
public abstract class JSONUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 对象转为字符串，默认无样式
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toString(Object object) {
        return toString(object, false);
    }

    /**
     * 对象转为字符串
     *
     * @param object      对象
     * @param prettyStyle 是否以指定样式进行转换，添加换行缩进等
     * @return JSON字符串
     */
    public static String toString(Object object, boolean prettyStyle) {
        try {
            if (prettyStyle) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return objectMapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析JSON字符串到指定对象
     *
     * @param text      JSON字符串
     * @param pojoClass 普通对象类型
     * @param <T>       对象类型
     * @return 对象
     */
    public static <T> T parseObject(String text, Class<T> pojoClass) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, pojoClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = JSONUtils.parseObject(json, Map.class);
        return map;
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象以JSON字符串形式写入到文件
     *
     * @param obj  对象
     * @param file 目标文件
     */
    public static void writeFile(Object obj, File file) {
        writeFile(obj, file, false);
    }

    /**
     * 将对象以JSON字符串形式写入到文件
     *
     * @param obj         对象
     * @param file        目标文件
     * @param prettyStyle 保留缩进与对其
     */
    public static void writeFile(Object obj, File file, boolean prettyStyle) {
        try {
            Files.writeString(file.toPath(), toString(obj, prettyStyle), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析字符串，有语法错误直接抛异常
     *
     * @param text 可能为JSON的字符串
     */
    public static void validateJson(String text) throws RuntimeException {
        parseObject(text, Object.class);
    }
}
