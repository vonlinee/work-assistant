package io.devpl.fxui.utils.office;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class OfficeUtils {

    /**
     * 根据Class定位本地的Class文件位置
     * @param clazz
     * @return
     */
    public static Path getProjectClassFilePathOfClass(Class<?> clazz) {
        try {
            return Path.of(Objects.requireNonNull(new File(Objects.requireNonNull(clazz.getResource(""))
                    .toURI()).getPath()), clazz.getSimpleName() + ".class");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * 根据Class定位项目源文件位置，Maven项目
     * @param clazz
     * @return
     */
    public static Path getProjectJavaFilePathOfClass(Class<?> clazz) {
        final URL resource = clazz.getResource("");
        if (resource == null) {
            return null;
        }
        try {
            final File file = new File(resource.toURI());
            return Path.of(file.getAbsolutePath()
                    .replace("\\", "/")
                    .replace("/target/classes", "/src/main/java"), clazz.getSimpleName() + ".java");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static File getDesktopDirectory() {
        String home = System.getProperty("user.home");
        return Path.of(home, "Desktop")
                .toFile();
    }

    public static File getDesktopDirectoryFile(String filename) {
        String home = System.getProperty("user.home");
        return Path.of(home, "Desktop", filename)
                .toFile();
    }

    public static Path getDesktopDirectoryPath(String filename) {
        String home = System.getProperty("user.home");
        return Path.of(home, "Desktop", filename);
    }
}
