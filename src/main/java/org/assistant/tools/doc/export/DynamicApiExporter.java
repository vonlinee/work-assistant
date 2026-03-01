package org.assistant.tools.doc.export;

import org.assistant.tools.doc.ApiProject;

import org.assistant.tools.doc.MockDataGeneratorHelper;
import org.assistant.tools.util.TemplateUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DynamicApiExporter implements ApiExporter {

    private final String templateName;
    private final String formatName;
    private final String fileExtension;
    private final ExportMessages msg = ExportMessages.getInstance();

    public DynamicApiExporter(String templateName, String formatName, String fileExtension) {
        this.templateName = templateName;
        this.formatName = formatName;
        this.fileExtension = fileExtension;
    }

    @Override
    public String getFormatName() {
        return formatName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("project", project);
        context.put("msg", msg);
        context.put("tools", new MarkdownExporter.TemplateHelper());
        context.put("mock", new MockDataGeneratorHelper());

        try (FileWriter writer = new FileWriter(output, StandardCharsets.UTF_8)) {
            TemplateUtil.render(templateName, context, writer);
        } catch (Exception e) {
            throw new IOException("Failed to render dynamic template " + templateName, e);
        }
    }
}
