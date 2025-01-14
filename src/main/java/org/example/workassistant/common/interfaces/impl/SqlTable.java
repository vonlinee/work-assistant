package org.example.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * sql 中涉及到的表信息
 */
@Getter
@Setter
public class SqlTable {

    protected String name;

    protected String catalog;

    protected String schema;
}
