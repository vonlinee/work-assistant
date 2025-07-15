package io.fxtras;

import javafx.application.Application;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * 资源目录
 */
public abstract class JavaFXApplication extends Application {

    @Override
    public final void init() throws Exception {
        super.init();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ResourceLoader resourceLoader = getResourceLoader();
        if (resourceLoader != null) {
            Thread.currentThread().setContextClassLoader(new ClassLoader() {
                @Nullable
                @Override
                public URL getResource(String name) {
                    return resourceLoader.getResourceAsUrl(name);
                }
            });
        }
        Theme theme = getTheme();
        if (theme != null) {
            String userAgentStylesheet = theme.getUserAgentStylesheet();
            if (userAgentStylesheet != null) {
                if (resourceLoader != null) {
                    URL url = resourceLoader.getResourceAsUrl(userAgentStylesheet);
                    if (url != null) {
                        /**
                         * 设置ContextClassLoader不生效，先转换为绝对路径
                         * @see com.sun.javafx.css.StyleManager loadStylesheetUnPrivileged
                         */
                        Application.setUserAgentStylesheet(url.toExternalForm());
                    }
                } else {
                    Application.setUserAgentStylesheet(userAgentStylesheet);
                }
            }
        }
        try {
            this.onInit();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * @see Application#init()
     */
    protected void onInit() throws Exception {
    }

    protected void onStop() {
    }

    @Override
    public final void stop() throws Exception {
        super.stop();
        this.onStop();
    }

    @Nullable
    protected Theme getTheme() {
        return null;
    }

    @Nullable
    protected ResourceLoader getResourceLoader() {
        return null;
    }
}
