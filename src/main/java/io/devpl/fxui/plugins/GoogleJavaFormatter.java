package io.devpl.fxui.plugins;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.config.Context;

/**
 * Google Java代码格式化
 */
public class GoogleJavaFormatter implements JavaFormatter {

    protected Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String getFormattedContent(CompilationUnit compilationUnit) {
        try {
            String formattedSource = new Formatter().formatSource("");
        } catch (FormatterException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
