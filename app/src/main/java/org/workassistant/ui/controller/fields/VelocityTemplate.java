package org.workassistant.ui.controller.fields;

import org.apache.velocity.VelocityContext;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 针对Velocity模板的包装
 *
 * @see org.apache.velocity.Template
 */
class VelocityTemplate implements Template {

    
    org.apache.velocity.Template template;

    public VelocityTemplate( org.apache.velocity.Template template) {
        this.template = template;
    }

    @Override
    public  String getName() {
        return template.getName();
    }

    @Override
    public String getContent() {
        return template.getName();
    }

    @Override
    public void setContent(String content) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(TemplateEngine engine, Object arguments, Writer writer) throws TemplateException {
        if (arguments instanceof Map<?, ?> argumentsMap) {
            if (argumentsMap.isEmpty()) {
                throw new TemplateException("template arguments is empty");
            }
            final Set<?> keys = ((Map<?, ?>) arguments).keySet();
            // 运行时检查
            for (Object key : keys) {
                if (!(key instanceof String)) {
                    throw new TemplateException("use map as template data model, but some keys is not a String type");
                }
            }
            template.merge(new VelocityContext((Map<String, Object>) argumentsMap), writer);
        } else if (arguments instanceof TemplateArgumentsMap tam) {
            if (tam.isMap()) {
                template.merge(new VelocityContext(tam.asMap()), writer);
            } else {
                Map<String, Object> map = new HashMap<>();
                tam.fill(map);
                template.merge(new VelocityContext(map), writer);
            }
        } else {
            throw new TemplateException("Velocity仅支持Map作为模板参数");
        }
    }
}
