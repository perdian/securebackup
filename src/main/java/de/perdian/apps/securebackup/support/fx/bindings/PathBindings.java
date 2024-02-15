package de.perdian.apps.securebackup.support.fx.bindings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathBindings {

    public static ObservableBooleanValue exists(Property<Path> pathProperty) {
        BooleanProperty existsProperty = new SimpleBooleanProperty(pathProperty.getValue() != null && Files.exists(pathProperty.getValue()));
        pathProperty.addListener((o, oldValue, newValue) -> existsProperty.setValue(newValue != null && Files.exists(newValue)));
        return existsProperty;
    }

}
