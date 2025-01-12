package io.devpl.fxui.view;

public interface LifeCycle {

    void init();

    void destroy();

    void beforeCreate();

    void beforeDestroy();
}
