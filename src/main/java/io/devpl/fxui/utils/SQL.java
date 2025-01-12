package io.devpl.fxui.utils;

import java.util.ArrayList;
import java.util.List;

public final class SQL {

    private static final SQL instance = new SQL();

    private SQL() {

    }

    public static final String SELECT_DICT_CONFIG = "select * from t_sys_dict_data";

    private final List<String> keys = new ArrayList<>();
    private final List<String> sqlStrings = new ArrayList<>();

    public static class SqlServer {
        public static final String SELECT = "SELECT name FROM sysobjects  WHERE xtype='u' OR xtype='v' ORDER BY name";
    }

    public static class Sqllite {
        public static final String SELECT = "SELECT name FROM sqlite_master;";
    }
}
