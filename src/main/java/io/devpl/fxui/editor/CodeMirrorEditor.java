package io.devpl.fxui.editor;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * CodeMirror Editor 6
 */
public class CodeMirrorEditor implements CodeEditor {

    private final WebView webView;
    private final AtomicBoolean isEditorInitialized = new AtomicBoolean(false);
    private final Queue<Runnable> queue = new LinkedBlockingQueue<>();

    /**
     * 此变量不能是static
     */
    private ScheduledExecutorService executor;

    static final String CODE_MIRROR_INDEX_HTML = "codemirror/index.html";

    /**
     * 自动补全文本
     */
    private Function<String, List<String>> autoCompleteFunction = s -> new ArrayList<>();

    public CodeMirrorEditor() {
        webView = new WebView();
    }

    public static CodeMirrorEditor newInstance(LanguageMode languageMode) {
        CodeMirrorEditor codeEditor = new CodeMirrorEditor();
        codeEditor.init(() -> codeEditor.setMode(languageMode),
            () -> codeEditor.setTheme(Theme.XQ_LIGHT));
        return codeEditor;
    }

    @Override
    public void init(Runnable... runAfterLoading) {
        try {
            URL resource = getClass().getClassLoader().getResource(CODE_MIRROR_INDEX_HTML);
            if (resource == null) {
                throw new RuntimeException("cannot find code mirror index.html");
            }
            queue.addAll(Arrays.asList(runAfterLoading));
            webView.getEngine().load(resource.toExternalForm());
            webView.getEngine().setOnError(event -> {
                throw new RuntimeException(event.getException());
            });
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(new Init(), 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public class Init implements Runnable {
        private final String command = "init();";

        @Override
        public void run() {
            Platform.runLater(() -> {
                try {
                    webView.getEngine().executeScript("CodeMirror;");
                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("injectedVariables", new AutoCompleteVariables());
                    webView.getEngine().executeScript(this.command);
                    executor.shutdown();
                    executor = null;
                    while (!queue.isEmpty()) {
                        Platform.runLater(queue.remove());
                    }
                    isEditorInitialized.set(true);
                } catch (Exception ex) {
                    //throw new RuntimeException(ex); // usually not needed
                }
            });
        }
    }

    /**
     * 自动提示
     */
    public class AutoCompleteVariables {
        public String match(String word) {
            List<String> resultList = autoCompleteFunction.apply(word);
            StringBuilder jsonResult = new StringBuilder("[");
            Iterator<String> iterator = resultList.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next();
                jsonResult.append(JsString.quote(value));
                if (iterator.hasNext()) {
                    jsonResult.append(",");
                }
            }
            jsonResult.append("]");
            return jsonResult.toString();
        }
    }

    @Override
    public String getText() {
        return (String) webView.getEngine().executeScript("codeMirror.getValue();");
    }

    @Override
    public void setText(String newContent, boolean markClean) {
        String escapedContent = JsString.quote(newContent);
        Platform.runLater(() -> {
            webView.getEngine().executeScript("codeMirror.setValue(" + escapedContent + ");");
            if (markClean) {
                this.markClean();
            }
        });
    }

    @Override
    public boolean isClean() {
        return (boolean) webView.getEngine().executeScript("codeMirror.isClean();");
    }

    @Override
    public void markClean() {
        webView.getEngine().executeScript("codeMirror.markClean();");
    }

    @Override
    public Position getCursorPosition() {
        String position = (String) webView.getEngine().executeScript("");
        return null;
    }

    @Override
    public void setCursorPosition(Position position) {
        Platform.runLater(() -> webView.getEngine().executeScript(""));
    }

    @Override
    public boolean isEditorInitialized() {
        return isEditorInitialized.get();
    }

    @Override
    public Parent getView() {
        return this.webView;
    }

    @Override
    public boolean isReadOnly() {
        return (Boolean) webView.getEngine().executeScript("codeMirror.getOption('readOnly');");
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript("codeMirror.setOption('readOnly', " + readOnly + ");");
        });
    }

    @Override
    public String getMode() {
        return (String) webView.getEngine().executeScript("codeMirror.getOption('mode');");
    }

    @Override
    public void setMode(String mode) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript("setMode(\"" + mode + "\");");
        });
    }

    @Override
    public void setMode(LanguageMode mode) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript("setMode(\"" + mode.getModeName() + "\");");
        });
    }

//    @Override
//    public void includeJSModules(String[] modules, Runnable runnable) {
//        //TODO test this
//        //fetchCodeEditorObject().call("importJSModules", modules, runnable);
//    }

    @Override
    public String getTheme() {
        return (String) webView.getEngine().executeScript("codeMirror.getOption('theme');");
    }

    @Override
    public void setTheme(Theme theme, String... cssFile) {
        String argument = "'" + theme.getName() + "'";
        if (cssFile != null && cssFile.length > 0) {
            StringBuilder cssFileArgument = new StringBuilder();
            for (String file : cssFile) {
                cssFileArgument.append(", '").append(file).append("'");
            }
            argument += cssFileArgument;
        }
        final String finalArg = argument;
        Platform.runLater(() -> webView.getEngine().executeScript("setTheme(" + finalArg + ");"));
    }

    @Override
    public void runWhenReady(Runnable runnable) {
        queue.add(runnable);
        handleQueue();
    }

    @Override
    public void setAutoCompleteFunction(Function<String, List<String>> autoCompleteFunction) {
        this.autoCompleteFunction = autoCompleteFunction;
    }

    @Override
    public Function<String, List<String>> getAutoCompleteFunction() {
        return this.autoCompleteFunction;
    }

    private void handleQueue() {
        if (isEditorInitialized.get()) {
            while (!queue.isEmpty()) {
                Runnable runnable = queue.remove();
                Platform.runLater(runnable);
            }
        }
    }
}
