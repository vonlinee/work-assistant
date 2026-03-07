package org.assistant.tools.doc.export;

import org.assistant.tools.doc.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Exports API definitions to a self-contained HTML page with embedded CSS.
 */
public class HtmlExporter implements ApiExporter {

    private final ExportMessages msg = ExportMessages.getInstance();

    @Override
    public String getFormatName() {
        return "HTML";
    }

    @Override
    public String getFileExtension() {
        return "html";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("project", project);
        context.put("msg", msg);
        context.put("tools", new TemplateHelper());
        context.put("mock", new org.assistant.tools.doc.MockDataGeneratorHelper());
        context.put("css", CSS);

        try (java.io.FileWriter writer = new java.io.FileWriter(output, StandardCharsets.UTF_8)) {
            org.assistant.tools.util.TemplateUtil.render("templates/api-html.vm", context, writer);
        } catch (Exception e) {
            throw new IOException("Failed to render HTML template", e);
        }
    }

    public static class TemplateHelper {
        public String esc(String s) {
            if (s == null)
                return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        }

        public String anchor(String name) {
            if (name == null)
                return "";
            return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
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
            if (depth == 0) {
                sb.append(indent).append("<h4 class=\"field-type\"><code>").append(esc(typeName))
                        .append("</code></h4>\n");
            }
            sb.append(indent).append("<table class=\"fields\">\n");
            sb.append(indent)
                    .append("<tr><th>").append(msg.headerField()).append("</th><th>").append(msg.headerType())
                    .append("</th><th>").append(msg.headerRequired()).append("</th><th>").append(msg.headerDefault())
                    .append("</th><th>").append(msg.headerDescription()).append("</th></tr>\n");
            for (FieldInfo f : fields) {
                sb.append(indent).append("<tr>")
                        .append("<td>").append(esc(f.getName())).append("</td>")
                        .append("<td><code>").append(esc(f.getFrontendType())).append("</code></td>")
                        .append("<td>").append(f.isRequired() ? "\u2713" : "").append("</td>")
                        .append("<td>").append(f.getDefaultValue() != null ? esc(f.getDefaultValue()) : "")
                        .append("</td>")
                        .append("<td>").append(f.getDescription() != null ? esc(f.getDescription()) : "")
                        .append("</td>")
                        .append("</tr>\n");
                if (f.hasChildren() && depth < 2) {
                    sb.append(indent).append("<tr><td colspan=\"5\">\n");
                    writeFieldTableRecursive(sb, f.getChildren(), f.getFrontendType(), depth + 1, msg);
                    sb.append(indent).append("</td></tr>\n");
                }
            }
            sb.append(indent).append("</table>\n");
        }
    }

    private static final String CSS = """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                   line-height: 1.6; color: #333; background: #f8f9fa; }

            /* Layout */
            .header { text-align: center; padding: 20px 0; background: #fff; border-bottom: 1px solid #e0e0e0; position: sticky; top: 0; z-index: 100; }
            .header h1 { font-size: 1.8em; color: #1a1a2e; }
            .version { color: #666; font-size: 0.9em; }
            .container { display: flex; max-width: 1400px; margin: 0 auto; min-height: calc(100vh - 80px); }

            /* Sidebar Navigation */
            .sidebar { width: 280px; flex-shrink: 0; background: #fff; padding: 20px; border-right: 1px solid #e0e0e0;
                       height: calc(100vh - 80px); position: sticky; top: 80px; overflow-y: auto; }
            .sidebar h2 { font-size: 1.2em; margin-bottom: 10px; color: #444; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #eee; padding-bottom: 8px;}
            .search-input { width: 100%; padding: 8px 10px; margin-bottom: 15px; border: 1px solid #ddd; border-radius: 4px; font-size: 0.9em; outline: none; transition: border-color 0.2s;}
            .search-input:focus { border-color: #0366d6; box-shadow: 0 0 0 3px rgba(3, 102, 214, 0.1); }
            .sidebar ul { list-style: none; }
            .sidebar li { margin: 8px 0; }
            .sidebar a { color: #0366d6; text-decoration: none; display: block; padding: 6px 10px; border-radius: 4px; transition: background 0.2s; font-weight: 500;}
            .sidebar a:hover { text-decoration: none; background: #f0f4f8; }
            .sidebar .api-list { padding-left: 12px; margin-top: 4px; border-left: 2px solid #eee; margin-left: 10px;}
            .sidebar .api-list li { margin: 4px 0; }
            .sidebar .api-list a { font-size: 0.85em; color: #555; padding: 4px 8px; font-weight: normal; font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;}
            .sidebar .api-list a:hover { color: #0366d6; background: #f0f4f8; }
            .menu-method { font-size: 0.8em; font-weight: 600; padding: 2px 5px; border-radius: 3px; color: #fff; margin-right: 5px; display: inline-block; min-width: 35px; text-align: center;}
            .sidebar a:hover { text-decoration: none; background: #f0f4f8; }

            /* Main Content */
            .content { flex-grow: 1; padding: 30px 40px; background: #f8f9fa; min-width: 0; }

            .group { background: #fff; padding: 25px; border-radius: 8px; margin-bottom: 30px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
            .group h2 { font-size: 1.6em; color: #1a1a2e; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 15px; }
            .base-path { color: #555; font-size: 0.95em; margin-bottom: 15px; background: #f5f5f5; padding: 8px 12px; border-radius: 4px; display: inline-block;}
            .endpoint { border: 1px solid #e8e8e8; border-radius: 6px; margin: 20px 0; overflow: hidden; background: #fff;}
            .endpoint-header { display: flex; align-items: center; gap: 12px; padding: 12px 15px; background: #fafafa; border-bottom: 1px solid #eee;}
            .method { font-weight: 700; font-size: 0.85em; padding: 4px 10px; border-radius: 4px; color: #fff; text-transform: uppercase; }
            .method-get { background: #61affe; }
            .method-post { background: #49cc90; }
            .method-put { background: #fca130; }
            .method-delete { background: #f93e3e; }
            .method-patch { background: #50e3c2; }
            .method-head { background: #9012fe; }
            .method-options { background: #0d5aa7; }
            .path { font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace; font-size: 1em; color: #24292e; word-break: break-all;}
            .deprecated { background: #fff8c5; color: #9a6700; padding: 2px 8px; border-radius: 3px; font-size: 0.8em; font-weight: 600; border: 1px solid #f6e890;}
            .summary { padding: 15px; color: #444; background: #fff; }
            .return { padding: 5px 15px; color: #666; font-size: 0.9em; }
            .params { width: 100%; border-collapse: collapse; margin: 0; }
            .params th { background: #f6f8fa; text-align: left; padding: 10px 15px; font-size: 0.85em; color: #24292e; border-top: 1px solid #e1e4e8; border-bottom: 1px solid #e1e4e8;}
            .params td { padding: 10px 15px; border-bottom: 1px solid #e1e4e8; font-size: 0.9em; color: #24292e;}
            .params tr:last-child td { border-bottom: none; }
            .params tr:hover { background: #fafbfc; }
            .fields-section { padding: 15px; border-top: 1px solid #eee;}
            .fields-section h4 { font-size: 1em; color: #24292e; margin-bottom: 12px; }
            .field-type { font-size: 0.85em; color: #586069; margin: 8px 0 4px 0; }
            .fields { width: 100%; border-collapse: collapse; margin: 5px 0 10px 15px; border: 1px solid #e1e4e8; border-radius: 4px; overflow: hidden;}
            .fields th { background: #f6f8fa; text-align: left; padding: 8px 12px; font-size: 0.8em; color: #586069; border-bottom: 1px solid #e1e4e8;}
            .fields td { padding: 8px 12px; border-bottom: 1px solid #e1e4e8; font-size: 0.85em; color: #24292e;}
            .fields tr:last-child td { border-bottom: none; }
            .fields tr:hover { background: #fafbfc; }
            code { background: #f6f8fa; padding: 3px 6px; border-radius: 3px; font-size: 0.9em; font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace; color: #24292e;}
            pre { background: #f6f8fa; padding: 15px; border-radius: 6px; overflow-x: auto; border: 1px solid #e1e4e8;}
            pre code { background: none; padding: 0; border: none; font-size: 0.9em; color: #24292e;}
            .desc { color: #586069; margin: 5px 0 15px 0; }
            """;
}
