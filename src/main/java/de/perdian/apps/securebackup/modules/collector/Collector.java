package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.encryptor.Encryptor;
import de.perdian.apps.securebackup.modules.sources.SourceFile;
import de.perdian.apps.securebackup.modules.sources.SourceFileCollection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Collector {

    private static final Logger log = LoggerFactory.getLogger(Collector.class);

    private final ObservableList<SourceFileCollection> backlogCollections = FXCollections.observableArrayList();
    private final ObservableList<SourceFileCollection> completedCollections = FXCollections.observableArrayList();
    private final ObjectProperty<SourceFileCollection> activeCollection = new SimpleObjectProperty<>();
    private final DoubleProperty activeCollectionProgress = new SimpleDoubleProperty();
    private final ObservableList<SourceFile> backlogFiles = FXCollections.observableArrayList();
    private final ObservableList<SourceFile> completedFiles = FXCollections.observableArrayList();
    private final ObjectProperty<SourceFile> activeFile = new SimpleObjectProperty<>();
    private final DoubleProperty activeFileProgress = new SimpleDoubleProperty();
    private final ObjectProperty<Path> targetDirectory = new SimpleObjectProperty<>();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<Encryptor> encrypor = new SimpleObjectProperty<>();
    private final BooleanProperty busy = new SimpleBooleanProperty();
    private final List<CollectorProgressListener> progressListeners = new ArrayList<>();
    private final BooleanProperty cancelled = new SimpleBooleanProperty(false);
    private final ObjectProperty<CollectorResult> result = new SimpleObjectProperty<>();

    Collector(List<SourceFileCollection> collections, BooleanProperty busyProperty) {
        this.getBacklogCollections().setAll(collections);
        this.getCompletedCollections().clear();
        this.activeCollectionProperty().setValue(null);
        this.activeCollectionProgressProperty().setValue(0);
        this.getBacklogFiles().clear();
        this.getCompletedFiles().clear();
        this.activeFileProperty().setValue(null);
        this.activeFileProgressProperty().setValue(0);
        this.busyProperty().addListener((o, oldValue, newValue) -> busyProperty.setValue(newValue));
    }

    public void execute() throws IOException {
        this.fireProgress("Executing backup", null);
        this.busyProperty().setValue(true);
        try {
            int collectionBacklogSize = this.getBacklogCollections().size();
            for (int collectionIndex = 0; !this.cancelledProperty().getValue() && !this.getBacklogCollections().isEmpty(); collectionIndex++) {
                double collectionProgress = (double)(collectionIndex + 1d) / (double)collectionBacklogSize;
                SourceFileCollection collection = this.getBacklogCollections().removeFirst();
                this.activeCollectionProperty().setValue(collection);
                this.activeCollectionProgressProperty().setValue(collectionProgress);

                Path rootDirectory = this.targetDirectoryProperty().getValue();
                String collectionFileName = this.encryporProperty().getValue().createEncryptedFileName(collection.getName() + ".zip");
                Path collectionFile = rootDirectory.resolve(collectionFileName);
                if (!Files.exists(collectionFile.getParent())) {
                    this.fireProgress("Creating new directory at: " + collectionFile.getParent());
                    Files.createDirectories(collectionFile.getParent());
                }

                try (OutputStream collectionFileStream = new BufferedOutputStream(Files.newOutputStream(collectionFile))) {
                    try (OutputStream encryptedFileStream = this.encryporProperty().getValue().createEncryptedOutputStream(this.passwordProperty().getValue(), collectionFileStream)) {
                        try (ZipOutputStream collectionZipStream = new ZipOutputStream(collectionFileStream)) {
                            this.executeForCollection(collection, collectionZipStream);
                            collectionZipStream.finish();
                            collectionZipStream.flush();
                        }
                    }
                }
                this.getCompletedCollections().add(collection);

            }
            this.resultProperty().setValue(this.cancelledProperty().getValue() ? CollectorResult.CANCELLED : CollectorResult.SUCCESS);
        } catch (Exception e) {
            log.warn("Cannot execute collection", e);
            this.fireProgress("Cannot execute collection", e);
            this.resultProperty().setValue(CollectorResult.ERROR);
        } finally {
            this.busyProperty().setValue(false);
        }
    }

    private void executeForCollection(SourceFileCollection collection, ZipOutputStream zipOutputStream) throws IOException {

        List<SourceFile> collectionFiles = collection.getFiles();
        int collectionFilesSize = collectionFiles.size();
        this.getBacklogFiles().setAll(collectionFiles);
        this.getCompletedFiles().clear();
        this.activeFileProgressProperty().setValue(0);

        for (int fileIndex = 0; !this.cancelledProperty().getValue() && !collectionFiles.isEmpty(); fileIndex++) {
            double fileProgress = (double)(fileIndex + 1d) / (double)collectionFilesSize;
            SourceFile file = collectionFiles.removeFirst();
            this.activeFileProperty().setValue(file);
            this.activeFileProgressProperty().setValue(fileProgress);
            this.executeForCollectionFile(file, zipOutputStream);
            this.getCompletedFiles().add(file);
            this.fireProgress("Backed up file: " + file.getRelativeFileName(), null);
        }

    }

    private void executeForCollectionFile(SourceFile file, ZipOutputStream zipOutputStream) throws IOException {

        ZipEntry zipEntry = new ZipEntry(file.getRelativeFileName());
        zipEntry.setCreationTime(Files.getLastModifiedTime(file.getFile()));
        zipEntry.setLastModifiedTime(Files.getLastModifiedTime(file.getFile()));
        zipOutputStream.putNextEntry(zipEntry);

        Files.copy(file.getFile(), zipOutputStream);

    }

    public ObservableList<SourceFileCollection> getBacklogCollections() {
        return this.backlogCollections;
    }

    public ObservableList<SourceFileCollection> getCompletedCollections() {
        return this.completedCollections;
    }

    public ObjectProperty<SourceFileCollection> activeCollectionProperty() {
        return this.activeCollection;
    }

    public DoubleProperty activeCollectionProgressProperty() {
        return this.activeCollectionProgress;
    }

    public ObservableList<SourceFile> getBacklogFiles() {
        return this.backlogFiles;
    }

    public ObservableList<SourceFile> getCompletedFiles() {
        return this.completedFiles;
    }

    public ObjectProperty<SourceFile> activeFileProperty() {
        return this.activeFile;
    }

    public DoubleProperty activeFileProgressProperty() {
        return this.activeFileProgress;
    }

    public ObjectProperty<Path> targetDirectoryProperty() {
        return this.targetDirectory;
    }

    public StringProperty passwordProperty() {
        return this.password;
    }

    public ObjectProperty<Encryptor> encryporProperty() {
        return this.encrypor;
    }

    public BooleanProperty busyProperty() {
        return this.busy;
    }

    public BooleanProperty cancelledProperty() {
        return this.cancelled;
    }

    public ObjectProperty<CollectorResult> resultProperty() {
        return this.result;
    }

    private void fireProgress(String message) {
        this.fireProgress(message, null);
    }
    private void fireProgress(String message, Throwable exception) {
        this.getProgressListeners().forEach(progressListener -> progressListener.onProgress(message, exception));
    }
    public void addProgressListener(CollectorProgressListener progressListener) {
        this.getProgressListeners().add(progressListener);
    }
    public List<CollectorProgressListener> getProgressListeners() {
        return this.progressListeners;
    }

}
