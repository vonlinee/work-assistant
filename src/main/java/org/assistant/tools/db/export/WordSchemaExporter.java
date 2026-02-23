package org.assistant.tools.db.export;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;

import java.io.File;
import java.io.FileOutputStream;

public class WordSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "Word (*.docx)";
    }

    @Override
    public String getFileExtension() {
        return "docx";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("Database Schema Documentation");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            for (TableInfo table : schema.getTables()) {
                document.createParagraph().createRun().addBreak(); // spacer

                XWPFParagraph tableTitle = document.createParagraph();
                XWPFRun tableTitleRun = tableTitle.createRun();
                tableTitleRun.setText("Table: " + table.getName());
                tableTitleRun.setBold(true);
                tableTitleRun.setFontSize(14);

                if (table.getRemarks() != null && !table.getRemarks().isEmpty()) {
                    XWPFParagraph remarksParagraph = document.createParagraph();
                    XWPFRun remarksRun = remarksParagraph.createRun();
                    remarksRun.setText("Description: " + table.getRemarks());
                    remarksRun.setItalic(true);
                }

                XWPFTable wordTable = document.createTable();
                wordTable.setWidth("100%");

                // Header row
                XWPFTableRow headerRow = wordTable.getRow(0);
                headerRow.getCell(0).setText("Column");
                headerRow.addNewTableCell().setText("Type");
                headerRow.addNewTableCell().setText("Size");
                headerRow.addNewTableCell().setText("PK");
                headerRow.addNewTableCell().setText("Auto");
                headerRow.addNewTableCell().setText("Nullable");
                headerRow.addNewTableCell().setText("Default");
                headerRow.addNewTableCell().setText("Remarks");

                for (ColumnInfo col : table.getColumns()) {
                    XWPFTableRow dataRow = wordTable.createRow();
                    dataRow.getCell(0).setText(col.getName() != null ? col.getName() : "");
                    dataRow.getCell(1).setText(col.getTypeName() != null ? col.getTypeName() : "");
                    dataRow.getCell(2).setText(String.valueOf(col.getSize()));
                    dataRow.getCell(3).setText(col.isPrimaryKey() ? "Yes" : "No");
                    dataRow.getCell(4).setText(col.isAutoIncrement() ? "Yes" : "No");
                    dataRow.getCell(5).setText(col.isNullable() ? "Yes" : "No");
                    dataRow.getCell(6).setText(col.getDefaultValue() != null ? col.getDefaultValue() : "");
                    dataRow.getCell(7).setText(col.getRemarks() != null ? col.getRemarks() : "");
                }
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }
    }
}
