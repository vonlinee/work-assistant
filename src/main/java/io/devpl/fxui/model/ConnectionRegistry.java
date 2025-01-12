package io.devpl.fxui.model;

import io.devpl.common.utils.EncryptUtils;
import io.devpl.fxui.utils.AppConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接信息注册中心
 */
public class ConnectionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionRegistry.class);

    /**
     * 缓存已注册的连接配置
     */
    static WeakReference<Map<String, ConnectionConfig>> connConfigRef;

    static {
        loadFromDatabase();
    }

    private static void loadFromDatabase() {
        List<ConnectionConfig> connConfigList = AppConfig.listConnectionInfo();
        Map<String, ConnectionConfig> registeredConnectionConfigMap = new ConcurrentHashMap<>();
        for (ConnectionConfig item : connConfigList) {
            registeredConnectionConfigMap.put(item.getConnectionName(), item);
        }
        connConfigRef = new WeakReference<>(registeredConnectionConfigMap);
    }

    public static boolean contains(String connectionName) {
        return getRegisteredConnectionConfigMap().containsKey(connectionName);
    }

    /**
     * 根据连接名称获取连接配置
     *
     * @param connectionName 连接名称
     * @return 连接配置
     */
    public static ConnectionConfig get(String connectionName) {
        ConnectionConfig cg = getRegisteredConnectionConfigMap().get(connectionName);
        cg.setPassword(EncryptUtils.tryDecrypt(cg.getPassword()));
        return cg;
    }

    /**
     * 获取所有连接配置
     *
     * @return 连接配置列表
     */
    public static ObservableList<ConnectionConfig> getConnectionConfigurations() {
        return FXCollections.observableArrayList(getRegisteredConnectionConfigMap().values());
    }

    /**
     * 同步方法
     *
     * @return key为连接名，value为对应的连接信息
     */
    public static synchronized Map<String, ConnectionConfig> getRegisteredConnectionConfigMap() {
        Map<String, ConnectionConfig> map = connConfigRef.get();
        if (map == null) {
            logger.info("reload ConnectionInfo from database");
            loadFromDatabase();
        }
        return connConfigRef.get();
    }
}
