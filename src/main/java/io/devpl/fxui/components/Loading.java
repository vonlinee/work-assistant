package io.devpl.fxui.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Loading extends StackPane {

    public static Loading wrap(Scene scene) {
        return Loading.get(scene);
    }

    final double maxOpacity = 0.5;

    private static final String ID = "loading";
    private final Label label;
    private final ProgressIndicator progressIndicator;
    private final Pane loadingPane;
    private final FadeTransition showAnimation;
    private final FadeTransition hideAnimation;

    static Loading get(Scene scene) {
        final Parent root = scene.getRoot();
        if (root instanceof StackPane && ID.equals(root.getId())) {
            return (Loading) root;
        } else {
            Loading newRoot = new Loading(root);
            scene.setRoot(newRoot);
            return newRoot;
        }
    }

    public static Loading wrap(Region region) {
        return new Loading(region);
    }

    private Loading(Parent originalRoot) {
        setId(ID);

        this.progressIndicator = new ProgressIndicator();
        this.progressIndicator.setMaxWidth(20);
        this.progressIndicator.setMaxHeight(20);
        this.label = new Label();
        this.label.setStyle("-fx-text-fill:white");

        VBox pane = new VBox();
        pane.getChildren().add(this.progressIndicator);
        pane.getChildren().add(this.label);
        pane.setVisible(false);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: black");
        pane.setSpacing(5.0);

        getChildren().addAll(originalRoot, this.loadingPane = pane);
        // 动画
        this.showAnimation = createFadeTransition(Duration.seconds(1), this.loadingPane, 0, maxOpacity);
        this.hideAnimation = createFadeTransition(Duration.seconds(0.3), this.loadingPane, maxOpacity, 0);
        this.hideAnimation.setOnFinished(e -> this.loadingPane.setVisible(false));
    }

    private static FadeTransition createFadeTransition(Duration duration, Parent parent, double from, double to) {
        FadeTransition fade = new FadeTransition(duration, parent);
        fade.setFromValue(from);
        fade.setToValue(to);
        return fade;
    }

    public final void setMaxOpacity(double maxOpacity) {
        this.showAnimation.setFromValue(0);
        this.showAnimation.setToValue(maxOpacity);
        this.hideAnimation.setFromValue(maxOpacity);
        this.hideAnimation.setToValue(0);
    }

    public final double getMaxOpacity() {
        return maxOpacity;
    }

    public final void show(String message) {
        this.label.setText(message);
        this.showAnimation.playFromStart();
        this.loadingPane.setVisible(true);
        this.progressIndicator.setProgress(0);
        this.progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    public final void hide() {
        this.hideAnimation.playFromStart();
    }
}
