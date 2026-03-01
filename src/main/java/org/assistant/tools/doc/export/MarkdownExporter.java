package org.assistant.tools.doc.export;

import org.assistant.tools.doc.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Exports API definitions to a Markdown file.
 */
public class MarkdownExporter implements ApiExporter {

    private final ExportMessages msg = ExportMessages.getInstance();

    @Override
    public String getFormatName() {
        return "Markdown";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("project", project);
        context.put("msg", msg);
        context.put("tools", new TemplateHelper());
        context.put("mock", new org.assistant.tools.doc.MockDataGeneratorHelper());

        try (java.io.FileWriter writer = new java.io.FileWriter(output, StandardCharsets.UTF_8)) {
            org.assistant.tools.util.TemplateUtil.render("templates/api-markdown.vm", context, writer);
        } catch (Exception e) {
            throw new IOException("Failed to render markdown template", e);
        }
    }

    public static class TemplateHelper {
        public String anchor(String text) {
            if (text == null)
                return "";
            return text.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }

        public String join(java.util.List<String> list) {
            if (list == null)
                return "";
            return String.join(", ", list);
        }

        public String writeFieldTable(java.util.List<FieldInfo> fields, String typeName, int depth,
                ExportMessages msg) {
            StringBuilder sb = new StringBuilder();
            writeFieldTableRecursive(sb, fields, typeName, depth, msg);
            return sb.toString();
        }

        private void writeFieldTableRecursive(StringBuilder sb, java.util.List<FieldInfo> fields, String typeName,
                int depth, ExportMessages msg) {
            String indent = "  ".repeat(depth);
            sb.append(indent).append("> `").append(typeName).append("` ").append(msg.fields()).append(":\n");
            sb.append(indent).append(">\n");
            sb.append(indent).append("> | ").append(msg.headerField()).append(" | ").append(msg.headerType())
                    .append(" | ").append(msg.headerRequired()).append(" | ").append(msg.headerDefault())
                    .append(" | ").append(msg.headerDescription()).append(" |\n");
            sb.append(indent).append("> |-------|------|----------|---------|-------------|\n");
            for (FieldInfo f : fields) {
                sb.append(indent).append("> | ").append(f.getName() != null ? f.getName() : "")
                        .append(" | `").append(f.getType() != null ? f.getType() : "").append("`")
                        .append(" | ").append(f.isRequired() ? "\u2713" : "")
                        .append(" | ").append(f.getDefaultValue() != null ? f.getDefaultValue() : "")
                        .append(" | ").append(f.getDescription() != null ? f.getDescription() : "")
                        .append(" |\n");
            }
            sb.append("\n");
            for (FieldInfo f : fields) {
                if (f.hasChildren() && depth < 2) {
                    writeFieldTableRecursive(sb, f.getChildren(), f.getType(), depth + 1, msg);
                }
            }
        }
    }
}
