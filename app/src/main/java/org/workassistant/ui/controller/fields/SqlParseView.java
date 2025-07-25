package org.workassistant.ui.controller.fields;

import org.workassistant.common.interfaces.FieldParser;
import org.workassistant.common.interfaces.impl.SqlFieldParser;
import org.workassistant.ui.editor.CodeEditor;
import org.workassistant.ui.editor.LanguageMode;
import javafx.scene.Node;

/**
 * 导入SQL，支持DDL和查询SQL
 */
public class SqlParseView extends FieldParseView {

    CodeEditor textArea;

    @Override
    String getName() {
        return "SQL";
    }

    @Override
    Node createRootNode() {
        textArea = CodeEditor.newInstance(LanguageMode.SQL);
        return textArea.getView();
    }

    @Override
    public String getParseableText() {
        return textArea.getText();
    }

    @Override
    public void fillSampleText() {
        textArea.setText(getSampleText(), true);
    }

    @Override
    protected FieldParser getFieldParser() {
        return new SqlFieldParser("mysql");
    }

    @Override
    public String getSampleText() {
        return """
            SELECT id, school_id, wisdom_exam_room, campus_monitor
            FROM external_system_param_config
            WHERE school_id = #{schoolId}
              """;
    }
}
