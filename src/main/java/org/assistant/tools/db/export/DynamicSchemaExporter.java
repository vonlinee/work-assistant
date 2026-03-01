package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.util.TemplateUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DynamicSchemaExporter implements SchemaExporter {

    private final String templateName;
    private final String formatName;
    private final String fileExtension;

    public DynamicSchemaExporter(String templateName, String formatName, String fileExtension) {
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
    public void export(DbSchema schema, File outputFile) throws Exception {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("schema", schema);
        context.put("tools", new XmlSchemaExporter.TemplateHelper());

        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            TemplateUtil.render(templateName, context, writer);
        } catch (Exception e) {
            throw new Exception("Failed to render dynamic template " + templateName, e);
        }
    }
}
