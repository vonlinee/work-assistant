package org.example.workassistant.fxui.controller;

import org.jetbrains.annotations.Nullable;

/**
 * 数据库类型
 * 部分数据库的不同版本也视为不同的类型
 */
public interface DatabaseType {

    /**
     * 数据库名称
     *
     * @return 数据库名称
     */
    String getName();

    /**
     * 版本号
     *
     * @return 版本号
     */
    default String getVersion() {
        return "UNKNOWN";
    }

    /**
     * 注册驱动类型
     *
     * @param driverType 驱动类型
     */
    void registerDriverType(DriverType driverType);

    /**
     * 取消注册驱动类型
     *
     * @param driverType 驱动类型
     */
    void deregisterDriverType(DriverType driverType);

    /**
     * the jdbc driver type instance of this database type.
     *
     * @return DriverType instances
     * @see DriverType
     */
    DriverType[] getSupportedDriverTypes();

    @Nullable
    DriverType getSupportedDriverType(int index);
}
