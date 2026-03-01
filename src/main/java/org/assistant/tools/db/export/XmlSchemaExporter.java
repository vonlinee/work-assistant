package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;

import java.io.File;
import java.io.FileOutputStream;

public class XmlSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "XML (DdlUtils) (*.xml)";
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("schema", schema);
        context.put("tools", new TemplateHelper());

        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile, java.nio.charset.StandardCharsets.UTF_8)) {
            org.assistant.tools.util.TemplateUtil.render("templates/db-schema-xml.vm", context, writer);
        } catch (Exception e) {
            throw new Exception("Failed to render XML template", e);
        }
    }

    public static class TemplateHelper {
        public String esc(String s) {
            if (s == null)
                return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&apos;");
        }
    }
}
