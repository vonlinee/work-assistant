package io.fxtras.fxml;

import java.net.URL;

public interface FXMLLocator {

    URL locate(Class<?> viewClass, String location);
}
