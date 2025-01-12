package io.devpl.fxui.common;

public final class Constants {

    private Constants() {
    }

    // mapper后缀
    public static final String MAPPER = "mapper";
    // 列名前缀
    public static final String COLUMN_PREFIX_PATTERN = "(?<=%s)[^\"]+";   // pattern regex and split prefix: (?<=aggregate_|f_)[^"]+  f_ or d_ prefix
    // 或正则表达式
    public static final String OR_REGEX = "|";

    public static final String DEFAULT_ENCODING = "utf8";

    public static final String CONFIGURATION_TYPE_XML_MAPPER = "XMLMAPPER";

    public static final int DEFAULT_MYSQL_SERVER_PORT = 3306;
    public static final String LOCALHOST = "127.0.0.1";
    public static final String MYSQL_ROOT_USERNAME = "root";

    public static final String[] SUPPORTED_ENCODING = new String[]{
            "utf8",
            "gb2312",
            "gbk"
    };
}
