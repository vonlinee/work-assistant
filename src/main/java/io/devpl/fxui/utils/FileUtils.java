package io.devpl.fxui.utils;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    private static final File[] EMPTY_FILES = new File[0];

    public static File[] listAllFiles(File dir) {
        if (!isDirectory(dir)) {
            return EMPTY_FILES;
        }
        final File[] files = dir.listFiles();
        if (files == null) {
            return EMPTY_FILES;
        }
        return files;
    }

    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    public static List<File> filter(File[] files, FileFilter fileFilter) {
        final List<File> fileList = new ArrayList<>(files.length);
        for (File file : files) {
            if (fileFilter != null && fileFilter.accept(file)) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * Converts an array of file extensions to suffixes for use
     * with IOFileFilters.
     *
     * @param extensions an array of extensions. Format: {"java", "xml"}
     * @return an array of suffixes. Format: {".java", ".xml"}
     */
    private static String[] toSuffixes(final String... extensions) {
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    public static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/') p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/")) p = "/" + p;
        if (!p.endsWith("/") && isDirectory) p = p + "/";
        return p;
    }

    /**
     * Makes a directory, including any necessary but nonexistent parent
     * directories. If a file already exists with specified name but it is
     * not a directory then an IOException is thrown.
     * If the directory cannot be created (or the file already exists but is not a directory)
     * then an IOException is thrown.
     *
     * @param directory directory to create, must not be {@code null}.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws IOException       if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     */
    public static File forceMkdir(final File directory) throws IOException {
        return mkdirs(directory);
    }

    public static boolean forceMkdir(File... dirs) throws IOException {
        for (File dir : dirs) {
            forceMkdir(dir);
        }
        return true;
    }

    public static boolean forceMkdir(Path... dirs) throws IOException {
        for (Path dir : dirs) {
            forceMkdir(dir.toFile());
        }
        return true;
    }

    public static boolean forceMkdir(List<Path> dirs) throws IOException {
        for (Path dir : dirs) {
            forceMkdir(dir.toFile());
        }
        return true;
    }

    /**
     * Calls {@link File#mkdirs()} and throws an exception on failure.
     *
     * @param directory the receiver for {@code mkdirs()}, may be null.
     * @return the given file, may be null.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws IOException       if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see File#mkdirs()
     */
    private static File mkdirs(final File directory) throws IOException {
        if ((directory != null) && (!directory.mkdirs() && !directory.isDirectory())) {
            throw new IOException("Cannot create directory '" + directory + "'.");
        }
        return directory;
    }

    private static File[] listFiles(final File directory, final FileFilter fileFilter) throws IOException {
        requireDirectoryExists(directory, "directory");
        final File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
        if (files == null) {
            // null if the directory does not denote a directory, or if an I/O error occurs.
            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
        }
        return files;
    }

    /**
     * Requires that the given {@code File} exists and is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name      The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectoryExists(final File directory, final String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link IllegalArgumentException} if it doesn't.
     *
     * @param file          The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist.
     */
    private static File requireExists(final File file, final String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    /**
     * Requires that the given {@code File} is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name      The parameter name to use in the exception message in case of null input or if the file is not a directory.
     * @return the given directory.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectory(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    public static void open(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
