package io.devpl.fxui.view;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * 替代JavaFX自带的TextArea
 */
public class RichTextArea extends CodeArea {

    public RichTextArea() {
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
    }

    /**
     * 完全替代所有文本
     *
     * @param chars 要替代的文本
     */
    public final void setText(CharSequence chars) {
        super.clear();
        super.appendText(chars.toString());
    }
}
