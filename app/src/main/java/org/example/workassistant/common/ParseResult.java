package org.example.workassistant.common;

import org.example.workassistant.ui.tools.mybatis.TreeNode;
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
