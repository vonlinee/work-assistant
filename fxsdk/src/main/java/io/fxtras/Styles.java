package io.fxtras;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 通过字符串的形式构建 javafx css
 * javafx 默认样式文件所在目录 javafx-controls-11-win.jar  -->com.sun.javafx.scene.control.skin.caspian
 */
public class Styles {

    private final StringBuilder style;

    private Styles() {
        style = new StringBuilder();
    }

    public static Styles of() {
        return new Styles();
    }

    public Styles backgroundColor(Color color) {
        style.append("-fx-background-color: #").append(color.toString().substring(4));
        return this;
    }

    public String build() {
        return style.toString();
    }

    public static String color(Color color) {
        return color.toString();
    }

    public static final String DATA_URI_PREFIX = "data:base64,";
    public static final String ACCENT = "accent";
    public static final String SUCCESS = "success";
    public static final String WARNING = "warning";
    public static final String DANGER = "danger";
    public static final String TEXT = "text";
    public static final String FONT_ICON = "font-icon";
    public static final String BUTTON_CIRCLE = "button-circle";
    public static final String BUTTON_ICON = "button-icon";
    public static final String BUTTON_OUTLINED = "button-outlined";
    public static final String LEFT_PILL = "left-pill";
    public static final String CENTER_PILL = "center-pill";
    public static final String RIGHT_PILL = "right-pill";
    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";
    public static final String TOP = "top";
    public static final String RIGHT = "right";
    public static final String BOTTOM = "bottom";
    public static final String LEFT = "left";
    public static final String CENTER = "center";
    public static final String FLAT = "flat";
    public static final String BORDERED = "bordered";
    public static final String DENSE = "dense";
    public static final String ELEVATED_1 = "elevated-1";
    public static final String ELEVATED_2 = "elevated-2";
    public static final String ELEVATED_3 = "elevated-3";
    public static final String ELEVATED_4 = "elevated-4";
    public static final String INTERACTIVE = "interactive";
    public static final String ROUNDED = "rounded";
    public static final String STRIPED = "striped";
    public static final String TABS_CLASSIC = "classic";
    public static final String TABS_FLOATING = "floating";
    public static final String TITLE_1 = "title-1";
    public static final String TITLE_2 = "title-2";
    public static final String TITLE_3 = "title-3";
    public static final String TITLE_4 = "title-4";
    public static final String TEXT_CAPTION = "text-caption";
    public static final String TEXT_SMALL = "text-small";
    public static final String TEXT_BOLD = "text-bold";
    public static final String TEXT_BOLDER = "text-bolder";
    public static final String TEXT_NORMAL = "text-normal";
    public static final String TEXT_LIGHTER = "text-lighter";
    public static final String TEXT_ITALIC = "text-italic";
    public static final String TEXT_OBLIQUE = "text-oblique";
    public static final String TEXT_STRIKETHROUGH = "text-strikethrough";
    public static final String TEXT_UNDERLINED = "text-underlined";
    public static final String TEXT_MUTED = "text-muted";
    public static final String TEXT_SUBTLE = "text-subtle";
    public static final String TEXT_ON_EMPHASIS = "text-on-emphasis";
    public static final PseudoClass STATE_ACCENT = PseudoClass.getPseudoClass("accent");
    public static final PseudoClass STATE_SUCCESS = PseudoClass.getPseudoClass("success");
    public static final PseudoClass STATE_WARNING = PseudoClass.getPseudoClass("warning");
    public static final PseudoClass STATE_DANGER = PseudoClass.getPseudoClass("danger");
    public static final PseudoClass STATE_INTERACTIVE = PseudoClass.getPseudoClass("interactive");
    public static final String BG_DEFAULT = "bg-default";
    public static final String BG_INSET = "bg-inset";
    public static final String BG_SUBTLE = "bg-subtle";
    public static final String BG_NEUTRAL_EMPHASIS_PLUS = "bg-neutral-emphasis-plus";
    public static final String BG_NEUTRAL_EMPHASIS = "bg-neutral-emphasis";
    public static final String BG_NEUTRAL_MUTED = "bg-neutral-muted";
    public static final String BG_NEUTRAL_SUBTLE = "bg-neutral-subtle";
    public static final String BG_ACCENT_EMPHASIS = "bg-accent-emphasis";
    public static final String BG_ACCENT_MUTED = "bg-accent-muted";
    public static final String BG_ACCENT_SUBTLE = "bg-accent-subtle";
    public static final String BG_WARNING_EMPHASIS = "bg-warning-emphasis";
    public static final String BG_WARNING_MUTED = "bg-warning-muted";
    public static final String BG_WARNING_SUBTLE = "bg-warning-subtle";
    public static final String BG_SUCCESS_EMPHASIS = "bg-success-emphasis";
    public static final String BG_SUCCESS_MUTED = "bg-success-muted";
    public static final String BG_SUCCESS_SUBTLE = "bg-success-subtle";
    public static final String BG_DANGER_EMPHASIS = "bg-danger-emphasis";
    public static final String BG_DANGER_MUTED = "bg-danger-muted";
    public static final String BG_DANGER_SUBTLE = "bg-danger-subtle";
    public static final String BORDER_DEFAULT = "border-default";
    public static final String BORDER_MUTED = "border-muted";
    public static final String BORDER_SUBTLE = "border-subtle";

    public static void toggleStyleClass(Node node, String styleClass) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        } else if (styleClass == null) {
            throw new NullPointerException("Style class cannot be null!");
        } else {
            int idx = node.getStyleClass().indexOf(styleClass);
            if (idx >= 0) {
                node.getStyleClass().remove(idx);
            } else {
                node.getStyleClass().add(styleClass);
            }

        }
    }

    public static void addStyleClass(Node node, String styleClass, String... excludes) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        } else if (styleClass == null) {
            throw new NullPointerException("Style class cannot be null!");
        } else {
            if (excludes != null && excludes.length > 0) {
                node.getStyleClass().removeAll(excludes);
            }

            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }

        }
    }

    public static void activatePseudoClass(Node node, PseudoClass pseudoClass, PseudoClass... excludes) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        } else if (pseudoClass == null) {
            throw new NullPointerException("PseudoClass cannot be null!");
        } else {
            if (excludes != null) {
                PseudoClass[] var3 = excludes;
                int var4 = excludes.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    PseudoClass exclude = var3[var5];
                    node.pseudoClassStateChanged(exclude, false);
                }
            }

            node.pseudoClassStateChanged(pseudoClass, true);
        }
    }

    public static void appendStyle(Node node, String prop, String value) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        } else if (prop != null && !prop.isBlank() && value != null && !value.isBlank()) {
            String style = (String) Objects.requireNonNullElse(node.getStyle(), "");
            if (!style.isEmpty() && !style.endsWith(";")) {
                style = style + ";";
            }

            style = style + prop.trim() + ":" + value.trim() + ";";
            node.setStyle(style);
        } else {
            System.err.printf("Ignoring invalid style: property = '%s', value = '%s'%n", prop, value);
        }
    }

    public static void removeStyle(Node node, String prop) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        } else {
            String currentStyle = node.getStyle();
            if (currentStyle != null && !currentStyle.isBlank()) {
                if (prop != null && !prop.isBlank()) {
                    String[] stylePairs = currentStyle.split(";");
                    StringBuilder newStyle = new StringBuilder();
                    int var6 = stylePairs.length;
                    for (String s : stylePairs) {
                        String[] styleParts = s.split(":");
                        if (!styleParts[0].trim().equals(prop)) {
                            newStyle.append(s);
                            newStyle.append(";");
                        }
                    }

                    node.setStyle(newStyle.toString());
                } else {
                    System.err.printf("Ignoring invalid property = '%s'%n", prop);
                }
            }
        }
    }

    public static String toDataURI(String css) {
        if (css == null) {
            throw new NullPointerException("CSS string cannot be null!");
        } else {
            String var10000 = new String(Base64.getEncoder().encode(css.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            return "data:base64," + var10000;
        }
    }
}
