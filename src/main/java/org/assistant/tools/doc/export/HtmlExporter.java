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

        // Split layout container
        sb.append("<div class=\"container\">\n");

        // Sidebar / TOC
        sb.append("  <nav class=\"sidebar\">\n")
                .append("    <h2>").append(msg.tableOfContents()).append("</h2>\n")
                .append("    <input type=\"text\" id=\"apiSearch\" placeholder=\"Search APIs...\" onkeyup=\"filterApis()\" class=\"search-input\">\n")
                .append("    <ul id=\"groupList\">\n");
        for (ApiGroup group : project.getGroups()) {
            sb.append("      <li>\n")
                    .append("        <a href=\"#").append(anchorId(group.getName())).append("\">")
                    .append(esc(group.getName())).append("</a>\n")
                    .append("        <ul class=\"api-list\">\n");
            for (WebApiInfo api : group.getApis()) {
                String apiAnchor = anchorId(group.getName() + "-" + api.getMethod() + "-" + api.getPath());
                sb.append("          <li><a href=\"#").append(apiAnchor).append("\">")
                        .append("<span class=\"menu-method method-").append(api.getMethod().toLowerCase()).append("\">")
                        .append(api.getMethod()).append("</span> ")
                        .append(esc(api.getPath())).append("</a></li>\n");
            }
            sb.append("        </ul>\n      </li>\n");
        }
        sb.append("    </ul>\n  </nav>\n");

        // Main content area
        sb.append("  <main class=\"content\">\n");

        // Groups
        for (ApiGroup group : project.getGroups()) {
            writeGroup(sb, group);
        }

        sb.append("  </main>\n") // End main content
                .append("</div>\n") // End container
                .append("<script>\n")
                .append("function filterApis() {\n")
                .append("  let input = document.getElementById('apiSearch').value.toLowerCase();\n")
                .append("  let groups = document.querySelectorAll('#groupList > li');\n")
                .append("  groups.forEach(group => {\n")
                .append("    let groupLinks = group.querySelectorAll('.api-list li');\n")
                .append("    let groupVisible = false;\n")
                .append("    groupLinks.forEach(link => {\n")
                .append("      let text = link.textContent.toLowerCase();\n")
                .append("      if (text.includes(input)) {\n")
                .append("        link.style.display = '';\n")
                .append("        groupVisible = true;\n")
                .append("      } else {\n")
                .append("        link.style.display = 'none';\n")
                .append("      }\n")
                .append("    });\n")
                .append("    if (groupVisible || group.querySelector('a').textContent.toLowerCase().includes(input)) {\n")
                .append("       group.style.display = '';\n")
                .append("    } else {\n")
                .append("       group.style.display = 'none';\n")
                .append("    }\n")
                .append("  });\n")
                .append("}\n")
                .append("</script>\n")
                .append("</body>\n</html>\n");
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
            writeEndpoint(sb, api, group.getName());
        }
        sb.append("</section>\n");
    }

    private void writeEndpoint(StringBuilder sb, WebApiInfo api, String groupName) {
        String methodClass = "method-" + api.getMethod().toLowerCase();
        String apiAnchor = anchorId(groupName + "-" + api.getMethod() + "-" + api.getPath());
        sb.append("<div class=\"endpoint\" id=\"").append(apiAnchor).append("\">\n")
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
