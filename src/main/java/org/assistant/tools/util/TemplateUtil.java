package org.assistant.tools.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.Writer;
import java.util.Properties;
import java.io.File;

public class TemplateUtil {

    private static final VelocityEngine engine;

    private static final String customTemplateDir = System.getProperty("user.home") + File.separator + ".work-assistant"
            + File.separator + "templates";

    static {
        // Ensure custom template directory exists
        File dir = new File(customTemplateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        engine = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty("resource.loaders", "file, classpath");
        p.setProperty("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        p.setProperty("resource.loader.file.path", customTemplateDir);
        p.setProperty("resource.loader.file.cache", "false"); // Don't cache so updates reflect immediately
        p.setProperty("resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init(p);
    }

    public static void render(String templatePath, VelocityContext context, Writer writer) throws Exception {
        Template template = engine.getTemplate(templatePath, "UTF-8");
        template.merge(context, writer);
    }
}
