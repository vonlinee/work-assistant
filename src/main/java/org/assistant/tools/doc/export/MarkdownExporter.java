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
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(project.getProjectName() != null ? project.getProjectName() : "API")
                .append(" ").append(msg.apiDocumentation()).append("\n\n");

        if (project.getVersion() != null) {
            sb.append("**").append(msg.version()).append(":** ").append(project.getVersion()).append("\n\n");
        }
        if (project.getDescription() != null) {
            sb.append(project.getDescription()).append("\n\n");
        }

        // TOC
        sb.append("## ").append(msg.tableOfContents()).append("\n\n");
        for (ApiGroup group : project.getGroups()) {
            sb.append("- [").append(group.getName()).append("](#")
                    .append(toAnchor(group.getName())).append(")\n");
        }
        sb.append("\n---\n\n");

        // Groups
        for (ApiGroup group : project.getGroups()) {
            writeGroup(sb, group);
        }

        Files.writeString(output.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private void writeGroup(StringBuilder sb, ApiGroup group) {
        sb.append("## ").append(group.getName()).append("\n\n");

        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            sb.append(group.getDescription()).append("\n\n");
        }
        if (group.getBasePath() != null && !group.getBasePath().isEmpty()) {
            sb.append("**").append(msg.basePath()).append(":** `").append(group.getBasePath()).append("`\n\n");
        }

        for (WebApiInfo api : group.getApis()) {
            writeEndpoint(sb, api);
        }
    }

    private void writeEndpoint(StringBuilder sb, WebApiInfo api) {
        sb.append("### ").append(api.getMethod()).append(" `").append(api.getPath()).append("`");
        if (api.isDeprecated()) {
            sb.append(" ⚠️ ").append(msg.deprecated());
        }
        sb.append("\n\n");

        if (api.getSummary() != null && !api.getSummary().isEmpty()) {
            sb.append(api.getSummary()).append("\n\n");
        }

        if (api.getReturnType() != null) {
            sb.append("**").append(msg.returnType()).append(":** `").append(api.getReturnType()).append("`\n\n");
        }

        if (!api.getConsumes().isEmpty()) {
            sb.append("**Consumes:** ").append(String.join(", ", api.getConsumes())).append("\n\n");
        }
        if (!api.getProduces().isEmpty()) {
            sb.append("**Produces:** ").append(String.join(", ", api.getProduces())).append("\n\n");
        }

        if (!api.getParams().isEmpty()) {
            sb.append("**").append(msg.parameters()).append(":**\n\n");
            sb.append("| ").append(msg.headerName()).append(" | ").append(msg.headerIn())
                    .append(" | ").append(msg.headerType()).append(" | ").append(msg.headerRequired())
                    .append(" | ").append(msg.headerDefault()).append(" | ").append(msg.headerDescription())
                    .append(" |\n");
            sb.append("|------|-----|------|----------|---------|-------------|\n");
            for (ApiParam p : api.getParams()) {
                sb.append("| ").append(p.getName() != null ? p.getName() : "")
                        .append(" | ").append(p.getIn() != null ? p.getIn().name().toLowerCase() : "")
                        .append(" | `").append(p.getDataType() != null ? p.getDataType() : "").append("`")
                        .append(" | ").append(p.isRequired() ? "\u2713" : "")
                        .append(" | ").append(p.getDefaultValue() != null ? p.getDefaultValue() : "")
                        .append(" | ").append(p.getDescription() != null ? p.getDescription() : "")
                        .append(" |\n");
                // Render resolved fields
                if (p.hasFields()) {
                    sb.append("\n");
                    writeFieldTable(sb, p.getFields(), p.getDataType(), 0);
                }
            }
            sb.append("\n");
        }

        // Return type fields
        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            sb.append("**").append(msg.responseFields()).append(":**\n\n");
            writeFieldTable(sb, api.getReturnTypeFields(), api.getReturnType(), 0);
        }

        if (!api.getParams().isEmpty() || api.getPath() != null) {
            sb.append("**Sample Request:**\n\n");
            String mockUrl = MockDataGenerator.generateMockUrl(api);
            sb.append("**URL:** `").append(mockUrl).append("`\n\n");

            String mockHeaders = MockDataGenerator.generateMockHeaders(api.getParams());
            if (!mockHeaders.isEmpty()) {
                sb.append("**Headers:**\n```text\n").append(mockHeaders).append("```\n\n");
            }

            String method = api.getMethod().toUpperCase();
            if (!"GET".equals(method) && !"DELETE".equals(method)) {
                String mockReq = MockDataGenerator.generateMockRequest(api.getParams());
                if (!"{}".equals(mockReq)) {
                    sb.append("**Body:**\n```json\n").append(mockReq).append("\n```\n\n");
                }
            }
        }

        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            String mockResp = MockDataGenerator.generateMockResponse(api.getReturnTypeFields(), api.getReturnType());
            if (!"{}".equals(mockResp)) {
                sb.append("**Sample Response:**\n\n```json\n").append(mockResp).append("\n```\n\n");
            }
        }

        sb.append("---\n\n");
    }

    private void writeFieldTable(StringBuilder sb, java.util.List<FieldInfo> fields, String typeName, int depth) {
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
        // Nested children
        for (FieldInfo f : fields) {
            if (f.hasChildren() && depth < 2) {
                writeFieldTable(sb, f.getChildren(), f.getType(), depth + 1);
            }
        }
    }

    private String toAnchor(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
