package io.devpl.fxui.utils.office;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import io.devpl.fxui.controller.fields.InfoSchemaColumn;
import io.devpl.fxui.utils.DBUtils;
import io.devpl.sdk.util.CollectionUtils;
import io.devpl.sdk.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * POI复制word、table、Paragraph
 * <a href="http://www.daimeng.fun/index/detail/220/1">...</a>
 */
public class PoiUtils2 {

    public static final String path = "C:\\Users\\Von\\Desktop\\";

    static final Logger log = LoggerFactory.getLogger(PoiUtils2.class);

    static final String sql = "SELECT * FROM information_schema.`TABLES` WHERE TABLE_SCHEMA = 'lgdb_campus_intelligent_portrait'\n" + "AND TABLE_NAME = '%s'";

    /**
     * 获取所有表字段信息
     * @return
     * @throws SQLException
     */
    public static TableSchemeInfo getTableInfo(String tableName) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lgdb_campus_intelligent_portrait?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "123456");
        Connection connection = DBUtils.getConnection(url, props);
        final Statement statement = connection.createStatement();
        final ResultSet rs = statement.executeQuery(String.format(sql, tableName));
        final DataClassRowMapper<InfoSchemaTable> rowMapper = new DataClassRowMapper<>(InfoSchemaTable.class);
        List<InfoSchemaTable> tmdList = new ArrayList<>();
        int rowIndex = 0;
        while (rs.next()) {
            tmdList.add(rowMapper.mapRow(rs, rowIndex++));
        }
        // 新建文档
        final DataClassRowMapper<InfoSchemaColumn> rowMapperColumn = new DataClassRowMapper<>(InfoSchemaColumn.class);
        final TableSchemeInfo tableInfo = new TableSchemeInfo();
        for (InfoSchemaTable infoSchemaTable : tmdList) {
            String tabelName = infoSchemaTable.getTableName();
            final String tableComment = infoSchemaTable.getTableComment();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM information_schema.`COLUMNS` where TABLE_NAME = '" + tabelName + "'");
            List<InfoSchemaColumn> columnList = new ArrayList<>();
            int rowIndex1 = 0;
            while (resultSet.next()) {
                columnList.add(rowMapperColumn.mapRow(resultSet, rowIndex1++));
            }
            tableInfo.setTable(infoSchemaTable);
            tableInfo.setColumnList(columnList);
        }
        return tableInfo;
    }

    @Getter
@Setter
    static class TableSchemeInfo {
        private InfoSchemaTable table;
        private List<InfoSchemaColumn> columnList;
    }

    public static void main(String[] args) throws IOException, SQLException {

        // XWPFDocument doc = new XWPFDocument(Files.newInputStream(Paths.get("C:\\Users\\Von\\Desktop\\表字段定义模板.docx")));

        List<String> targetTableNames = new ArrayList<>();
        targetTableNames.add("base_custom_view");

        try (XWPFDocument document = new XWPFDocument()) {
            for (String targetTableName : targetTableNames) {
                System.out.println("开始写入" + targetTableName);
                writeOneTable(document, targetTableName);
            }
            document.write(new FileOutputStream(new File(path, "表字段描述.doc")));
        }
        // doc.write(new FileOutputStream(path + "1.doc"));
    }

    public static void writeOneTable(XWPFDocument document, String targetTableName) throws SQLException {
        TableDescription tableDescription = getTableDescription(targetTableName);
        // // 添加表格
        // for (int i = 0; i < tableSize; i++) {
        //     final XWPFTable newTable = doc.createTable();
        //     cloneTable(newTable, tables.get(i));
        // }

        final XWPFParagraph paragraph = document.createParagraph();
        final XWPFRun run = paragraph.createRun();
        run.setText(tableDescription.getTableComment());
        final XWPFRun run1 = paragraph.createRun();
        run1.setText(tableDescription.getTableName());

        final XWPFTable table = document.createTable();
        // 第一行
        final List<Row> rowList = tableDescription.getRowList();
        final Row row1 = rowList.get(0);
        final XWPFTableRow row = table.getRows()
                .get(0);
        final XWPFTableCell cell = row.getCell(0);
        cell.setText(row1.getColumnName());
        final XWPFTableCell cell1 = row.createCell();
        cell1.setText(row1.getColumnType());
        final XWPFTableCell cell2 = row.createCell();
        cell2.setText(row1.getColumnComment());

        // 剩下的行

        for (int i = 1; i < rowList.size(); i++) {
            final XWPFTableRow nextRow = table.createRow();
            final List<XWPFTableCell> tableCells = nextRow.getTableCells();

            final Row row2 = rowList.get(i);

            final XWPFTableCell cell3 = tableCells.get(0);
            final XWPFTableCell cell4 = tableCells.get(1);
            final XWPFTableCell cell5 = tableCells.get(2);
            cell3.setText(row2.getColumnName());
            cell4.setText(row2.getColumnType());
            cell5.setText(row2.getColumnComment());
        }

        final XWPFParagraph empty1 = document.createParagraph();
        final XWPFParagraph empty2 = document.createParagraph();
    }

    @Getter
@Setter
    static class TableDescription {

        String tableName;
        String tableComment;
        String tableDescription;
        List<Row> rowList;
    }

    public static TableDescription getTableDescription(String targetTableName) throws SQLException {
        final TableSchemeInfo tableInfo = getTableInfo(targetTableName);

        TableDescription tableDescription = new TableDescription();
        // 所有列
        List<Row> rows = new ArrayList<>();
        tableDescription.setTableName(tableInfo.getTable()
                .getTableName());
        tableDescription.setTableComment(tableInfo.getTable()
                .getTableComment());
        tableDescription.setRowList(rows);
        final InfoSchemaTable table = tableInfo.getTable();
        final List<InfoSchemaColumn> columnList = tableInfo.getColumnList();

        for (InfoSchemaColumn infoSchemaColumn : columnList) {
            final String columnName = infoSchemaColumn.getColumnName();
            if (columnName.contains("is_delete")) {
                continue;
            }
            if (columnName.contains("create_time")) {
                continue;
            }
            if (columnName.contains("update_time")) {
                continue;
            }
            final String columnType = infoSchemaColumn.getColumnType();
            String columnDefault = infoSchemaColumn.getColumnDefault();
            final String columnComment = infoSchemaColumn.getColumnComment();

            final String isNullable = infoSchemaColumn.getIsNullable();
            if (columnDefault == null || columnDefault.length() == 0) {
                columnDefault = columnType;
            }
            if ("NO".equals(isNullable)) {
                columnDefault += " not null";
            }
            if (columnName.endsWith("id") || columnName.equals("school_code")) {
                columnDefault += " 主键";
            }
            final Row row = new Row(columnName, columnDefault, columnComment);
            row.setTableName(table.getTableName());
            row.setTableComment(table.getTableComment());
            rows.add(row);
        }
        return tableDescription;
    }

    public void renderTemplate(XWPFParagraph paragraph) {
        final String text = paragraph.getText();
    }

    @Getter
@Setter
    @ExcelTarget("数据表定义")
    static class Row {

        @Excel(name = "表名")
        String tableName;
        @Excel(name = "表注释")
        String tableComment;
        @Excel(name = "列名")
        String columnName;
        @Excel(name = "类型")
        String columnType;
        @Excel(name = "注释")
        String columnComment;

        public Row(String columnName, String columnType, String columnComment) {
            this.columnName = columnName;
            this.columnType = columnType;
            this.columnComment = columnComment;
        }
    }

    /**
     * 替换段落中的指定文字
     */
    public static void replaceXWPFParagraphWithTableInfo(XWPFParagraph paragraph, Map<String, Object> map) {
        List<XWPFRun> xwpfRuns = paragraph.getRuns();
        for (XWPFRun xwpfRun : xwpfRuns) {
            final int textPosition = xwpfRun.getTextPosition();
            String text = xwpfRun.getText(textPosition);
            if (text.startsWith("<")) {
                log.info("模板:" + text);
                final ST st = new ST(text);
                st.add("tableComment", map.get("tableComment"));
                st.add("tableName", map.get("tableName"));
                xwpfRun.setText(st.render(), 0);
            }
        }
    }

    /**
     * 复制一个XWPFTable
     * @param target
     * @param source
     */
    public static void cloneTable(XWPFTable target, XWPFTable source) {
        target.getCTTbl()
                .set(source.getCTTbl());
    }

    public static void cloneParagraph(XWPFParagraph target, XWPFParagraph source) {
        // target.getCTP().set(source.getCTP());
        final List<XWPFRun> xwpfRuns = source.getRuns();
        // 复制文本对象
        for (XWPFRun xwpfRun : xwpfRuns) {
            final XWPFRun run = target.createRun();
            run.getCTR()
                    .set(xwpfRun.getCTR());
        }
    }

    /**
     * 复制单元格和样式，有点问题
     * @param targetRow 要复制的行
     * @param sourceRow 被复制的行
     */
    public static void cloneTableRow(XWPFTableRow targetRow, XWPFTableRow sourceRow) {
        targetRow.getCtRow()
                .setTrPr(sourceRow.getCtRow()
                        .getTrPr());
        List<XWPFTableCell> tableCells = sourceRow.getTableCells();
        if (CollectionUtils.isEmpty(tableCells)) {
            return;
        }
        // 复制单元格
        for (XWPFTableCell sourceCell : tableCells) {
            XWPFTableCell newCell = targetRow.addNewTableCell();
            newCell.getCTTc()
                    .setTcPr(sourceCell.getCTTc()
                            .getTcPr());
            List<XWPFParagraph> sourceParagraphs = sourceCell.getParagraphs();
            if (CollectionUtils.isEmpty(sourceParagraphs)) {
                continue;
            }
            XWPFParagraph sourceParagraph = sourceParagraphs.get(0);
            List<XWPFParagraph> targetParagraphs = newCell.getParagraphs();
            XWPFParagraph targetParagraph = CollectionUtils.isEmpty(targetParagraphs) ? newCell.addParagraph() : (XWPFParagraph) targetParagraphs.get(0);
            targetParagraph.getCTP()
                    .setPPr(sourceParagraph.getCTP()
                            .getPPr());
            XWPFRun targetRun = targetParagraph.getRuns()
                    .isEmpty() ? targetParagraph.createRun() : targetParagraph.getRuns()
                    .get(0);
            List<XWPFRun> sourceRunList = sourceParagraph.getRuns();
            if (!CollectionUtils.isEmpty(sourceRunList)) {
                XWPFRun sourceRun = sourceRunList.get(0);
                targetRun.setFontFamily(sourceRun.getFontFamily());  // 字体名称
                targetRun.setFontSize(sourceRun.getFontSize()); // 字体大小
                targetRun.setColor(sourceRun.getColor()); // 字体颜色
                targetRun.setBold(sourceRun.isBold()); // 字体加粗
                targetRun.setItalic(sourceRun.isItalic()); // 字体倾斜
            }
        }
    }

    public static void fillEmptyTableRows(XWPFTable table, int targetRowCount) {
        final int originRowCount = table.getRows()
                .size();
        int rowCountNeedToFill = targetRowCount - originRowCount;
        if (rowCountNeedToFill <= 0) return;
        final XWPFTableRow oldRow = table.getRows()
                .get(0);
        fillEmptyTableRows(table, 0, rowCountNeedToFill, originRowCount);
    }

    /**
     * des:表末尾添加行(表，要复制样式的行，添加行数，插入的行下标索引)
     * @param table
     * @param source
     * @param rows
     */
    public static void fillEmptyTableRows(XWPFTable table, int source, int rows, int insertRowIndex) {
        try {
            // 获取表格的总行数
            int index = table.getNumberOfRows();
            // 循环添加行和和单元格
            for (int i = 1; i <= rows; i++) {
                // 获取要复制样式的行
                XWPFTableRow sourceRow = table.getRow(source);
                // 添加新行
                XWPFTableRow targetRow = table.insertNewTableRow(insertRowIndex++);
                // 复制行的样式给新行
                targetRow.getCtRow()
                        .setTrPr(sourceRow.getCtRow()
                                .getTrPr());
                // 获取要复制样式的行的单元格
                List<XWPFTableCell> sourceCells = sourceRow.getTableCells();
                // 循环复制单元格
                for (XWPFTableCell sourceCell : sourceCells) {
                    // 添加新列
                    XWPFTableCell newCell = targetRow.addNewTableCell();
                    // 复制单元格的样式给新单元格
                    newCell.getCTTc()
                            .setTcPr(sourceCell.getCTTc()
                                    .getTcPr());
                    // 设置垂直居中
                    newCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);// 垂直居中
                    // 复制单元格的居中方式给新单元格
                    CTPPr pPr = sourceCell.getCTTc()
                            .getPList()
                            .get(0)
                            .getPPr();
                    if (pPr != null && pPr.getJc() != null && pPr.getJc()
                            .getVal() != null) {
                        CTTc cttc = newCell.getCTTc();
                        CTP ctp = cttc.getPList()
                                .get(0);
                        CTPPr ctppr = ctp.getPPr();
                        if (ctppr == null) {
                            ctppr = ctp.addNewPPr();
                        }
                        CTJc ctjc = ctppr.getJc();
                        if (ctjc == null) {
                            ctjc = ctppr.addNewJc();
                        }
                        ctjc.setVal(pPr.getJc()
                                .getVal()); // 水平居中
                    }
                    // 得到复制单元格的段落
                    List<XWPFParagraph> sourceParagraphs = sourceCell.getParagraphs();
                    if (!StringUtils.isEmpty(sourceCell.getText())) {
                        // TODO 不复制段落
                        continue;
                    }
                    // 拿到第一段
                    XWPFParagraph sourceParagraph = sourceParagraphs.get(0);
                    // 得到新单元格的段落
                    List<XWPFParagraph> targetParagraphs = newCell.getParagraphs();
                    // 判断新单元格是否为空
                    if (StringUtils.isEmpty(newCell.getText())) {
                        // 添加新的段落
                        XWPFParagraph ph = newCell.addParagraph();
                        // 复制段落样式给新段落
                        ph.getCTP()
                                .setPPr(sourceParagraph.getCTP()
                                        .getPPr());
                        // 得到文本对象
                        XWPFRun run = ph.getRuns()
                                .isEmpty() ? ph.createRun() : ph.getRuns()
                                .get(0);
                        // 复制文本样式
                        run.setFontFamily(sourceParagraph.getRuns()
                                .get(0)
                                .getFontFamily());
                    } else {
                        XWPFParagraph ph = targetParagraphs.get(0);
                        ph.getCTP()
                                .setPPr(sourceParagraph.getCTP()
                                        .getPPr());
                        XWPFRun run = ph.getRuns()
                                .isEmpty() ? ph.createRun() : ph.getRuns()
                                .get(0);
                        run.setFontFamily(sourceParagraph.getRuns()
                                .get(0)
                                .getFontFamily());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述:复制单元格，从source到target
     * @param target
     * @param source
     * @param index
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void copyTableCell(XWPFTableCell target, XWPFTableCell source, Integer index) {
// 列属性
        if (source.getCTTc() != null) {
            target.getCTTc()
                    .setTcPr(source.getCTTc()
                            .getTcPr());
        }
// 删除段落
        for (int pos = 0; pos < target.getParagraphs()
                .size(); pos++) {
            target.removeParagraph(pos);
        }
// 添加段落
        for (XWPFParagraph sp : source.getParagraphs()) {
            XWPFParagraph targetP = target.addParagraph();
            copyParagraph(targetP, sp, index);
        }
    }

    /**
     * 功能描述:复制段落，从source到target
     * @param target
     * @param source
     * @param index
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void copyParagraph(XWPFParagraph target, XWPFParagraph source, Integer index) {
        // 设置段落样式
        target.getCTP()
                .setPPr(source.getCTP()
                        .getPPr());
        // 移除所有的run
        for (int pos = target.getRuns()
                .size() - 1; pos >= 0; pos--) {
            target.removeRun(pos);
        }
        // copy 新的run
        for (XWPFRun s : source.getRuns()) {
            XWPFRun targetrun = target.createRun();
            copyRun(targetrun, s, index);
        }
    }

    /**
     * 功能描述:复制RUN，从source到target
     * @param target
     * @param source
     * @param index
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void copyRun(XWPFRun target, XWPFRun source, Integer index) {
        // 设置run属性
        target.getCTR()
                .setRPr(source.getCTR()
                        .getRPr());
        // 设置文本
        String tail = "";
        if (index != null) {
            tail = index.toString();
        }
        target.setText(source.text()
                .replace("}", "") + tail + "}");
    }

    /**
     * 功能描述:复制行，从source到target
     * @param target
     * @param source
     * @param index
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void copyTableRow(XWPFTableRow target, XWPFTableRow source, Integer index) {
        // 复制样式
        if (source.getCtRow() != null) {
            target.getCtRow()
                    .setTrPr(source.getCtRow()
                            .getTrPr());
        }
        // 复制单元格
        for (int i = 0; i < source.getTableCells()
                .size(); i++) {
            XWPFTableCell cell1 = target.getCell(i);
            XWPFTableCell cell2 = source.getCell(i);
            if (cell1 == null) {
                cell1 = target.addNewTableCell();
            }
            copyTableCell(cell1, cell2, index);
        }
    }

    /**
     * 功能描述:为表格插入数据，行数不够添加新行
     * @param table
     * @param tableList
     * @param daList
     * @param type
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static void insertTable(XWPFTable table, List<String> tableList, List<String[]> daList, Integer type) {
        if (2 == type) {
// 创建行和创建需要的列
            for (int i = 1; i < daList.size(); i++) {
// 添加一个新行
                XWPFTableRow row = table.insertNewTableRow(1);
                for (int k = 0; k < daList.get(0).length; k++) {
// 根据String数组第一条数据的长度动态创建列
                    row.createCell();
                }
            }
// 创建行,根据需要插入的数据添加新行，不处理表头
            for (int i = 0; i < daList.size(); i++) {
                List<XWPFTableCell> cells = table.getRow(i + 1)
                        .getTableCells();
                for (int j = 0; j < cells.size(); j++) {
                    XWPFTableCell cell02 = cells.get(j);
                    cell02.setText(daList.get(i)[j]);
                }
            }
        } else if (4 == type) {
// 插入表头下面第一行的数据
            for (int i = 0; i < tableList.size(); i++) {
                XWPFTableRow row = table.createRow();
                List<XWPFTableCell> cells = row.getTableCells();
                cells.get(0)
                        .setText(tableList.get(i));
            }
        }
    }

    /**
     * 功能描述:判断文本中时候包含$
     * @param text
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static boolean checkText(String text) {
        boolean check = false;
        if (text.indexOf("$") != -1) {
            check = true;
        }
        return check;
    }

    /**
     * 功能描述:匹配传入信息集合与模板
     * @param value
     * @param textMap
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Object changeValue(String value, Map<String, Object> textMap) {
        Set<Map.Entry<String, Object>> textSets = textMap.entrySet();
        Object valu = "";
        for (Map.Entry<String, Object> textSet : textSets) {
// 匹配模板与替换值 格式${key}
            String key = textSet.getKey();
            if (value.contains(key)) {
                valu = textSet.getValue();
            }
        }
        return valu;
    }
}
