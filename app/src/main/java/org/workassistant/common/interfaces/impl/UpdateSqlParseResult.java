package org.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateSqlParseResult {

    private SqlTable table;

    private List<UpdateColumn> updateColumns;

    private String whereCondition;
}
