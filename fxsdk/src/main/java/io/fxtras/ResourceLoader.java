package io.fxtras;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;

/**
 * 用于资源加载
 */
public interface ResourceLoader {

    URL getResourceAsUrl(String resource);

    /**
     * 加载类路径下的资源作为URL
     *
     * @param name 不要以/开头
     * @return 文件URL
     */
    static URL load(String name) {
        ClassPathResource resource = new ClassPathResource(name);
        try {
            URL url = null;
            if (resource.exists()) {
                url = resource.getURL();
            }
            if (url == null) {
                url = ResourceLoader.class.getResource("/" + name);
            }
            return url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载资源为URL
     *
     * @param clazz 以Class的包名作为根路径
     * @param name  相对路径名称
     * @return URL
     */
    static URL load(Class<?> clazz, String name) {
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
