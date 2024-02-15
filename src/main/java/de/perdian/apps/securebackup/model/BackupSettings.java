package de.perdian.apps.securebackup.model;

import de.perdian.apps.securebackup.modules.preferences.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;

public class BackupSettings {

    private final ObjectProperty<Path> targetDirectory = new SimpleObjectProperty<>();
    private final ObservableList<SourcePackageBuilder> sourcePackageBuilders = FXCollections.observableArrayList();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty passwordConfirmation = new SimpleStringProperty();
    private final ObjectProperty<BackupEncryptionType> encryptionType = new SimpleObjectProperty<>(BackupEncryptionType.OPENSSL);

    public BackupSettings(Preferences preferences) {
        this.targetDirectoryProperty().bindBidirectional(preferences.resolvePathProperty("targetDirectory", null));
        this.passwordProperty().bindBidirectional(preferences.resolveStringProperty("password", null));
        this.encryptionTypeProperty().bindBidirectional(preferences.resolveEnumProperty("encryptionType", BackupEncryptionType.OPENSSL, BackupEncryptionType.class));
    }

    public ObjectProperty<Path> targetDirectoryProperty() {
        return this.targetDirectory;
    }

    public ObservableList<SourcePackageBuilder> getSourcePackageBuilders() {
        return this.sourcePackageBuilders;
    }

    public StringProperty passwordProperty() {
        return this.password;
    }

    public StringProperty passwordConfirmationProperty() {
        return this.passwordConfirmation;
    }

    public ObjectProperty<BackupEncryptionType> encryptionTypeProperty() {
        return this.encryptionType;
    }

}
