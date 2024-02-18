package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.encryptor.Encryptor;
import de.perdian.apps.securebackup.modules.input.InputPackage;
import de.perdian.apps.securebackup.modules.input.InputPackageFile;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CollectorJob {

    private static final Logger log = LoggerFactory.getLogger(CollectorJob.class);

    private String id = null;
    private Path targetDirectory = null;
    private ObservableList<InputPackage> inputPackages = null;
    private Encryptor encryptor = null;
    private String encryptionPassword = null;
    private String encryptionPasswordHint = null;
    private CollectorJobProgress<InputPackage> packageProgress = null;
    private CollectorJobProgress<InputPackageFile> packageFileProgress = null;
    private ObjectProperty<CollectorJobStatus> status = null;

    CollectorJob(String id, Path targetDirectory, List<InputPackage> inputPackages) {
        this.id = id;
        this.targetDirectory = targetDirectory;
        this.inputPackages = FXCollections.observableArrayList(inputPackages);
        this.packageProgress = new CollectorJobProgress<>();
        this.packageFileProgress = new CollectorJobProgress<>();
        this.status = new SimpleObjectProperty<>(CollectorJobStatus.NEW);
    }

    synchronized void run() {
        if (!CollectorJobStatus.NEW.equals(this.getStatus())) {
            throw new IllegalStateException("CollectorJob has already been started");
        } else {
            log.debug("Starting CollectorJob into: {}", this.getTargetDirectory());
            Platform.runLater(() -> this.setStatus(CollectorJobStatus.RUNNING));
            try {
                this.runInternal();
                if (!CollectorJobStatus.CANCELLED.equals(this.getStatus())) {
                    log.debug("CollectorJob completed collection into: {}", this.getTargetDirectory());
                    Platform.runLater(() -> this.setStatus(CollectorJobStatus.FINISHED_SUCCESS));
                }
            } catch (Exception e) {
                log.error("CollectorJob failed collection into: {}", this.getTargetDirectory(), e);
                this.getPackageProgress().fireProgress(null, "CollectorJob failed collection", e);
                Platform.runLater(() -> this.setStatus(CollectorJobStatus.FINISHED_ERROR));
            }
        }
    }

    private void runInternal() throws Exception {

        this.collectMetaInformation();

        List<InputPackage> inputPackages = this.getInputPackages();
        this.getPackageProgress().initializeItems(inputPackages);

        for (int inputPackageIndex = 0; !this.isCancelled() && inputPackageIndex < inputPackages.size(); inputPackageIndex++) {

            InputPackage inputPackage = inputPackages.get(inputPackageIndex);
            this.getPackageProgress().currentItemProperty().setValue(inputPackage);
            this.getPackageProgress().getBacklogItems().remove(inputPackage);

            Double inputPackageProgress = (double)(inputPackageIndex + 1) / (double)inputPackages.size();
            this.getPackageProgress().fireProgress(inputPackageProgress, "Processing package: " + inputPackage.getName(), null);

            this.collectInputPackage(inputPackage);
            this.getPackageProgress().getCompletedItems().add(inputPackage);

        }

    }

    private void collectMetaInformation() throws Exception {

        StringBuilder metaInformationFileContent = new StringBuilder();
        metaInformationFileContent.append("## Creation time\n\n");
        metaInformationFileContent.append(ZonedDateTime.now()).append("\n");

        if (StringUtils.isNotEmpty(this.getEncryptionPasswordHint())) {
            metaInformationFileContent.append("\n\n## Password hint\n\n");
            metaInformationFileContent.append(this.getEncryptionPasswordHint()).append("\n");
        }

        String encryptorReadme = this.getEncryptor().createReadme();
        if (StringUtils.isNotEmpty(encryptorReadme)) {
            metaInformationFileContent.append("\n\n## Encryptor Information\n\n");
            metaInformationFileContent.append(encryptorReadme).append("\n");
        }

        Path metaInformationFile = this.getTargetDirectory().resolve("README.txt");
        if (!Files.exists(metaInformationFile.getParent())) {
            Files.createDirectories(metaInformationFile.getParent());
        }
        Files.writeString(metaInformationFile, metaInformationFileContent.toString());

    }

    private void collectInputPackage(InputPackage inputPackage) throws Exception {

        // First we collect all the content of the package into a temporary zip file
        Path archiveFile = Files.createTempFile("collector", ".zip");
        try {

            try (OutputStream archiveFileStream = new BufferedOutputStream(Files.newOutputStream(archiveFile))) {
                try (ZipOutputStream archiveZipStream = new ZipOutputStream(archiveFileStream)) {
                    archiveZipStream.setLevel(9);
                    this.collectInputPackageArchive(inputPackage, archiveZipStream);
                    archiveZipStream.finish();
                }
            }

            // Now we encrypt the archive into the actual target location
            Path collectorTargetDirectory = this.getTargetDirectory();
            String collectorTargetBaseFileName = "archives/" + inputPackage.getName() + ".zip";
            String collectorTargetFileName = this.getEncryptor().createEncryptedFileName(collectorTargetBaseFileName);
            Path collectorTargetFile = collectorTargetDirectory.resolve(collectorTargetFileName);
            if (!Files.exists(collectorTargetFile.getParent())) {
                this.getPackageProgress().fireProgress(null, "Creating new directory at: " + collectorTargetFile.getParent(), null);
                Files.createDirectories(collectorTargetFile.getParent());
            }

            try (OutputStream collectorTargetStream = Files.newOutputStream(collectorTargetFile)) {
                try (OutputStream encryptedOutputStream = this.getEncryptor().createEncryptedOutputStream(this.getEncryptionPassword(), collectorTargetStream)) {
                    Files.copy(archiveFile, encryptedOutputStream);
                }
            }
            Files.setLastModifiedTime(collectorTargetFile, FileTime.from(inputPackage.getLastModifiedTime()));

        } finally {
            Files.deleteIfExists(archiveFile);
        }

    }

    private void collectInputPackageArchive(InputPackage inputPackage, ZipOutputStream archiveZipStream) throws Exception {

        List<InputPackageFile> inputPackageFiles = inputPackage.getFiles();
        this.getPackageFileProgress().initializeItems(inputPackageFiles);

        for (int inputPackageFileIndex = 0; !this.isCancelled() && inputPackageFileIndex < inputPackageFiles.size(); inputPackageFileIndex++) {

            InputPackageFile inputPackageFile = inputPackageFiles.get(inputPackageFileIndex);
            this.getPackageFileProgress().currentItemProperty().setValue(inputPackageFile);
            this.getPackageFileProgress().getBacklogItems().remove(inputPackageFile);

            String inputPackageName = inputPackage.getName();
            int inputPackageLastSlashIndex = inputPackageName.lastIndexOf("/");
            String inputPackageFilePrefix = inputPackageLastSlashIndex <= 0 ? inputPackageName : inputPackageName.substring(inputPackageLastSlashIndex + 1);
            Double inputPackageFileProgress = (double)(inputPackageFileIndex + 1) / (double)inputPackageFiles.size();
            this.getPackageFileProgress().fireProgress(inputPackageFileProgress, "Processing file: " + inputPackageFile.getFile().getFileName(), null);
            this.collectInputPackageArchiveFile(inputPackageFile, inputPackageFilePrefix, archiveZipStream);

        }

    }

    private void collectInputPackageArchiveFile(InputPackageFile file, String fileNamePrefix, ZipOutputStream zipOutputStream) throws Exception {

        ZipEntry zipEntry = new ZipEntry(fileNamePrefix + "/" + file.getRelativeFileName());
        zipEntry.setCreationTime(Files.getLastModifiedTime(file.getFile()));
        zipEntry.setLastModifiedTime(Files.getLastModifiedTime(file.getFile()));
        zipOutputStream.putNextEntry(zipEntry);

        Files.copy(file.getFile(), zipOutputStream);
        zipOutputStream.closeEntry();

    }

    public String getId() {
        return this.id;
    }

    public Path getTargetDirectory() {
        return this.targetDirectory;
    }

    public ObservableList<InputPackage> getInputPackages() {
        return this.inputPackages;
    }

    String getEncryptionPassword() {
        return this.encryptionPassword;
    }
    void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    String getEncryptionPasswordHint() {
        return this.encryptionPasswordHint;
    }
    void setEncryptionPasswordHint(String encryptionPasswordHint) {
        this.encryptionPasswordHint = encryptionPasswordHint;
    }

    public Encryptor getEncryptor() {
        return this.encryptor;
    }
    void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public CollectorJobProgress<InputPackage> getPackageProgress() {
        return this.packageProgress;
    }

    public CollectorJobProgress<InputPackageFile> getPackageFileProgress() {
        return this.packageFileProgress;
    }
    private void setPackageFileProgress(CollectorJobProgress<InputPackageFile> packageFileProgress) {
        this.packageFileProgress = packageFileProgress;
    }

    public boolean isCancelled() {
        return CollectorJobStatus.CANCELLED.equals(this.getStatus());
    }

    public boolean isFinished() {
        return CollectorJobStatus.FINISHED_SUCCESS.equals(this.getStatus())
            || CollectorJobStatus.FINISHED_ERROR.equals(this.getStatus());
    }

    public CollectorJobStatus getStatus() {
        return this.status.getValue();
    }
    private void setStatus(CollectorJobStatus status) {
        this.status.setValue(status);
    }
    public ReadOnlyObjectProperty<CollectorJobStatus> statusProperty() {
        return this.status;
    }

}
