package org.workassistant.common;

import org.workassistant.ui.tools.mybatis.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.MappedStatement;

@Getter
@Setter
@AllArgsConstructor
public class ParseResult {

    private TreeNode<String> root;
    private MappedStatement mappedStatement;
}
