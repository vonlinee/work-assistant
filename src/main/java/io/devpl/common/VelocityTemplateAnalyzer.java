package io.devpl.common;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.node.*;

/**
 * Velocity
 * <a href="https://developer.aliyun.com/article/64918">...</a>
 */
public class VelocityTemplateAnalyzer implements TemplateVariableAnalyzer<Template> {

    public static void parseTemplate(Template template) {
        Object data = template.getData();
        // SimpleNode是所有节点的父类
        if (data instanceof SimpleNode sn) {
            VariableExtractor.extract(sn, data);
            recursive(sn);
        } else {
            throw new RuntimeException(String.valueOf(data.getClass()));
        }
    }

    /**
     * 递归遍历AST树
     * 深度优先
     *
     * @param parent AST点
     */
    public static void recursive(Node parent) {
        int numOfChildren = parent.jjtGetNumChildren();
        if (numOfChildren <= 0) {
            return;
        }
        for (int i = 0; i < numOfChildren; i++) {
            Node node = parent.jjtGetChild(i);
            if (node instanceof ASTText astText) {
                // 普通文本节点
                recursive(astText);
            } else if (node instanceof ASTReference astReference) {
                recursive(astReference);
            } else if (node instanceof ASTDirective astDirective) {
                recursive(astDirective);
            } else if (node instanceof ASTIfStatement astIfStatement) {
                recursive(astIfStatement);
            } else if (node instanceof ASTComment astComment) {
                recursive(astComment);
            } else if (node instanceof ASTBlock astBlock) {
                recursive(astBlock);
            } else if (node instanceof ASTDivNode astDivNode) {
                recursive(astDivNode);
            } else if (node instanceof ASTExpression astExpression) {
                recursive(astExpression);
            } else if (node instanceof ASTElseIfStatement astElseIfStatement) {
                recursive(astElseIfStatement);
            } else if (node instanceof ASTVariable astVariable) {
                recursive(astVariable);
            } else if (node instanceof ASTMethod astMethod) {
                recursive(astMethod);
            } else if (node instanceof ASTIdentifier astIdentifier) {
                System.out.println(astIdentifier.literal());
                recursive(astIdentifier);
            }
        }
    }

    @Override
    public boolean support(Class<?> templateClass) {
        return templateClass == Template.class;
    }

    @Override
    public void analyze(Template template) {
        parseTemplate(template);
    }
}
