package io.devpl.fxui.editor;

import javafx.scene.Parent;

import java.util.List;
import java.util.function.Function;

/**
 * 代码编辑器，支持语法高亮
 * <a href="https://tun6.com/projects/code_mirror/api/#api_doc">...</a>
 */
public interface CodeEditor {

    static CodeEditor newInstance(LanguageMode mode) {
        return CodeMirrorEditor.newInstance(mode);
    }

    /**
     * 获取编辑器文本
     *
     * @return 编辑器文本
     */
    String getText();

    /**
     * @param text 文本
     */
    default void setText(String text) {
        setText(text, false);
    }

    /**
     * @param newContent 文本
     * @param markClean  是否清除之前的文本
     */
    void setText(String newContent, boolean markClean);

    /**
     * doc.isClean(?generation: integer) → boolean
     * 参数为空时，从初始化或最后一次调用 markClean 函数到现在，正文是否有修改。
     * 参数不为空时，从调用 changeGeneration 函数到现在，正文是否有修改。
     *
     * @return 返回正文是否是 "clean" 的。
     */
    boolean isClean();

    /**
     * 标记正文是 "clean" 的，该状态会一直保持到下次编辑时，可用来判断正文是否需要保存。
     * 该函数与 changeGeneration 不同，后者支持多个子系统在不互相干扰的情况下跟踪多个 "clean" 状态。
     */
    void markClean();

    Position getCursorPosition();

    void setCursorPosition(Position position);

    boolean isEditorInitialized();

    /**
     * 初始化回调
     *
     * @param runAfterLoading WebView加载完成之后调用
     */
    void init(Runnable... runAfterLoading);

    /**
     * 编辑器对应的节点 Node
     *
     * @return 编辑器对应的节点 Node
     */
    Parent getView();

    /**
     * 编辑器是否只读
     *
     * @return 编辑器是否只读
     */
    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

    String getMode();

    void setMode(String mode);

    void setMode(LanguageMode mode);

    String getTheme();

    void setTheme(Theme theme, String... cssFile);

    /**
     * 回调
     *
     * @param runnable 执行逻辑
     */
    void runWhenReady(Runnable runnable);

    void setAutoCompleteFunction(Function<String, List<String>> autoCompleteFunction);

    Function<String, List<String>> getAutoCompleteFunction();
}
