package de.perdian.apps.securebackup.fx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EncryptionModel {

    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<EncryptionType> type = new SimpleObjectProperty<>(EncryptionType.OPENSSL);

    public StringProperty passwordProperty() {
        return password;
    }

    public ObjectProperty<EncryptionType> typeProperty() {
        return type;
    }

}
