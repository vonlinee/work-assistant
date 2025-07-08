package io.fxtras.fxml;

import javafx.fxml.FXMLLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class ViewFXMLLoader extends FXMLLoader {

    String fxmlLocation;
    Class<?> viewClass;

    FXMLLocator locator;

    public <T> ViewFXMLLoader(String fxmlLocation, Class<T> viewClass) {
        if (fxmlLocation.isEmpty()) {
            String packageName = viewClass.getPackageName();
            fxmlLocation = packageName.replace(".", "/") + "/" + viewClass.getSimpleName() + ".fxml";
        }
        this.fxmlLocation = fxmlLocation;
        this.viewClass = viewClass;
    }

    public void setFXMLLocator(FXMLLocator locator) {
        this.locator = Objects.requireNonNull(locator, "locator cannot be null");
    }

    @Override
    public <T> T load() throws IOException {
        URL location = getLocation();
        if (location == null) {
            if (this.locator == null) {
                location = viewClass.getClassLoader().getResource(fxmlLocation);
            } else {
                location = this.locator.locate(this.viewClass, fxmlLocation);
            }
            if (location == null) {
                throw new FileNotFoundException(fxmlLocation);
            }
            setLocation(location);
        }
        return super.load(location.openStream());
    }
}
