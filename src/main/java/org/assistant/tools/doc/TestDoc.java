package org.assistant.tools.doc;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class TestDoc {

	public static void main(String[] args) {
		XWPFDocument document = new XWPFDocument();

		int rowNum = 7;
		int colNum = 12;
		XWPFTable table = document.createTable(rowNum, colNum);

		table.setWidth("100%");
		table.setCellMargins(50, 20, 50, 20);
		table.setTableAlignment(TableRowAlign.CENTER);

		for (int i = 0; i < rowNum; i++) {
			XWPFTableRow row = table.getRow(i);
			if (i == 0) {
				row.getCell(0).setText("结束时间");
				row.getCell(4).setText("负责人");
				row.getCell(5).setText("许可人");
				row.getCell(6).setText("开始时间");
				row.getCell(10).setText("许可人");
				row.getCell(11).setText("负责人");

				row.getCell(0).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
				row.getCell(1).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
				row.getCell(2).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
				row.getCell(3).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
				row.getCell(6).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
				row.getCell(7).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
				row.getCell(8).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
				row.getCell(9).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);

				CTVMerge ctvMerge = CTVMerge.Factory.newInstance();
				ctvMerge.setVal(STMerge.RESTART);
				row.getCell(4).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(5).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(10).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(11).getCTTc().addNewTcPr().setVMerge(ctvMerge);

				for (int j = 0; j < colNum; j++) {
					XWPFTableCell cell = row.getCell(j);
					cell.getParagraphArray(0).setAlignment(ParagraphAlignment.CENTER);
					cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
					List<Integer> timeList = Arrays.asList(4, 5, 10, 11);
					if (timeList.contains(j)) {
						cell.setWidth("15%");
					} else {
						cell.setWidth("5%");
					}
				}

			} else if (i == 1) {

//                row.getCell(0).setWidthType(TableWidthType.AUTO);
				row.getCell(0).setText("月");
				row.getCell(1).setText("日");
				row.getCell(2).setText("时");
				row.getCell(3).setText("分");

				row.getCell(6).setText("月");
				row.getCell(7).setText("日");
				row.getCell(8).setText("时");
				row.getCell(9).setText("分");

				CTVMerge ctvMerge = CTVMerge.Factory.newInstance();
				ctvMerge.setVal(STMerge.CONTINUE);
				row.getCell(4).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(5).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(10).getCTTc().addNewTcPr().setVMerge(ctvMerge);
				row.getCell(11).getCTTc().addNewTcPr().setVMerge(ctvMerge);

				for (int j = 0; j < colNum; j++) {
					XWPFTableCell cell = row.getCell(j);
					cell.getParagraphArray(0).setAlignment(ParagraphAlignment.CENTER);
					cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
					List<Integer> timeList = Arrays.asList(4, 5, 10, 11);
					if (timeList.contains(j)) {
						cell.setWidth("15%");
					} else {
						cell.setWidth("5%");
					}
				}

			} else {

				for (int j = 0; j < colNum; j++) {
					XWPFTableCell cell = row.getCell(j);
					List<Integer> timeList = Arrays.asList(4, 5, 10, 11);
					if (timeList.contains(j)) {
						cell.setWidth("15%");
					} else {
						cell.setWidth("5%");
					}
//                    cell.setText("");
					cell.getParagraphArray(0).setAlignment(ParagraphAlignment.CENTER);
					cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
				}
			}
		}

		try {
			FileOutputStream out = new FileOutputStream("d:/test.docx");
			document.write(out);
			out.close();
			document.close();
		} catch (Exception e) {
			System.out.println("=====error=====");
			System.out.println(e.getMessage());
		}
	}
}