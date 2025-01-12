package io.devpl.common.interfaces.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcMetadataObject {

    /**
     * 根据ResultSet进行初始化
     *
     * @param resultSet ResultSet
     * @throws SQLException JDBC操作异常
     */
    void initialize(ResultSet resultSet) throws SQLException;
}
