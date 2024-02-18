package de.perdian.apps.securebackup.support.fx.components;

import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;

public class ComponentDecorator {

    public static void addValidationDecorator(Node component, ObservableBooleanValue validProperty) {
        if (!validProperty.getValue()) {
            component.setStyle("-fx-control-inner-background: #ffeeee;");
        }
        validProperty.addListener((o, oldValue, newValue) -> Platform.runLater(() -> {
            component.setStyle(newValue ? "" : "-fx-control-inner-background: #ffeeee;");
        }));
    }

}
