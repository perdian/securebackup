package de.perdian.apps.securebackup.support.fx.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class ComponentFactory {

    public static <T> TextField createValidatedTextField(ObjectProperty<T> property, StringConverter<T> propertyStringConverter, ObservableBooleanValue validProperty) {
        TextField textField = ComponentFactory.createTextField(property, propertyStringConverter);
        ComponentDecorator.addValidationDecorator(textField, validProperty);
        return textField;
    }

    public static <T> TextField createTextField(ObjectProperty<T> objectProperty, StringConverter<T> objectStringConverter) {
        TextFormatter<T> fieldFormatter = new TextFormatter<>(objectStringConverter);
        fieldFormatter.valueProperty().bindBidirectional(objectProperty);
        TextField field = new TextField();
        field.setTextFormatter(fieldFormatter);
        return field;
    }

    public static Label createSmallLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 90%");
        return label;
    }

}
