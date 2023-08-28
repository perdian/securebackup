package de.perdian.apps.securebackup.fx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;

public class ArchiverModel {

    private final ObjectProperty<Path> targetDirectory = new SimpleObjectProperty<>();
    private final ObservableList<SourceModel> sources = FXCollections.observableArrayList();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<EncryptionType> encryptionType = new SimpleObjectProperty<>(EncryptionType.OPENSSL);

    public ObjectProperty<Path> getTargetDirectory() {
        return this.targetDirectory;
    }

    public ObservableList<SourceModel> getSources() {
        return this.sources;
    }

    public StringProperty getPassword() {
        return this.password;
    }

    public ObjectProperty<EncryptionType> getEncryptionType() {
        return this.encryptionType;
    }

}
