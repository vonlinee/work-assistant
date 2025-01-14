package org.example.workassistant.fxui.view;

import org.example.workassistant.fxui.utils.FXUtils;
import javafx.scene.layout.Region;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.net.URL;
import java.util.Objects;

public class Icon extends Region {

    private final SVGImage svg;

    public Icon(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        svg = SVGLoader.load(Objects.requireNonNull(resource));
        getChildren().add(svg);
    }

    public final void setSize(int width) {
        this.svg.scaleTo(width);
    }

    @Override
    protected void layoutChildren() {
        FXUtils.layoutInRegion(this, svg);
    }
}
