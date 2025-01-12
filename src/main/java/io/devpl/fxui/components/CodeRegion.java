package io.devpl.fxui.components;

import io.devpl.fxui.utils.FXUtils;
import javafx.scene.layout.Region;
import org.fxmisc.richtext.CodeArea;

/**
 * 代码区域
 * 封装 ricthtextfx 的CodeArea
 */
public final class CodeRegion extends Region {

    CodeArea codeArea;

    public CodeRegion() {
        codeArea = new CodeArea();
        codeArea.setStyle("-fx-font-family: consolas; -fx-font-size: 12pt;");
        codeArea.setWrapText(false);
        getChildren().add(codeArea);
    }

    @Override
    protected void layoutChildren() {
        FXUtils.layoutInRegion(this, codeArea);
    }
}
