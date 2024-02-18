package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.SecureBackupPreferences;
import de.perdian.apps.securebackup.modules.encryptor.EncryptorType;
import de.perdian.apps.securebackup.modules.input.InputPackage;
import de.perdian.apps.securebackup.modules.input.InputScannerCollection;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Collector {

    private static final DateTimeFormatter JOB_ID_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    private ObjectProperty<Path> targetDirectory = null;
    private StringProperty password = null;
    private StringProperty passwordConfirmation = null;
    private StringProperty passwordHint = null;
    private ObservableBooleanValue passwordsMatch = null;
    private ObjectProperty<EncryptorType> encryptorType = null;
    private ObservableList<CollectorJob> activeJobs = null;
    private ObservableBooleanValue busy = null;
    private ObservableBooleanValue ready = null;

    public Collector(SecureBackupPreferences preferences) {
        this.targetDirectory = preferences.resolvePathProperty("targetDirectory", null);
        this.password = preferences.resolveStringProperty("password", null);
        this.passwordHint = preferences.resolveStringProperty("passwordHint", null);
        this.passwordConfirmation = new SimpleStringProperty();
        this.passwordsMatch = this.password.isEqualTo(this.passwordConfirmation).and(this.password.isNotEmpty()).and(this.passwordConfirmation.isNotEmpty());
        this.encryptorType = preferences.resolveEnumProperty("encryptorType", EncryptorType.OPENSSL, EncryptorType.class);
        this.activeJobs = FXCollections.observableArrayList();
        this.busy = Bindings.isNotEmpty(this.activeJobs);
        this.ready = this.targetDirectory.isNotNull().and(this.passwordProperty().isNotEmpty()).and(this.passwordConfirmationProperty().isNotEmpty()).and(this.passwordsMatch).and(this.encryptorType.isNotNull());
this.passwordConfirmation.setValue(this.password.getValue());
    }

    public synchronized CollectorJob startJob(InputScannerCollection inputScannerCollection) {

        Path collectorTargetDirectory = this.targetDirectory.getValue();
        String collectorJobId = JOB_ID_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
        Path collectorJobTargetDirectory = collectorTargetDirectory.resolve(collectorJobId);

        List<InputPackage> inputPackages = inputScannerCollection.getInputScanners().stream()
            .flatMap(inputScanner -> inputScanner.createInputPackages().stream())
            .toList();

        CollectorJob collectorJob = new CollectorJob(collectorJobId, collectorJobTargetDirectory, inputPackages);
        collectorJob.setEncryptor(this.encryptorTypeProperty().getValue().createEncryptor());
        collectorJob.setEncryptionPassword(this.passwordProperty().getValue());
        collectorJob.setEncryptionPasswordHint(this.passwordHintProperty().getValue());
        this.getActiveJobs().add(collectorJob);
        Thread.ofVirtual().start(() -> {
            try {
                collectorJob.run();
            } finally {
                this.getActiveJobs().remove(collectorJob);
            }
        });
        return collectorJob;

    }

    public ObjectProperty<Path> targetDirectoryProperty() {
        return this.targetDirectory;
    }

    public StringProperty passwordProperty() {
        return this.password;
    }

    public StringProperty passwordConfirmationProperty() {
        return this.passwordConfirmation;
    }

    public StringProperty passwordHintProperty() {
        return this.passwordHint;
    }

    public ObservableBooleanValue passwordsMatchProperty() {
        return this.passwordsMatch;
    }

    public ObjectProperty<EncryptorType> encryptorTypeProperty() {
        return this.encryptorType;
    }

    public ObservableList<CollectorJob> getActiveJobs() {
        return this.activeJobs;
    }
    private void setActiveJobs(ObservableList<CollectorJob> activeJobs) {
        this.activeJobs = activeJobs;
    }

    public ObservableBooleanValue busyProperty() {
        return this.busy;
    }

    public ObservableBooleanValue readyProperty() {
        return this.ready;
    }

}
