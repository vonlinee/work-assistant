package org.example.workassistant.fxui.controller.fields;

import java.io.IOException;
import java.io.Writer;

/**
 * 字符串模板
 * 针对不同类型语法的模板做一层包装，不然就需要针对每一种语法写一个实现
 * VelocityStringTemplateSource，FreeMarkerStringTemplateSource等
 */
public class StringTemplate implements Template {

    String content;

    public StringTemplate(String content) {
        this.content = content;
    }

    /**
     * 字符串模板的名称为其模板文本
     *
     * @return 模板名称
     */
    @Override
    public  String getName() {
        return content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean isStringTemplate() {
        return true;
    }

    @Override
    public void render(TemplateEngine engine, Object arguments, Writer writer) {
        String result = engine.evaluate(content, arguments);
        try {
            writer.write(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
