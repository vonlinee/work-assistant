package org.example.workassistant.ui.app;

import io.fxtras.FXControl;
import io.fxtras.FXUtils;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JvmTool extends BorderPane {
    ListView<String> list;
    TextArea editor;

    public JvmTool() {

        VBox center = new VBox();

        list = new ListView<>();

        editor = new TextArea("""
            CodeEditor.newInstance(LanguageMode.PLAIN_TEXT);
            """);

        setCenter(center);

        HBox buttonBox = new HBox();

        buttonBox.getChildren().addAll(FXUtils.newButton("解析", event -> {
            String text = editor.getText();
            if (FXUtils.isStringHasText(text)) {
                parseCommand(text);
            }
        }));

        center.getChildren().addAll(editor, list, buttonBox);


        JavaProcessTableBase table = new JavaProcessTableBase();

        center.getChildren().addAll(table);

        center.getChildren().add(FXControl.button("刷新Java进程", event -> table.refreshData()));
    }

    public static void parseCommand(String command) {
        // 使用空格作为分隔符来分割命令字符串
        String[] parts = split(command);
        // windows /path/to/java.exe
        if (parts.length > 0) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                parts[0] = parts[0].replace("\\", "/");
            }
            Path path = Path.of(parts[0]);
            if (path.getFileName().toString().equals("java.exe")) {
                // 移除"java"元素
                String[] argsArray = Arrays.copyOfRange(parts, 1, parts.length);
                // 处理剩下的部分，这里简单地打印出来

                for (int i = 0; i < argsArray.length; i++) {

                    String arg = argsArray[i];
                    if (arg.startsWith("\"") && arg.endsWith("\"")) {
                        arg = arg.substring(1, arg.length() - 2);
                    }
                    // 这里可以根据需要判断参数类型（如-cp是类路径，后面跟着的是路径）
                    if (arg.startsWith("-")) {
                        // 这是一个选项，处理它（例如，-cp后面跟着的是类路径）
                        System.out.println("Option: " + arg);
                        // 可能还需要处理选项的值（例如，-cp后面的路径）

                        if (arg.startsWith("-D")) {
                            // 环境变量
                        }

                        if (arg.startsWith("-classpath")) {

                        }
                    }
                }
            }
        } else {
            System.out.println("The command does not start with 'java'.");
        }
    }

    /**
     * 分割字符串
     *
     * @param str 字符串 需要保证“成对出现
     * @return 任意多个空格分割的字符串，使用"包裹的空格无效
     */
    public static String[] split(String str) {
        boolean flag = false;
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 碰到空格
            if (c == ' ') {
                if (sb.isEmpty()) {
                    // 空格，忽略掉
                    continue;
                }
                if (flag) {
                    // 有引号，忽略空格
                    sb.append(c);
                } else {
                    // 保存结果
                    res.add(sb.toString());
                    // 清空
                    sb.setLength(0);
                }
            } else if (c == '"') {
                if (!flag) {
                    flag = true;
                } else {
                    // 保存结果
                    res.add(sb.toString());
                    // 清空
                    sb.setLength(0);
                    flag = false;
                }
            } else {
                sb.append(c);
            }
        }

        if (!sb.isEmpty()) {
            res.add(sb.toString());
        }

        return res.toArray(new String[0]);
    }


}
