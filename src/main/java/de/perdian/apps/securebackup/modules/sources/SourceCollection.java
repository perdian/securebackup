package de.perdian.apps.securebackup.modules.sources;

import de.perdian.apps.securebackup.modules.preferences.Preferences;
import de.perdian.apps.securebackup.modules.sources.impl.JsonSourcesStorageDelegate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCollection {

    private static final Logger log = LoggerFactory.getLogger(SourceCollection.class);

    private final SourceCollectionStorageDelegate storageDelegate = new JsonSourcesStorageDelegate();
    private final ObjectProperty<Path> storageFile = new SimpleObjectProperty<>(null);
    private final ObservableList<SourcePackage> packages = FXCollections.observableArrayList();

    public SourceCollection(Preferences preferences) {

        ObjectProperty<Path> preferencesStorageFile = preferences.resolvePathProperty("sourcesStorageFile", null);
        this.storageFileProperty().bindBidirectional(preferencesStorageFile);
        this.loadFromFile(preferencesStorageFile.getValue());

        ChangeListener<SourcePackage> packageChangeListener = (o, oldValue, newValue) -> this.saveIntoFile(this.storageFileProperty().getValue());
        packages.addListener((ListChangeListener<SourcePackage>) change -> {
            while (change.next()) {
                for (SourcePackage addedPackage : change.getAddedSubList()) {
                    addedPackage.addChangeListener(packageChangeListener);
                }
                for (SourcePackage removedPackage : change.getRemoved()) {
                    removedPackage.removeChangeListener(packageChangeListener);
                }
            }
        });

    }

    public void loadFromFile(Path sourceFile) {
        if (sourceFile != null && Files.exists(sourceFile)) {
            this.storageFileProperty().setValue(sourceFile);
            try {
                this.getStorageDelegate().loadPackagesFromFile(sourceFile, this.getPackages());
            } catch (IOException e) {
                log.error("Cannot read from storage file at: {}", sourceFile, e);
            }
        }
    }

    public void saveIntoFile(Path targetFile) {
        if (targetFile != null) {
            this.storageFileProperty().setValue(targetFile);
            if (!Files.exists(targetFile.getParent())) {
                try {
                    Files.createDirectories(targetFile.getParent());
                } catch (IOException e) {
                    log.warn("Cannot create parent directory for storage file at: {}", targetFile.getParent(), e);
                }
            }
            try {
                this.getStorageDelegate().savePackagesIntoFile(targetFile, this.getPackages());
            } catch (IOException e) {
                log.error("Cannot write into storage file at: {}", targetFile, e);
            }
        }
    }

    private SourceCollectionStorageDelegate getStorageDelegate() {
        return this.storageDelegate;
    }

    public ObjectProperty<Path> storageFileProperty() {
        return this.storageFile;
    }

    public ObservableList<SourcePackage> getPackages() {
        return this.packages;
    }


}
