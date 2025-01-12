package io.devpl.common.utils;

import java.io.File;
import java.util.Comparator;

/**
 * 文件比较器
 */
public class FileComparator implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
        // 首先比较是否是目录
        boolean isDir1 = f1.isDirectory();
        boolean isDir2 = f2.isDirectory();
        if (isDir1 && !isDir2) { // 如果f1是目录而f2不是，f1排在前面
            return -1;
        } else if (!isDir1 && isDir2) { // 如果f2是目录而f1不是，f2排在前面
            return 1;
        } else { // 如果两者都是目录或都是文件，则按名称排序
            return f1.getName().compareTo(f2.getName());
        }
    }
}
