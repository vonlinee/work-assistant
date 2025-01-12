package io.devpl.fxui.tools.text;

import io.devpl.sdk.util.StringUtils;
import javafx.scene.control.TextArea;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextHandleMap {

    /**
     * key 为触发的Node，value为处理规则
     */
    private final Map<Object, TextHandleRule> handleRuleMap;

    public TextHandleMap() {
        this.handleRuleMap = new ConcurrentHashMap<>();
    }

    public void register(Object name, TextHandleRule handleRule) {
        handleRuleMap.putIfAbsent(name, handleRule);
    }

    /**
     * 文本处理
     * @param trigger 触发的控件
     * @param src     源字符串
     * @param dest    目标位置
     */
    public void handle(Object trigger, TextArea src, TextArea dest) {
        final String srcText = src.getText();
        if (trigger == null || !StringUtils.hasText(srcText)) {
            return;
        }
        final TextHandleRule handleRule = handleRuleMap.get(trigger);
        if (handleRule != null) {
            // 处理不为空的文本，直接覆盖目标的 TextArea
            final String result = handleRule.handle(srcText);
            // 有有意义的文本才进行显示
            if (StringUtils.hasText(result)) {
                dest.setText(result);
            }
        }
    }
}
