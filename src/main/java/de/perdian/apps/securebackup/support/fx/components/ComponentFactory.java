package de.perdian.apps.securebackup.support.fx.components;

import javafx.scene.control.Label;

public class ComponentFactory {

    public static Label createSmallLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 90%");
        return label;
    }

}
