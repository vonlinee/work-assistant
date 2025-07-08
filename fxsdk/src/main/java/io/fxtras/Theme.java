package io.fxtras;

import org.jetbrains.annotations.Nullable;

public interface Theme {

    String getName();

    String getUserAgentStylesheet();

    @Nullable
    String getUserAgentStylesheetBSS();

    default boolean isDarkMode() {
        return false;
    }

    static Theme of(final String name, final String userAgentStylesheet, final boolean darkMode) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null!");
        } else if (userAgentStylesheet == null) {
            throw new NullPointerException("User agent stylesheet cannot be null!");
        } else {
            return new Theme() {
                public String getName() {
                    return name;
                }

                public String getUserAgentStylesheet() {
                    return userAgentStylesheet;
                }

                public @Nullable String getUserAgentStylesheetBSS() {
                    return null;
                }

                public boolean isDarkMode() {
                    return darkMode;
                }
            };
        }
    }

    default boolean isDefault() {
        return "MODENA".equals(this.getUserAgentStylesheet()) || "CASPIAN".equals(this.getUserAgentStylesheet());
    }
}
