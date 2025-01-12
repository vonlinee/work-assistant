package io.devpl.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询表
 */
@Getter
@Setter
public class SelectTable extends SqlTable {

    /**
     * 查询表用到的别名
     */
    protected String alias;

    /**
     * 是否是临时表
     */
    private boolean temporary;

    public SelectTable(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }
}
