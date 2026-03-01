package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class MarkdownSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "Markdown (*.md)";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("schema", schema);

        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile, java.nio.charset.StandardCharsets.UTF_8)) {
            org.assistant.tools.util.TemplateUtil.render("templates/db-schema-markdown.vm", context, writer);
        } catch (Exception e) {
            throw new Exception("Failed to render markdown template", e);
        }
    }
}
