package io.devpl.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class ProjectUtils {

    public static String toSimpleIdentifier(String src) {
        if (src == null) {
            return "null";
        }
        return src.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static ProjectModule parse(File entryFile) {
        if (entryFile.isDirectory()) {
            File[] files = entryFile.listFiles(f -> f.getName().equals("pom.xml") || f.getName().equals("build.grade"));
            if (files == null || files.length == 0) {
                return null;
            }
            entryFile = files[0];
        }

        if ("pom.xml".equals(entryFile.getName())) {
            return new MavenProjectAnalyser().analyse(entryFile);
        }
        return null;
    }

    /**
     * 列出该目录下的所有目录的路径，忽略文件夹
     * 例如: /aaa/bbb/ccc/ddd
     * 开始为 /aaa/bbb，则结果为 ccc/ddd, /ccc
     *
     * @param start           开始目录
     * @param ignoredDirNames 忽略的目录名称，忽略该目录及其所有子目录
     * @return 路径列表，字符串形式，相对路径列表
     */
    public static Set<String> listAllDirectoryPath(Path start, Set<String> ignoredDirNames) {
        try {
            Set<String> paths = new HashSet<>();
            Files.walkFileTree(start, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (ignoredDirNames.contains(String.valueOf(dir.getFileName()))) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    // 相对路径，相对于传入的参数start指定的路径
                    String relativePath = dir.toString();
                    if (!relativePath.isEmpty()) {
                        paths.add(relativePath);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return paths;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将路径转换为包名称
     *
     * @param filePath 比如 /org/springframework/core
     * @return org.springframework.core
     */
    public static String convertPathToPackage(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        if (filePath.startsWith("\\")) {
            filePath = filePath.substring(1);
        }
        filePath = filePath.replace("\\", "/");
        String[] segments = filePath.split("/");
        StringJoiner sb = new StringJoiner(".");
        for (String segment : segments) {
            sb.add(segment);
        }
        return sb.toString();
    }

    /**
     * 将包名称转换为路径
     *
     * @param packageName 比如 org.springframework.core
     * @return org/springframework/core
     */
    public static String convertPackageNameToPathname(String packageName) {
        return packageName.replace(".", File.separator);
    }

    public static String convertPathToPackage(Path filePath) {
        return convertPathToPackage(String.valueOf(filePath));
    }
}
