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
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("<title>").append(esc(project.getProjectName())).append(" ").append(msg.apiDocumentation())
                .append("</title>\n")
                .append("<style>\n").append(CSS).append("\n</style>\n")
                .append("</head>\n<body>\n");

        // Header
        sb.append("<div class=\"header\">\n")
                .append("  <h1>").append(esc(project.getProjectName())).append(" ").append(msg.apiDocumentation())
                .append("</h1>\n");
        if (project.getVersion() != null) {
            sb.append("  <p class=\"version\">").append(msg.version()).append(" ").append(esc(project.getVersion()))
                    .append("</p>\n");
        }
        if (project.getDescription() != null) {
            sb.append("  <p class=\"desc\">").append(esc(project.getDescription())).append("</p>\n");
        }
        sb.append("</div>\n");

        // TOC
        sb.append("<nav class=\"toc\"><h2>").append(msg.tableOfContents()).append("</h2><ul>\n");
        for (ApiGroup group : project.getGroups()) {
            sb.append("<li><a href=\"#").append(anchorId(group.getName())).append("\">")
                    .append(esc(group.getName())).append("</a></li>\n");
        }
        sb.append("</ul></nav>\n");

        // Groups
        for (ApiGroup group : project.getGroups()) {
            writeGroup(sb, group);
        }

        sb.append("</body>\n</html>\n");
        Files.writeString(output.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private void writeGroup(StringBuilder sb, ApiGroup group) {
        sb.append("<section class=\"group\" id=\"").append(anchorId(group.getName())).append("\">\n")
                .append("  <h2>").append(esc(group.getName())).append("</h2>\n");

        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            sb.append("  <p class=\"desc\">").append(esc(group.getDescription())).append("</p>\n");
        }
        if (group.getBasePath() != null && !group.getBasePath().isEmpty()) {
            sb.append("  <p class=\"base-path\">").append(msg.basePath()).append(": <code>")
                    .append(esc(group.getBasePath())).append("</code></p>\n");
        }

        for (WebApiInfo api : group.getApis()) {
            writeEndpoint(sb, api);
        }
        sb.append("</section>\n");
    }

    private void writeEndpoint(StringBuilder sb, WebApiInfo api) {
        String methodClass = "method-" + api.getMethod().toLowerCase();
        sb.append("<div class=\"endpoint\">\n")
                .append("  <div class=\"endpoint-header\">\n")
                .append("    <span class=\"method ").append(methodClass).append("\">").append(esc(api.getMethod()))
                .append("</span>\n")
                .append("    <span class=\"path\">").append(esc(api.getPath())).append("</span>\n");
        if (api.isDeprecated()) {
            sb.append("    <span class=\"deprecated\">").append(msg.deprecated()).append("</span>\n");
        }
        sb.append("  </div>\n");

        if (api.getSummary() != null && !api.getSummary().isEmpty()) {
            sb.append("  <p class=\"summary\">").append(esc(api.getSummary())).append("</p>\n");
        }

        if (api.getReturnType() != null) {
            sb.append("  <p class=\"return\">").append(msg.returnType()).append(": <code>")
                    .append(esc(api.getReturnType())).append("</code></p>\n");
        }

        if (!api.getParams().isEmpty()) {
            sb.append("  <table class=\"params\">\n")
                    .append("    <tr><th>").append(msg.headerName()).append("</th><th>").append(msg.headerIn())
                    .append("</th><th>").append(msg.headerType()).append("</th><th>").append(msg.headerRequired())
                    .append("</th><th>").append(msg.headerDefault()).append("</th><th>").append(msg.headerDescription())
                    .append("</th></tr>\n");
            for (ApiParam p : api.getParams()) {
                sb.append("    <tr>")
                        .append("<td>").append(esc(p.getName())).append("</td>")
                        .append("<td>").append(p.getIn() != null ? p.getIn().name().toLowerCase() : "").append("</td>")
                        .append("<td><code>").append(esc(p.getDataType())).append("</code></td>")
                        .append("<td>").append(p.isRequired() ? "\u2713" : "").append("</td>")
                        .append("<td>").append(p.getDefaultValue() != null ? esc(p.getDefaultValue()) : "")
                        .append("</td>")
                        .append("<td>").append(p.getDescription() != null ? esc(p.getDescription()) : "")
                        .append("</td>")
                        .append("</tr>\n");
                // Render resolved fields for complex types
                if (p.hasFields()) {
                    sb.append("    <tr><td colspan=\"6\">\n");
                    writeFieldTable(sb, p.getFields(), p.getDataType(), 0);
                    sb.append("    </td></tr>\n");
                }
            }
            sb.append("  </table>\n");
        }

        // Return type fields
        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            sb.append("  <div class=\"fields-section\">\n")
                    .append("    <h4>").append(msg.responseFields()).append("</h4>\n");
            writeFieldTable(sb, api.getReturnTypeFields(), api.getReturnType(), 0);
            sb.append("  </div>\n");
        }

        if (!api.getParams().isEmpty() || api.getPath() != null) {
            sb.append("  <div class=\"fields-section\">\n")
                    .append("    <h4>Sample Request</h4>\n");

            String mockUrl = MockDataGenerator.generateMockUrl(api);
            sb.append("    <p><strong>URL:</strong> <code>").append(esc(mockUrl)).append("</code></p>\n");

            String mockHeaders = MockDataGenerator.generateMockHeaders(api.getParams());
            if (!mockHeaders.isEmpty()) {
                sb.append("    <p><strong>Headers:</strong></p>\n")
                        .append("    <pre><code>").append(esc(mockHeaders)).append("</code></pre>\n");
            }

            String method = api.getMethod().toUpperCase();
            if (!"GET".equals(method) && !"DELETE".equals(method)) {
                String mockReq = MockDataGenerator.generateMockRequest(api.getParams());
                if (!"{}".equals(mockReq)) {
                    sb.append("    <p><strong>Body JSON:</strong></p>\n")
                            .append("    <pre><code>").append(esc(mockReq)).append("</code></pre>\n");
                }
            }
            sb.append("  </div>\n");
        }

        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            String mockResp = MockDataGenerator.generateMockResponse(api.getReturnTypeFields(), api.getReturnType());
            if (!"{}".equals(mockResp)) {
                sb.append("  <div class=\"fields-section\">\n")
                        .append("    <h4>Sample Response</h4>\n")
                        .append("    <pre><code>").append(esc(mockResp)).append("</code></pre>\n")
                        .append("  </div>\n");
            }
        }

        sb.append("</div>\n");
    }

    private void writeFieldTable(StringBuilder sb, java.util.List<FieldInfo> fields, String typeName, int depth) {
        String indent = "      ".repeat(depth + 1);
        sb.append(indent).append("<div class=\"field-type\"><code>").append(esc(typeName))
                .append("</code> ").append(msg.fields()).append(":</div>\n");
        sb.append(indent).append("<table class=\"fields\">\n")
                .append(indent)
                .append("<tr><th>").append(msg.headerField()).append("</th><th>").append(msg.headerType())
                .append("</th><th>").append(msg.headerRequired()).append("</th><th>").append(msg.headerDefault())
                .append("</th><th>").append(msg.headerDescription()).append("</th></tr>\n");
        for (FieldInfo f : fields) {
            sb.append(indent).append("<tr>")
                    .append("<td>").append(esc(f.getName())).append("</td>")
                    .append("<td><code>").append(esc(f.getType())).append("</code></td>")
                    .append("<td>").append(f.isRequired() ? "\u2713" : "").append("</td>")
                    .append("<td>").append(f.getDefaultValue() != null ? esc(f.getDefaultValue()) : "").append("</td>")
                    .append("<td>").append(f.getDescription() != null ? esc(f.getDescription()) : "").append("</td>")
                    .append("</tr>\n");
            if (f.hasChildren() && depth < 2) {
                sb.append(indent).append("<tr><td colspan=\"5\">\n");
                writeFieldTable(sb, f.getChildren(), f.getType(), depth + 1);
                sb.append(indent).append("</td></tr>\n");
            }
        }
        sb.append(indent).append("</table>\n");
    }

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String anchorId(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private static final String CSS = """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                   line-height: 1.6; max-width: 1200px; margin: 0 auto; padding: 20px; color: #333; background: #f8f9fa; }
            .header { text-align: center; padding: 30px 0; border-bottom: 2px solid #e0e0e0; margin-bottom: 30px; }
            .header h1 { font-size: 2em; color: #1a1a2e; }
            .version { color: #666; font-size: 0.9em; }
            .toc { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 30px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
            .toc h2 { font-size: 1.2em; margin-bottom: 10px; }
            .toc ul { list-style: none; padding-left: 10px; }
            .toc li { margin: 5px 0; }
            .toc a { color: #0366d6; text-decoration: none; }
            .toc a:hover { text-decoration: underline; }
            .group { background: #fff; padding: 25px; border-radius: 8px; margin-bottom: 25px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
            .group h2 { font-size: 1.5em; color: #1a1a2e; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 15px; }
            .base-path { color: #666; font-size: 0.9em; margin-bottom: 15px; }
            .endpoint { border: 1px solid #e0e0e0; border-radius: 6px; margin: 15px 0; overflow: hidden; }
            .endpoint-header { display: flex; align-items: center; gap: 12px; padding: 12px 15px; background: #f5f5f5; }
            .method { font-weight: 700; font-size: 0.85em; padding: 4px 10px; border-radius: 4px; color: #fff; text-transform: uppercase; }
            .method-get { background: #61affe; }
            .method-post { background: #49cc90; }
            .method-put { background: #fca130; }
            .method-delete { background: #f93e3e; }
            .method-patch { background: #50e3c2; }
            .method-head { background: #9012fe; }
            .method-options { background: #0d5aa7; }
            .path { font-family: monospace; font-size: 1em; color: #333; }
            .deprecated { background: #ffebcc; color: #b35900; padding: 2px 8px; border-radius: 3px; font-size: 0.8em; font-weight: 600; }
            .summary { padding: 10px 15px; color: #555; }
            .return { padding: 5px 15px; color: #666; font-size: 0.9em; }
            .params { width: 100%; border-collapse: collapse; margin: 0; }
            .params th { background: #f0f0f0; text-align: left; padding: 8px 12px; font-size: 0.85em; color: #555; border-top: 1px solid #e0e0e0; }
            .params td { padding: 8px 12px; border-top: 1px solid #f0f0f0; font-size: 0.9em; }
            .params tr:hover { background: #f9f9f9; }
            .fields-section { padding: 10px 15px; }
            .fields-section h4 { font-size: 0.95em; color: #444; margin-bottom: 8px; }
            .field-type { font-size: 0.85em; color: #666; margin: 8px 0 4px 0; }
            .fields { width: 100%; border-collapse: collapse; margin: 4px 0 8px 15px; border: 1px solid #e8e8e8; border-radius: 4px; }
            .fields th { background: #f5f5f5; text-align: left; padding: 6px 10px; font-size: 0.8em; color: #666; }
            .fields td { padding: 6px 10px; border-top: 1px solid #f0f0f0; font-size: 0.85em; }
            .fields tr:hover { background: #fafafa; }
            code { background: #f0f0f0; padding: 2px 5px; border-radius: 3px; font-size: 0.9em; }
            .desc { color: #555; margin: 5px 0 15px 0; }
            """;
}
