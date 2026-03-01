package org.assistant.tools.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.Writer;
import java.util.Properties;

public class TemplateUtil {

    private static final VelocityEngine engine;

    static {
        engine = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty("resource.loaders", "classpath");
        p.setProperty("resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init(p);
    }

    public static void render(String templatePath, VelocityContext context, Writer writer) throws Exception {
        Template template = engine.getTemplate(templatePath, "UTF-8");
        template.merge(context, writer);
    }
}
