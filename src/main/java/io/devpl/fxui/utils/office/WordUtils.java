package io.devpl.fxui.utils.office;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.util.List;

public class WordUtils {

    public static XWPFParagraph copyParagraph(XWPFDocument wordDocument, int currentParagraphIndex) {
        List<XWPFParagraph> paragraphs = wordDocument.getParagraphs();
        // get whatever paragraph we're working with from the list
        XWPFParagraph paragraph = paragraphs.get(currentParagraphIndex);
        XmlCursor cursor = paragraph.getCTP()
                .newCursor();
        // inserts a blank paragraph before the original one
        paragraph.getDocument()
                .insertNewParagraph(cursor);
        // make a fully parsed copy of the old paragraph
        XWPFParagraph newParagraph = new XWPFParagraph((CTP) paragraph.getCTP()
                .copy(), wordDocument);
        // update the document with our now paragraph replacing the blank one with our new one and cause the document to reparse it.
        // Not sure why, but any modification to the new paragraph must be performed prior to setting it.
        wordDocument.setParagraph(newParagraph, currentParagraphIndex);
        return newParagraph;
    }

    public static void cloneParagraph(XWPFParagraph clone, XWPFParagraph source) {
        CTPPr pPr = clone.getCTP()
                .isSetPPr() ? clone.getCTP()
                .getPPr() : clone.getCTP()
                .addNewPPr();
        pPr.set(source.getCTP()
                .getPPr());
        for (XWPFRun r : source.getRuns()) {
            XWPFRun nr = clone.createRun();
            cloneRun(nr, r);
        }
    }

    public static void cloneRun(XWPFRun clone, XWPFRun source) {
        CTRPr rPr = clone.getCTR()
                .isSetRPr() ? clone.getCTR()
                .getRPr() : clone.getCTR()
                .addNewRPr();
        rPr.set(source.getCTR()
                .getRPr());
        clone.setText(source.getText(0));
    }
}
