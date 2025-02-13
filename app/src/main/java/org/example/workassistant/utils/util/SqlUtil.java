package org.example.workassistant.utils.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * sql操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlUtil {

    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     */
    public static final String SQL_PATTERN = "[a-zA-Z0-9_ ,.]+";
    /**
     * 定义常用的 sql关键字
     */
    public static String SQL_REGEX = "and |extractvalue|updatexml|sleep|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";

    /**
     * 检查字符，防止注入绕过
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.hasText(value) && !isValidOrderBySql(value)) {
            throw new IllegalArgumentException("参数不符合规范，不能进行查询");
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL关键字检查
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        List<String> sqlKeywords = StringUtils.splitToList(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new IllegalArgumentException("参数存在SQL注入风险");
            }
        }
    }
}
