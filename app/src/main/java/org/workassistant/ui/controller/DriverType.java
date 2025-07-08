package org.workassistant.ui.controller;

import java.util.Properties;

/**
 * 驱动类型
 */
public interface DriverType {

    String JDBC_PROTOCOL = "jdbc";

    /**
     * 驱动类型名称
     *
     * @return 驱动类型名称
     */
    String getName();

    /**
     * 驱动类名
     *
     * @return 驱动类名
     */
    String getDriverClassName();

    /**
     * 获取子协议
     *
     * @return 子协议
     */
    String getSubProtocol();

    /**
     * 获取连接地址
     *
     * @param host         IP地址
     * @param port         端口号
     * @param databaseName 数据库名称
     * @param props        连接属性
     * @return 连接URL地址
     */
    String getConnectionUrl(String host, int port, String databaseName, Properties props);

    /**
     * 默认端口
     *
     * @return 默认端口号, -1表示不存在
     */
    default int getDefaultPort() {
        return -1;
    }
}
