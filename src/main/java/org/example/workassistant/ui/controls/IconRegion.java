package org.example.workassistant.ui.controls;

import io.fxtras.FXUtils;
import javafx.scene.layout.Region;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import java.net.URL;
import java.util.Objects;

public final class IconRegion extends Region {

    private final SVGImage svg;

    public IconRegion(String name) {
        URL resource = getClass().getResource(name);
        svg = SVGLoader.load(Objects.requireNonNull(resource));
        getChildren().add(svg);
    }

    public void setSize(int width) {
        this.svg.scaleTo(width);
    }

    @Override
    protected void layoutChildren() {
        FXUtils.layoutInRegion(this, svg);
    }
}
