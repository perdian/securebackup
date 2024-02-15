package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.encryptor.EncryptorType;
import de.perdian.apps.securebackup.modules.preferences.Preferences;
import de.perdian.apps.securebackup.support.fx.bindings.PathBindings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;

import java.nio.file.Path;

public class CollectorSettings {

    private ObjectProperty<Path> targetDirectory = null;
    private ObservableBooleanValue targetDirectoryExists = null;
    private StringProperty password = null;
    private StringProperty passwordConfirmation = null;
    private ObservableBooleanValue passwordsMatch = null;
    private ObjectProperty<EncryptorType> encryptorType = null;
    private ObservableBooleanValue valid = null;

    public CollectorSettings(Preferences preferences) {
        this.targetDirectory = preferences.resolvePathProperty("targetDirectory", null);
        this.targetDirectoryExists = PathBindings.exists(this.targetDirectory);
        this.password = preferences.resolveStringProperty("password", null);
        this.passwordConfirmation = new SimpleStringProperty();
        this.passwordsMatch = this.password.isEqualTo(this.passwordConfirmation);
        this.encryptorType = preferences.resolveEnumProperty("encryptorType", EncryptorType.OPENSSL, EncryptorType.class);
        this.valid = Bindings.and(this.targetDirectoryExistsProperty(), this.encryptorType.isNotNull()).and(this.password.isNotEmpty()).and(this.passwordsMatch);
    }

    public ObjectProperty<Path> targetDirectoryProperty() {
        return this.targetDirectory;
    }

    public ObservableBooleanValue targetDirectoryExistsProperty() {
        return this.targetDirectoryExists;
    }

    public StringProperty passwordProperty() {
        return this.password;
    }

    public ObservableBooleanValue passwordsMatchProperty() {
        return this.passwordsMatch;
    }

    public StringProperty passwordConfirmationProperty() {
        return this.passwordConfirmation;
    }

    public ObjectProperty<EncryptorType> encryptorTypeProperty() {
        return this.encryptorType;
    }

    public ObservableBooleanValue validProperty() {
        return this.valid;
    }

}
