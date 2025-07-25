package org.workassistant.ui.controller.dbconn;

import org.workassistant.ui.controls.ExceptionDialog;
import org.workassistant.ui.model.ConnectionConfig;
import org.workassistant.util.AppConfig;
import org.workassistant.util.DBUtils;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 连接配置 Service
 */
public class ConnectionConfigServiceImpl implements ConnectionConfigService {

    @Override
    public List<ConnectionConfig> listAll() {
        String sql = "select * from rdbms_connection_info";
        try (Connection connection = AppConfig.getConnection()) {
            return DBUtils.queryBeanList(connection, sql, ConnectionConfig.class);
        } catch (Exception exception) {
            ExceptionDialog.show(exception);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean save(ConnectionConfig connectionConfig) {
        String sql = "insert into rdbms_connection_info values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";
        sql = sql.formatted(UUID.randomUUID().toString(), connectionConfig.getConnectionName(), connectionConfig.getConnectionUrl(), connectionConfig.getHost(), connectionConfig.getPort(), connectionConfig.getDbType()
                , connectionConfig.getDbName(), connectionConfig.getUsername(), connectionConfig.getPassword(), connectionConfig.getEncoding());
        try (Connection connection = AppConfig.getConnection()) {
            return DBUtils.insert(connection, sql) > 0;
        } catch (Exception exception) {
            ExceptionDialog.show(exception);
        }
        return false;
    }

    @Override
    public boolean updateById(ConnectionConfig connectionConfig) {
        return false;
    }
}
