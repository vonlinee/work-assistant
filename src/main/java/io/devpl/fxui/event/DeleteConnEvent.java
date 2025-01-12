package io.devpl.fxui.event;

import java.util.List;

/**
 * 删除连接信息事件
 */
public class DeleteConnEvent {

    private List<String> connectionNames;

    public List<String> getConnectionNames() {
        return connectionNames;
    }

    public void setConnectionNames(List<String> connectionNames) {
        this.connectionNames = connectionNames;
    }
}
