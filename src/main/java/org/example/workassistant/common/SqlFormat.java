package org.example.workassistant.common;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;

public class SqlFormat {

    public static String formatMySql(String sql) {
        return SQLUtils.formatMySql(sql);
    }

    public static void main(String[] args) {
        String mysql = "SELECT *,CASE WHEN UNIX_TIMESTAMP( expire_time ) < UNIX_TIMESTAMP( NOW( ) ) THEN 1 ELSE 0 END state FROM `expire_time_data`;";
        mysql(mysql);
        String oracle = "SELECT a.TABLE_NAME,b.COMMENTS FROM user_tables a,user_tab_comments b WHERE a.TABLE_NAME=b.TABLE_NAME ORDER BY TABLE_NAME;";
        oracleSql(oracle);
        String pgsql = "SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg%' AND tablename NOT LIKE 'sql_%' ORDER BY tablename;";
        PgSql(pgsql);
        sqlFormat(mysql, DbType.mysql);
    }

    /**
     * mysql格式化
     *
     * @param sql
     */
    public static void mysql(String sql) {
        System.out.println("Mysql格式化：" + SQLUtils.formatMySql(sql));
    }

    /**
     * oracle格式化
     *
     * @param sql
     */
    public static void oracleSql(String sql) {
        System.out.println("Oracle格式化：" + SQLUtils.formatOracle(sql));
    }

    /**
     * pgsql格式化
     *
     * @param sql
     */
    public static void PgSql(String sql) {
        System.out.println("postgreSql格式化：" + SQLUtils.format(sql, DbType.postgresql));
    }

    /**
     * sql格式
     *
     * @param sql    格式化的语句
     * @param dbType 数据库类型
     */
    public static void sqlFormat(String sql, DbType dbType) {
        System.out.println("sql格式化：" + SQLUtils.format(sql, dbType));
    }
}
