package org.assistant.tools.doc.export;

import org.apache.poi.xwpf.usermodel.*;
import org.assistant.tools.doc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports API definitions to a Word (.docx) file using Apache POI.
 * <p>
 * Generates a professional document with a title page, table of contents
 * per controller group, and detailed tables for each endpoint.
 * </p>
 * <p>
 * Use {@link FieldLayout#INLINE} to render nested DTO fields as indented
 * rows within the parameter table, or {@link FieldLayout#SEPARATE} to
 * render them as separate detail tables below each endpoint.
 * </p>
 */
public class DocxExporter implements ApiExporter {

    /**
     * Controls how nested DTO/POJO field details are rendered.
     */
    public enum FieldLayout {
        /** Nested fields appear as indented rows inside the parameter table */
        INLINE,
        /** Nested fields appear in separate tables below the parameter table */
        SEPARATE
    }

    private final FieldLayout fieldLayout;
    private final ExportMessages msg = ExportMessages.getInstance();

    /** Creates a DocxExporter with the default INLINE field layout. */
    public DocxExporter() {
        this(FieldLayout.INLINE);
    }

    /** Creates a DocxExporter with the specified field layout. */
    public DocxExporter(FieldLayout fieldLayout) {
        this.fieldLayout = fieldLayout;
    }

    @Override
    public String getFormatName() {
        return fieldLayout == FieldLayout.INLINE
                ? "Word (DOCX) - Inline Fields"
                : "Word (DOCX) - Separate Tables";
    }

    @Override
    public String getFileExtension() {
        return "docx";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Create built-in heading styles so Word navigation pane works
            createHeadingStyles(document);

            // Title
            XWPFParagraph title = document.createParagraph();
            title.setStyle("Title");
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(project.getProjectName() != null
                    ? project.getProjectName() + " " + msg.apiDocumentation()
                    : msg.apiDocumentation());
            titleRun.setBold(true);
            titleRun.setFontSize(24);

            // Version / description
            if (project.getVersion() != null) {
                XWPFParagraph versionPara = document.createParagraph();
                versionPara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun versionRun = versionPara.createRun();
                versionRun.setText(msg.version() + ": " + project.getVersion());
                versionRun.setFontSize(12);
                versionRun.setColor("666666");
            }
            if (project.getDescription() != null) {
                XWPFParagraph descPara = document.createParagraph();
                descPara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun descRun = descPara.createRun();
                descRun.setText(project.getDescription());
                descRun.setColor("888888");
            }

            // Page break after title
            document.createParagraph().setPageBreak(true);

            // Table of Contents
            XWPFParagraph tocTitle = document.createParagraph();
            tocTitle.setStyle("Heading1");
            XWPFRun tocRun = tocTitle.createRun();
            tocRun.setText(msg.tableOfContents());
            tocRun.setBold(true);
            tocRun.setFontSize(18);

            // TOC field — Word will populate this when user presses "Update Field"
            XWPFParagraph tocField = document.createParagraph();
            tocField.getCTP().addNewFldSimple().setInstr("TOC \\o \"1-3\" \\h \\z \\u");
            XWPFRun tocHint = tocField.createRun();
            tocHint.setText(msg.tocHint());
            tocHint.setItalic(true);
            tocHint.setColor("999999");
            tocHint.setFontSize(9);

            // Page break after TOC
            document.createParagraph().setPageBreak(true);

            // Each group
            for (ApiGroup group : project.getGroups()) {
                writeGroup(document, group);
            }

            try (FileOutputStream out = new FileOutputStream(output)) {
                document.write(out);
            }
        }
    }

    /**
     * Create built-in heading styles (Heading1–3 + Title) so that Word's
     * navigation pane can list all groups and endpoints for quick jumping.
     */
    private void createHeadingStyles(XWPFDocument document) {
        // Ensure styles part exists
        XWPFStyles styles = document.createStyles();

        // Title style
        addHeadingStyle(styles, "Title", 0, 26, "1B2A4A", true);
        // Heading 1 — controller groups
        addHeadingStyle(styles, "Heading1", 1, 18, "1B2A4A", true);
        // Heading 2 — API endpoints
        addHeadingStyle(styles, "Heading2", 2, 14, "2E5090", true);
        // Heading 3 — sections (Parameters, Response Fields)
        addHeadingStyle(styles, "Heading3", 3, 12, "444444", true);
    }

    private void addHeadingStyle(XWPFStyles styles, String styleId, int outlineLevel,
            int fontSize, String color, boolean bold) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle ctStyle = org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle.Factory
                .newInstance();
        ctStyle.setStyleId(styleId);
        ctStyle.setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.PARAGRAPH);

        // Name
        ctStyle.addNewName().setVal(styleId.replaceAll("(\\d)", " $1").trim());

        // Paragraph properties — outline level for navigation
        var pPr = ctStyle.addNewPPr();
        if (outlineLevel > 0) {
            pPr.addNewOutlineLvl().setVal(java.math.BigInteger.valueOf(outlineLevel - 1));
        }
        pPr.addNewSpacing().setBefore(java.math.BigInteger.valueOf(240));
        pPr.addNewSpacing().setAfter(java.math.BigInteger.valueOf(120));

        // Run properties — font styling
        var rPr = ctStyle.addNewRPr();
        rPr.addNewSz().setVal(java.math.BigInteger.valueOf(fontSize * 2L));
        rPr.addNewColor().setVal(color);
        if (bold) {
            rPr.addNewB().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff.ON);
        }

        XWPFStyle style = new XWPFStyle(ctStyle);
        style.setType(org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType.PARAGRAPH);
        styles.addStyle(style);
    }

    private void writeGroup(XWPFDocument document, ApiGroup group) {
        // Group heading
        XWPFParagraph heading = document.createParagraph();
        heading.setStyle("Heading1");
        XWPFRun headingRun = heading.createRun();
        headingRun.setText(group.getName());
        headingRun.setBold(true);
        headingRun.setFontSize(16);

        if (group.getDescription() != null && !group.getDescription().isEmpty()) {
            XWPFParagraph desc = document.createParagraph();
            desc.createRun().setText(group.getDescription());
        }

        if (group.getBasePath() != null && !group.getBasePath().isEmpty()) {
            XWPFParagraph basePath = document.createParagraph();
            XWPFRun bpRun = basePath.createRun();
            bpRun.setText(msg.basePath() + ": " + group.getBasePath());
            bpRun.setItalic(true);
        }

        // Each endpoint
        for (WebApiInfo api : group.getApis()) {
            writeEndpoint(document, api);
        }
    }

    private void writeEndpoint(XWPFDocument document, WebApiInfo api) {
        // Endpoint heading
        XWPFParagraph epHeading = document.createParagraph();
        epHeading.setStyle("Heading2");
        XWPFRun epRun = epHeading.createRun();
        String headingText = api.getMethod() + " " + api.getPath();
        if (api.isDeprecated()) {
            headingText += " [" + msg.deprecated() + "]";
        }
        epRun.setText(headingText);
        epRun.setBold(true);
        epRun.setFontSize(13);

        if (api.getSummary() != null && !api.getSummary().isEmpty()) {
            XWPFParagraph summary = document.createParagraph();
            summary.createRun().setText(api.getSummary());
        }

        // Metadata
        if (api.getFrontendReturnType() != null && !"object".equals(api.getFrontendReturnType())
                && !"void".equalsIgnoreCase(api.getReturnType())) {
            XWPFParagraph retPara = document.createParagraph();
            XWPFRun retRun = retPara.createRun();
            retRun.setText(msg.returnType() + ": " + api.getFrontendReturnType());
            retRun.setItalic(true);
        }

        // Parameters
        List<ApiParam> params = api.getParams();
        if (!params.isEmpty()) {
            if (fieldLayout == FieldLayout.INLINE) {
                writeParamsInline(document, params);
            } else {
                writeParamsSeparate(document, params);
            }
        }

        // Return type fields
        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            if (fieldLayout == FieldLayout.INLINE) {
                writeReturnFieldsInline(document, api);
            } else {
                writeFieldTableSeparate(document, api.getReturnTypeFields(),
                        msg.responseFields() + ": " + api.getFrontendReturnType());
            }
        }

        if (!api.getParams().isEmpty() || api.getPath() != null) {
            XWPFParagraph reqTitle = document.createParagraph();
            XWPFRun reqRun = reqTitle.createRun();
            reqRun.setBold(true);
            reqRun.setText("Sample Request:");

            String mockUrl = MockDataGenerator.generateMockUrl(api);
            XWPFParagraph urlPara = document.createParagraph();
            XWPFRun urlRun = urlPara.createRun();
            urlRun.setBold(true);
            urlRun.setText("URL: ");
            XWPFRun urlDataRun = urlPara.createRun();
            urlDataRun.setFontFamily("Consolas");
            urlDataRun.setFontSize(9);
            urlDataRun.setText(mockUrl);

            String mockHeaders = MockDataGenerator.generateMockHeaders(api.getParams());
            if (!mockHeaders.isEmpty()) {
                XWPFParagraph hPara = document.createParagraph();
                XWPFRun hRun = hPara.createRun();
                hRun.setBold(true);
                hRun.setText("Headers:");
                XWPFParagraph hDataPara = document.createParagraph();
                XWPFRun hDataRun = hDataPara.createRun();
                hDataRun.setFontFamily("Consolas");
                hDataRun.setFontSize(9);
                for (String line : mockHeaders.split("\n")) {
                    hDataRun.setText(line);
                    hDataRun.addBreak();
                }
            }

            String method = api.getMethod().toUpperCase();
            if (!"GET".equals(method) && !"DELETE".equals(method)) {
                String mockReq = MockDataGenerator.generateMockRequest(api.getParams());
                if (!"{}".equals(mockReq)) {
                    XWPFParagraph bodyPara = document.createParagraph();
                    XWPFRun bodyRun = bodyPara.createRun();
                    bodyRun.setBold(true);
                    bodyRun.setText("Body:");

                    XWPFParagraph reqData = document.createParagraph();
                    XWPFRun reqDataRun = reqData.createRun();
                    reqDataRun.setFontFamily("Consolas");
                    reqDataRun.setFontSize(9);
                    if (mockReq.contains("\n")) {
                        for (String line : mockReq.split("\n")) {
                            reqDataRun.setText(line);
                            reqDataRun.addBreak();
                        }
                    } else {
                        reqDataRun.setText(mockReq);
                    }
                }
            }
        }

        if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
            String mockResp = MockDataGenerator.generateMockResponse(api.getReturnTypeFields(),
                    api.getFrontendReturnType());
            if (!"{}".equals(mockResp)) {
                XWPFParagraph respTitle = document.createParagraph();
                XWPFRun respRun = respTitle.createRun();
                respRun.setBold(true);
                respRun.setText("Sample Response:");

                XWPFParagraph respData = document.createParagraph();
                XWPFRun respDataRun = respData.createRun();
                respDataRun.setFontFamily("Consolas");
                respDataRun.setFontSize(9);
                if (mockResp.contains("\n")) {
                    for (String line : mockResp.split("\n")) {
                        respDataRun.setText(line);
                        respDataRun.addBreak();
                    }
                } else {
                    respDataRun.setText(mockResp);
                }
            }
        }

        document.createParagraph(); // Spacer
    }

    // ==================== INLINE layout ====================

    private void writeParamsInline(XWPFDocument document, List<ApiParam> params) {
        XWPFParagraph paramTitle = document.createParagraph();
        XWPFRun ptRun = paramTitle.createRun();
        ptRun.setText(msg.parameters() + ":");
        ptRun.setBold(true);

        // Collect all rows (params + nested field sub-rows)
        List<String[]> allRows = new ArrayList<>();
        for (ApiParam param : params) {
            allRows.add(new String[] {
                    param.getName(),
                    param.getIn() != null ? param.getIn().name().toLowerCase() : "",
                    param.getFrontendDataType(),
                    param.isRequired() ? msg.yes() : msg.no(),
                    param.getDefaultValue() != null ? param.getDefaultValue() : "",
                    param.getDescription() != null ? param.getDescription() : ""
            });
            if (param.hasFields()) {
                collectFieldRows(allRows, param.getFields(), 1);
            }
        }

        writeTableFromRows(document,
                new String[] { msg.headerName(), msg.headerLocation(), msg.headerType(),
                        msg.headerRequired(), msg.headerDefault(), msg.headerDescription() },
                allRows, 11);
    }

    private void writeReturnFieldsInline(XWPFDocument document, WebApiInfo api) {
        XWPFParagraph retTitle = document.createParagraph();
        XWPFRun retRun = retTitle.createRun();
        retRun.setText(msg.responseFields() + " (" + api.getFrontendReturnType() + "):");
        retRun.setBold(true);

        List<String[]> fieldRows = new ArrayList<>();
        collectFieldRows(fieldRows, api.getReturnTypeFields(), 0);

        writeTableFromRows(document,
                new String[] { msg.headerField(), msg.headerLocation(), msg.headerType(),
                        msg.headerRequired(), msg.headerDefault(), msg.headerDescription() },
                fieldRows, 9);
    }

    private void collectFieldRows(List<String[]> rows, List<FieldInfo> fields, int depth) {
        String indent = "  ".repeat(depth);
        String prefix = depth > 0 ? "▪ " : "";
        for (FieldInfo field : fields) {
            rows.add(new String[] {
                    indent + prefix + field.getName(),
                    "",
                    field.getFrontendType(),
                    field.isRequired() ? msg.yes() : msg.no(),
                    field.getDefaultValue() != null ? field.getDefaultValue() : "",
                    field.getDescription() != null ? field.getDescription() : ""
            });
            if (field.hasChildren()) {
                collectFieldRows(rows, field.getChildren(), depth + 1);
            }
        }
    }

    // ==================== SEPARATE layout ====================

    private void writeParamsSeparate(XWPFDocument document, List<ApiParam> params) {
        XWPFParagraph paramTitle = document.createParagraph();
        XWPFRun ptRun = paramTitle.createRun();
        ptRun.setText(msg.parameters() + ":");
        ptRun.setBold(true);

        // Basic param table (no nested fields)
        List<String[]> paramRows = new ArrayList<>();
        for (ApiParam param : params) {
            paramRows.add(new String[] {
                    param.getName(),
                    param.getIn() != null ? param.getIn().name().toLowerCase() : "",
                    param.getFrontendDataType(),
                    param.isRequired() ? msg.yes() : msg.no(),
                    param.getDefaultValue() != null ? param.getDefaultValue() : "",
                    param.getDescription() != null ? param.getDescription() : ""
            });
        }
        writeTableFromRows(document,
                new String[] { msg.headerName(), msg.headerLocation(), msg.headerType(),
                        msg.headerRequired(), msg.headerDefault(), msg.headerDescription() },
                paramRows, 11);

        // Separate field detail tables for complex param types
        for (ApiParam param : params) {
            if (param.hasFields()) {
                writeFieldTableSeparate(document, param.getFields(), param.getFrontendDataType());
            }
        }
    }

    private void writeFieldTableSeparate(XWPFDocument document, List<FieldInfo> fields, String typeName) {
        XWPFParagraph title = document.createParagraph();
        XWPFRun titleRun = title.createRun();
        titleRun.setText(typeName + " " + msg.fields() + ":");
        titleRun.setBold(true);
        titleRun.setFontSize(10);
        titleRun.setItalic(true);

        List<String[]> fieldRows = new ArrayList<>();
        for (FieldInfo field : fields) {
            fieldRows.add(new String[] {
                    field.getName(),
                    field.getFrontendType(),
                    field.isRequired() ? msg.yes() : msg.no(),
                    field.getDefaultValue() != null ? field.getDefaultValue() : "",
                    field.getDescription() != null ? field.getDescription() : ""
            });
        }

        writeTableFromRows(document,
                new String[] { msg.headerField(), msg.headerType(), msg.headerRequired(),
                        msg.headerDefault(), msg.headerDescription() },
                fieldRows, 9);

        // Recursively write nested field tables
        for (FieldInfo field : fields) {
            if (field.hasChildren()) {
                writeFieldTableSeparate(document, field.getChildren(), field.getFrontendType());
            }
        }
    }

    // ==================== Shared helpers ====================

    private void writeTableFromRows(XWPFDocument document, String[] headers,
            List<String[]> dataRows, int headerFontSize) {
        XWPFTable table = document.createTable(dataRows.size() + 1, headers.length);
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            XWPFParagraph p = headerRow.getCell(i).getParagraphArray(0);
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setText(headers[i]);
            r.setBold(true);
            r.setFontSize(headerFontSize);
        }

        for (int row = 0; row < dataRows.size(); row++) {
            String[] data = dataRows.get(row);
            XWPFTableRow dataRow = table.getRow(row + 1);
            for (int col = 0; col < data.length; col++) {
                setCellText(dataRow, col, data[col]);
            }
        }
    }

    private void setCellText(XWPFTableRow row, int col, String text) {
        XWPFTableCell cell = row.getCell(col);
        cell.getParagraphArray(0).createRun().setText(text != null ? text : "");
    }
}
