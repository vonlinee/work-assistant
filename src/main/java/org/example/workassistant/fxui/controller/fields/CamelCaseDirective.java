package org.example.workassistant.fxui.controller.fields;

/**
 * userdirective=aaa.bbb.ccc.XxxDirective
 * 注册自定义指令: velocity.properties
 * (userdirective=继承了org.apache.velocity.runtime.directive.Directive类的子类全类名)
 * 新版本配置:
 * runtime.custom_directives=io.devpl.codegen.template.velocity.CamelCaseDirective
 */
public class CamelCaseDirective extends VelocityTemplateDirective {

    /**
     * 函数名，也就是模板调用这个函数的名称，比如#hellofun()
     *
     * @return 函数名
     */
    @Override
    public String getName() {
        return "toCamelCase";
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return new Class[]{String.class};
    }

    @Override
    public String render(Object[] params) {
        return CaseFormat.CAPITAL_FIRST.normalize(CaseFormat.underlineToCamel((String) params[0]));
    }
}
