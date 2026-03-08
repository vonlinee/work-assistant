package org.assistant.tools.datasource;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports a list of DataSourceConfig objects to Markdown or Excel.
 */
public class DataSourceExporter {

    private static final String[] HEADERS = {
            "Name", "Type", "Host", "Port", "Database", "Username", "JDBC URL", "Remark"
    };

    /**
     * Exports to a Markdown file as a formatted table.
     */
    public void exportMarkdown(List<DataSourceConfig> configs, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("# Datasource Connections");
            pw.println();
            pw.printf("| %-20s | %-12s | %-16s | %-6s | %-16s | %-12s | %-50s | %-20s |%n",
                    (Object[]) HEADERS);
            pw.printf("| %-20s | %-12s | %-16s | %-6s | %-16s | %-12s | %-50s | %-20s |%n",
                    "--------------------", "------------", "----------------", "------",
                    "----------------", "------------",
                    "--------------------------------------------------", "--------------------");
            for (DataSourceConfig c : configs) {
                pw.printf("| %-20s | %-12s | %-16s | %-6s | %-16s | %-12s | %-50s | %-20s |%n",
                        nullSafe(c.getName()),
                        nullSafe(c.getType()),
                        nullSafe(c.getHost()),
                        nullSafe(c.getPort()),
                        nullSafe(c.getDatabase()),
                        nullSafe(c.getUsername()),
                        nullSafe(c.getEffectiveJdbcUrl()),
                        nullSafe(c.getRemark()));
            }
        }
    }

    /**
     * Exports to an Excel (.xlsx) file using EasyExcel.
     */
    public void exportExcel(List<DataSourceConfig> configs, File file) {
        List<List<Object>> dataList = new ArrayList<>();
        // header
        List<Object> header = new ArrayList<>();
        for (String h : HEADERS)
            header.add(h);
        dataList.add(header);
        // rows
        for (DataSourceConfig c : configs) {
            List<Object> row = new ArrayList<>();
            row.add(nullSafe(c.getName()));
            row.add(nullSafe(c.getType()));
            row.add(nullSafe(c.getHost()));
            row.add(nullSafe(c.getPort()));
            row.add(nullSafe(c.getDatabase()));
            row.add(nullSafe(c.getUsername()));
            row.add(nullSafe(c.getEffectiveJdbcUrl()));
            row.add(nullSafe(c.getRemark()));
            dataList.add(row);
        }
        EasyExcel.write(file)
                .sheet("Datasources")
                .doWrite(dataList);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
