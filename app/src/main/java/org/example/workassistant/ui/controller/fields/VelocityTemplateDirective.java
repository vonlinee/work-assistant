package org.example.workassistant.ui.controller.fields;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Velocity模板指令的父类
 */
public abstract class VelocityTemplateDirective extends Directive implements TemplateDirective {

    /**
     * 函数类型，可以是行也可以是块函数
     * BLOCK: 表示该指令会替换其所在位置的所有内容，需要end结束符
     * LINE: 不要end结束符
     *
     * @return 指令类型
     * @see DirectiveConstants
     */
    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    }

    /**
     * @param context 上下文 当前velocity的容器，可以存取变量，比如在页面上使用#set($name="bwong")，可以通过context.get("name")取出"bwong"
     * @param writer  输出位置
     * @param node    节点 node里面可以取出调用这个函数时的入参，比如#hellofun("a")，通过node.jjtGetChild(0).value(context)取出"a"
     * @return 是否成功, 如果指令渲染成功，返回true
     * @throws IOException               IOException
     * @throws ResourceNotFoundException 模板不存在
     * @throws ParseErrorException       模板语法错误
     * @throws MethodInvocationException 反射执行异常
     */
    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Class<?>[] parameterTypes = getParameterTypes();
        // 获取指令传参 传参的文字文本或者对象
        // 指令参数
        final int childCount = node.jjtGetNumChildren();
        Object[] directiveArguments = new Object[childCount];
        for (int i = 0; i < childCount; i++) {
            Node childNode = node.jjtGetChild(i);
            directiveArguments[i] = childNode.value(context);
        }
        // 渲染结果
        String renderResult = this.render(directiveArguments);
        // 输出渲染结果
        writer.write(Objects.requireNonNullElse(renderResult, ""));
        return true;
    }
}
