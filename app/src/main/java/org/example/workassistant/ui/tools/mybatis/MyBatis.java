package org.example.workassistant.ui.tools.mybatis;

import org.example.workassistant.common.ParseResult;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.ognl.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.*;

public class MyBatis {

    public Configuration configuration;

    public MyBatis() {
        this.configuration = new Configuration();
    }

    public ParseResult parseSelectXml(String xml) {
        // 直接获取XML中的节点
        XPathParser xPathParser = new XPathParser(xml, false, null, new IgnoreDTDEntityResolver());
        XNode selectNode = xPathParser.evalNode("select");
        MyXmlStatemtnBuilder statementParser = new MyXmlStatemtnBuilder(configuration, selectNode);
        // 解析结果会放到 Configuration里
        statementParser.parseStatementNode();
        Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
        // MyBatis会存在重复的两个MappedStatement，但是ID不一样
        // StrictMap的put方法
        List<MappedStatement> list = mappedStatements.stream().distinct().toList();
        MappedStatement mappedStatement = list.get(0);
        Set<String> ognlVar = getOgnlVar(mappedStatement);
        return new ParseResult(tree(ognlVar), mappedStatement);
    }

    public static TreeNode<String> tree(Set<String> ognlVar) {
        TreeNode<String> forest = new TreeNode<>("root");
        TreeNode<String> current = forest;
        for (String expression : ognlVar) {
            TreeNode<String> root = current;
            for (String data : expression.split("\\.")) {
                current = current.addChild(data);
            }
            current = root;
        }
        return forest;
    }

    public static Set<String> getOgnlVar(MappedStatement mappedStatement) {
        SqlSource sqlSource = mappedStatement.getSqlSource();
        HashSet<String> result = new HashSet<>();
        if (sqlSource instanceof DynamicSqlSource dss) {
            SqlNode rootNode = (SqlNode) ReflectionUtils.getValue(dss, "rootSqlNode");
            searchExpressions(rootNode, result);
        }
        return result;
    }

    /**
     * 查找MyBatis的MappedStatement中所有出现的变量引用，只能出现文本中出现的变量
     * @param parent      根节点
     * @param expressions 存放结果，未去重
     */
    @SuppressWarnings("unchecked")
    public static void searchExpressions(SqlNode parent, Set<String> expressions) {
        if (parent instanceof MixedSqlNode msn) {
            List<SqlNode> contents = (List<SqlNode>) ReflectionUtils.getValue(msn, "contents");
            if (contents != null) {
                for (SqlNode content : contents) {
                    searchExpressions(content, expressions);
                }
            }
        } else if (parent instanceof StaticTextSqlNode stsn) {
            String sqlText = (String) ReflectionUtils.getValue(stsn, "text");
            if (sqlText != null && !sqlText.isEmpty()) {
                find(sqlText, expressions);
            }
        } else if (parent instanceof TextSqlNode tsn) {
            String sqlText = (String) ReflectionUtils.getValue(tsn, "text");
            if (sqlText != null && !sqlText.isEmpty()) {
                find(sqlText, expressions);
            }
        } else if (parent instanceof ForEachSqlNode fesn) {
            String expression = (String) ReflectionUtils.getValue(fesn, "collectionExpression");
            expressions.add(expression);
            SqlNode contents = (SqlNode) ReflectionUtils.getValue(fesn, "contents");
            searchExpressions(contents, expressions);
        } else if (parent instanceof IfSqlNode ifsn) {
            // IfSqlNode会导致解析到的表达式重复
            // test 条件
            String testCondition = (String) ReflectionUtils.getValue(ifsn, "test");
            parseIfExpression(testCondition, expressions);
            // 解析条件表达式中使用的表达式变量  Ognl表达式
            SqlNode content = (SqlNode) ReflectionUtils.getValue(ifsn, "contents");
            searchExpressions(content, expressions);
        } else if (parent instanceof WhereSqlNode wsn) {
            SqlNode contents = (SqlNode) ReflectionUtils.getValue(wsn, "contents");
            searchExpressions(contents, expressions);
        } else if (parent instanceof SetSqlNode ssn) {
            SqlNode contents = (SqlNode) ReflectionUtils.getValue(ssn, "contents");
            searchExpressions(contents, expressions);
        } else if (parent instanceof ChooseSqlNode csn) {
            List<SqlNode> ifSqlNodes = (List<SqlNode>) ReflectionUtils.getValue(csn, "ifSqlNodes");
            if (ifSqlNodes != null) {
                SqlNode defaultSqlNode = (SqlNode) ReflectionUtils.getValue(csn, "defaultSqlNode");
                if (defaultSqlNode != null) {
                    ifSqlNodes.add(defaultSqlNode);
                }
                for (SqlNode sqlNode : ifSqlNodes) {
                    searchExpressions(sqlNode, expressions);
                }
            }
        }
    }

    private static void parseIfExpression(String testCondition, Set<String> expressions) {
        try {
            Object node = Ognl.parseExpression(testCondition);
            ExpressionNode expressionNode = (ExpressionNode) node;
            searchOgnlExpressionNode(expressionNode, expressions);
        } catch (OgnlException e) {
            // ignore
        }
    }

    private static void searchOgnlExpressionNode(SimpleNode expressionNode, Set<String> results) {
        if (expressionNode instanceof ExpressionNode) {
            // 比较
            if (expressionNode instanceof ASTNotEq notEq) {
                searchChildren(notEq, results);
            } else if (expressionNode instanceof ASTAnd andNode) {
                searchChildren(andNode, results);
            } else if (expressionNode instanceof ASTEq eqNode) {
                searchChildren(eqNode, results);
            }
        } else if (expressionNode instanceof ASTChain chainNode) {
            results.add(chainNode.toString());
        }
    }

    private static void searchChildren(SimpleNode parent, Set<String> results) {
        int childrenCount = parent.jjtGetNumChildren();
        for (int i = 0; i < childrenCount; i++) {
            Node node = parent.jjtGetChild(i);
            searchOgnlExpressionNode((SimpleNode) node, results);
        }
    }

    /**
     * 递归寻找$引用的表达式，对应的SqlNode是 TextSqlNode
     * @param content     文本，包含${xxx}或者#{xxx}
     * @param expressions 存放结果的容器
     */
    private static void find(String content, Set<String> expressions) {
        content = content.trim().replace("\n", "");
        if (content.isEmpty()) {
            return;
        }
        final char[] chars = content.toCharArray();
        int fromIndex, endIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            // MyBatis要求 $和{之间没有空格才有效
            // 且不能嵌套
            // Mapper文件语法正确的情况下，一轮遍历即可，不会回头
            if ((chars[i] == '$' || chars[i] == '#') && chars[i + 1] == '{') {
                // 找到}
                fromIndex = i + 2;
                endIndex = fromIndex + 1;
                while (chars[endIndex] != '}') {
                    if (chars[endIndex] == ' ') {
                        fromIndex++;
                    }
                    endIndex++;
                }
                final char[] chars1 = Arrays.copyOfRange(chars, fromIndex, endIndex);
                expressions.add(String.valueOf(chars1));
                i = endIndex + 1;
            }
        }
    }
}
