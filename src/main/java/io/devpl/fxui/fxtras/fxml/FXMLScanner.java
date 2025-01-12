package io.devpl.fxui.fxtras.fxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FXMLScanner {

    private static final String parentPath = "static/fxml";

    private static int i = 0;

    public static Map<String, String> scan() {
        URL classpathRoot = Thread.currentThread().getContextClassLoader().getResource(parentPath);
        if (classpathRoot == null) {
            return Collections.emptyMap();
        }
        try {
            final File rootDirectory = new File(classpathRoot.toURI());
            final String absoluteRootPath = rootDirectory.getAbsolutePath().replace("\\", "/");
            i = absoluteRootPath.indexOf(parentPath);
            Map<String, String> map = new LinkedHashMap<>();
            doScan(rootDirectory.getAbsolutePath(), map);
            return map;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO:递归扫描指定文件夹下面的指定文件
     */
    private static void doScan(String folderPath, final Map<String, String> result) throws FileNotFoundException {
        File directory = new File(folderPath);
        if (!directory.isDirectory()) {
            return;
        }
        if (directory.isDirectory()) {
            // 不应该过滤
            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (File file : files) {
                // 如果当前是文件夹，进入递归扫描文件夹
                if (file.isDirectory()) {
                    // 递归扫描下面的文件夹
                    doScan(file.getAbsolutePath(), result);
                } else {  // 非文件夹
                    //
                    final String absolutePath = file.getAbsolutePath().intern();
                    try {
                        result.put(absolutePath.substring(i).replace("\\", "/"), file.toURI().toURL().toExternalForm());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
