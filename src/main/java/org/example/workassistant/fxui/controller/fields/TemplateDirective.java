package org.example.workassistant.fxui.controller.fields;

/**
 * 所有模板引擎的指令实现
 * 注：某些模板引擎不支持自定义指令
 */
public interface TemplateDirective {

    /**
     * 指令的名称，将作为模板中使用的指令名称
     *
     * @return 指令的名称
     */
    String getName();

    /**
     * 参数类型，指令所需的参数类型列表
     *
     * @return 参数类型列表
     * @see TemplateDirective#render(Object[])
     */
    Class<?>[] getParameterTypes();

    /**
     * @param params 指令参数 对应{@code getParameterTypes}的实参
     * @return 输出的内容
     */
    String render(Object[] params);
}
