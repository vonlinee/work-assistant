package org.workassistant.ui.view;

public interface LifeCycle {

    void init();

    void destroy();

    void beforeCreate();

    void beforeDestroy();
}
