package io.devpl.fxui.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 用于资源加载
 */
public class ResourceLoader {

    /**
     * 加载类路径下的资源作为URL
     *
     * @param name 不要以/开头
     * @return 文件URL
     */
    public static URL load(String name) {
        ClassPathResource resource = new ClassPathResource(name);
        try {
            Path path = Paths.get(resource.getFile().toURI());
            return path.toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        String classpath = System.getProperty("java.class.path");

        String[] split = classpath.split(";");

        for (String s : split) {
            System.out.println(s
            );
        }


        ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        URL resource = classLoader.getResource("layout/connection_manage.fxml");


        System.out.println(resource);


    }

    /**
     * 加载资源为URL
     *
     * @param clazz 以Class的包名作为根路径
     * @param name  相对路径名称
     * @return URL
     */
    public static URL load(Class<?> clazz, String name) {
        if (clazz == null) {
            return load(name);
        }
        String packageName = clazz.getPackage().getName();
        String directoryName = packageName.replace(".", "/");

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("the path is empty!");
        }
        String finalFileName = directoryName + "/" + name;
        return load(finalFileName);
    }
}
