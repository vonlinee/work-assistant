package io.devpl.fxui.controller.dbconn;

import io.devpl.fxui.model.ConnectionConfig;

import java.util.List;

/**
 * 连接信息Service
 */
public interface ConnectionConfigService {

    /**
     * 查询所有
     *
     * @return 连接信息列表
     */
    List<ConnectionConfig> listAll();

    /**
     * 保存连接信息
     *
     * @param connectionConfig 连接信息
     * @return 是否成功
     */
    boolean save(ConnectionConfig connectionConfig);

    /**
     * 更新连接信息
     *
     * @param connectionConfig 连接信息
     * @return 是否成功
     */
    boolean updateById(ConnectionConfig connectionConfig);
}
