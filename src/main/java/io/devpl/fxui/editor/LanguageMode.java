package io.devpl.fxui.editor;

/**
 * <a href="https://codemirror.net/5/mode/">Code Mirror Language Mode</a>
 */
public enum LanguageMode {

    VELOCITY("text/velocity"),
    JSON("application/json"),
    JAVASCRIPT("text/javascript"),
    SQL("text/x-sql"),
    XML("application/xml"),
    JAVA("text/x-java"),
    CSS("text/css"),
    HTML("text/html"),
    YAML("text/x-yaml"),
    PLAIN_TEXT("text/plain"),
    UNKNOWN(null);

    private final String modeName;

    LanguageMode(String mode) {
        this.modeName = mode;
    }

    public String getModeName() {
        return this.modeName;
    }
}
