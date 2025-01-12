package io.devpl.fxui.fxtras;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 资源目录
 */
public abstract class JavaFXApplication extends Application {

    @Override
    public final void init() throws Exception {
        super.init();
        this.onInit();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    protected void onInit() throws Exception {
    }

    protected void onStop() {
    }

    @Override
    public final void stop() throws Exception {
        super.stop();
        this.onStop();
    }
}
