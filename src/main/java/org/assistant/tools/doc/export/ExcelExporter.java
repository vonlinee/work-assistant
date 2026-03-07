package org.assistant.tools.doc.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.assistant.tools.doc.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports API definitions to an Excel (.xlsx) file using EasyExcel.
 * <p>
 * Each controller group gets its own sheet. Parameters are expanded
 * to individual rows, and complex type fields are rendered as nested
 * sub-rows with indentation.
 * </p>
 */
public class ExcelExporter implements ApiExporter {

    private final ExportMessages msg = ExportMessages.getInstance();

    @Override
    public String getFormatName() {
        return "Excel";
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        List<List<String>> headers = new ArrayList<>();
        headers.add(List.of(msg.headerMethod()));
        headers.add(List.of(msg.headerPath()));
        headers.add(List.of(msg.headerSummary()));
        headers.add(List.of(msg.headerField()));
        headers.add(List.of(msg.headerLocation()));
        headers.add(List.of(msg.headerType()));
        headers.add(List.of(msg.headerRequired()));
        headers.add(List.of(msg.headerExample()));
        headers.add(List.of(msg.headerDefault()));
        headers.add(List.of(msg.headerDescription()));

        if (project.getGroups().isEmpty()) {
            return;
        }

        var writer = EasyExcel.write(output)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .head(headers)
                .needHead(true)
                .build();

        try {
            int sheetNo = 0;
            for (ApiGroup group : project.getGroups()) {
                List<List<Object>> rows = new ArrayList<>();

                for (WebApiInfo api : group.getApis()) {
                    // Endpoint header row
                    List<Object> headerRow = new ArrayList<>();
                    headerRow.add(api.getMethod());
                    headerRow.add(api.getPath());
                    headerRow.add(api.getSummary() != null ? api.getSummary() : "");
                    headerRow.add(""); // Param/Field
                    headerRow.add(""); // Location
                    headerRow.add(api.getFrontendReturnType() != null && !"object".equals(api.getFrontendReturnType())
                            && !"void".equalsIgnoreCase(api.getReturnType()) ? api.getFrontendReturnType() : "");
                    headerRow.add(api.isDeprecated() ? msg.deprecated() : "");
                    headerRow.add(""); // Example
                    headerRow.add(""); // Default
                    headerRow.add(api.getDescription() != null ? api.getDescription() : "");
                    rows.add(headerRow);

                    // Parameter rows
                    for (ApiParam param : api.getParams()) {
                        rows.add(buildParamRow(param));

                        // Nested field rows for complex types
                        if (param.hasFields()) {
                            addFieldRows(rows, param.getFields(), 1);
                        }
                    }

                    // Return type fields
                    if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
                        List<Object> retRow = new ArrayList<>();
                        retRow.add("");
                        retRow.add("");
                        retRow.add("");
                        retRow.add("▸ " + msg.responseFields());
                        retRow.add("");
                        retRow.add("");
                        retRow.add("");
                        retRow.add("");
                        retRow.add("");
                        retRow.add("");
                        rows.add(retRow);
                        addFieldRows(rows, api.getReturnTypeFields(), 1);
                    }

                    // Blank separator row between endpoints
                    rows.add(List.of("", "", "", "", "", "", "", "", "", ""));
                }

                var sheet = EasyExcel.writerSheet(sheetNo, group.getName()).build();
                writer.write(rows, sheet);
                sheetNo++;
            }
        } finally {
            writer.finish();
        }
    }

    private List<Object> buildParamRow(ApiParam param) {
        List<Object> row = new ArrayList<>();
        row.add(""); // Method (blank — belongs to header row)
        row.add(""); // Path
        row.add(""); // Summary
        row.add("  " + param.getName());
        row.add(param.getIn() != null ? param.getIn().name().toLowerCase() : "");
        row.add(param.getFrontendDataType());
        row.add(param.isRequired() ? "✓" : "");
        row.add(param.getExample() != null ? param.getExample() : "");
        row.add(param.getDefaultValue() != null ? param.getDefaultValue() : "");
        row.add(param.getDescription() != null ? param.getDescription() : "");
        return row;
    }

    private void addFieldRows(List<List<Object>> rows, List<FieldInfo> fields, int depth) {
        String indent = "    ".repeat(depth);
        for (FieldInfo field : fields) {
            List<Object> row = new ArrayList<>();
            row.add(""); // Method
            row.add(""); // Path
            row.add(""); // Summary
            row.add(indent + "▪ " + field.getName());
            row.add(""); // Location (fields don't have a location)
            row.add(field.getFrontendType());
            row.add(field.isRequired() ? "✓" : "");
            row.add(field.getExample() != null ? field.getExample() : "");
            row.add(field.getDefaultValue() != null ? field.getDefaultValue() : "");
            row.add(field.getDescription() != null ? field.getDescription() : "");
            rows.add(row);

            // Recursive children
            if (field.hasChildren()) {
                addFieldRows(rows, field.getChildren(), depth + 1);
            }
        }
    }
}
