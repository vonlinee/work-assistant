package org.assistant.tools.db.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "Excel (*.xlsx)";
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile).build()) {
            int sheetNo = 0;

            for (TableInfo table : schema.getTables()) {
                String sheetName = table.getName();
                if (sheetName.length() > 31) {
                    sheetName = sheetName.substring(0, 31); // Excel sheet name limit
                }

                WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo++, sheetName)
                        .head(getHead())
                        .build();

                List<List<Object>> data = new ArrayList<>();
                // Insert a pseudo description row if we want, but column headers might
                // misalign.
                // We will stick to pure data rows matching headers.
                for (ColumnInfo col : table.getColumns()) {
                    List<Object> row = new ArrayList<>();
                    row.add(col.getName());
                    row.add(col.getTypeName());
                    row.add(col.getSize());
                    row.add(col.isPrimaryKey() ? "Yes" : "No");
                    row.add(col.isAutoIncrement() ? "Yes" : "No");
                    row.add(col.isNullable() ? "Yes" : "No");
                    row.add(col.getDefaultValue() != null ? col.getDefaultValue() : "");
                    row.add(col.getRemarks() != null ? col.getRemarks() : "");
                    data.add(row);
                }
                excelWriter.write(data, writeSheet);
            }
        }
    }

    private List<List<String>> getHead() {
        List<List<String>> list = new ArrayList<>();
        list.add(Arrays.asList("Column"));
        list.add(Arrays.asList("Type"));
        list.add(Arrays.asList("Size"));
        list.add(Arrays.asList("Primary Key"));
        list.add(Arrays.asList("Auto Increment"));
        list.add(Arrays.asList("Nullable"));
        list.add(Arrays.asList("Default"));
        list.add(Arrays.asList("Remarks"));
        return list;
    }
}
