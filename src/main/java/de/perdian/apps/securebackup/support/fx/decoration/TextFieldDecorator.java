package de.perdian.apps.securebackup.support.fx.decoration;

import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.TextField;

public class TextFieldDecorator {

    public static void bindErrorBackground(TextField textField, ObservableBooleanValue validationValue) {
        if (!validationValue.get()) {
            textField.setStyle("-fx-control-inner-background: #ffeeee;");
        }
        validationValue.addListener((o, oldValue, newValue) -> Platform.runLater(() -> {
            textField.setStyle(newValue ? "" : "-fx-control-inner-background: #ffeeee;");
        }));
    }

}
