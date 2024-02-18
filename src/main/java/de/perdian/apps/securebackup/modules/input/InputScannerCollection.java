package de.perdian.apps.securebackup.modules.input;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.perdian.apps.securebackup.SecureBackupPreferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InputScannerCollection {

    private static final Logger log = LoggerFactory.getLogger(InputScannerCollection.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ObjectProperty<Path> configurationFile = null;
    private ObservableList<InputScanner> inputScanners = null;
    private boolean saveEnabled = true;

    public InputScannerCollection(SecureBackupPreferences preferences) {

        this.configurationFile = preferences.resolvePathProperty("inputScannerCollectionConfigurationFile", null);
        this.inputScanners = FXCollections.observableArrayList();

        ChangeListener<InputScanner> inputScannerChangeListener = (o, oldValue, newValue) -> this.saveIntoFile(this.configurationFile.getValue());
        this.inputScanners.addListener((ListChangeListener<InputScanner>) change -> {
            while (change.next()) {
                for (InputScanner addedInputScanner : change.getAddedSubList()) {
                    addedInputScanner.addChangeListener(inputScannerChangeListener);
                }
                for (InputScanner removedInputScanner : change.getRemoved()) {
                    removedInputScanner.removeChangeListener(inputScannerChangeListener);
                }
            }
            if (this.isSaveEnabled()) {
                inputScannerChangeListener.changed(null, null, null);
            }
        });

        this.loadFromFile(this.configurationFile.getValue());

    }

    public void loadFromFile(Path sourceFile) {
        if (sourceFile != null && Files.exists(sourceFile)) {
            this.setSaveEnabled(false);
            try {
                this.configurationFileProperty().setValue(sourceFile);
                try {

                    log.debug("Loading input scanners from JSON configuration file at: {}", sourceFile);
                    try (InputStream configurationFileStream = new BufferedInputStream(Files.newInputStream(sourceFile))) {
                        ObjectReader objectReader = OBJECT_MAPPER.reader();
                        JsonNode rootNode = objectReader.readTree(configurationFileStream);
                        this.getInputScanners().setAll(this.parseInputScanners(rootNode.withArray("inputScanners")));
                    }

                } catch (Exception e) {
                    log.error("Cannot read input scanners from JSON configuration file at: {}", sourceFile, e);
                }
            } finally {
                this.setSaveEnabled(true);
            }
        }
    }

    private List<InputScanner> parseInputScanners(ArrayNode inputScannersNode) {
        List<InputScanner> inputScanners = new ArrayList<>(inputScannersNode.size());
        for (int i=0; i < inputScannersNode.size(); i++) {
            inputScanners.add(this.parseInputScanner(inputScannersNode.get(i)));
        }
        return inputScanners;
    }

    private InputScanner parseInputScanner(JsonNode inputScannerNode) {

        String rootDirectoryValue = inputScannerNode.get("rootDirectory") == null || inputScannerNode.get("rootDirectory").isNull() ? null : inputScannerNode.get("rootDirectory").asText();
        String rootNameValue = inputScannerNode.get("rootName") == null || inputScannerNode.get("rootName").isNull()  ? null : inputScannerNode.get("rootName").asText();
        String separatePackageDepthValue = inputScannerNode.get("separatePackageDepth") == null ? null : inputScannerNode.get("separatePackageDepth").asText();

        InputScanner inputScannerPackage = new InputScanner(StringUtils.isEmpty(rootDirectoryValue) ? null : Path.of(rootDirectoryValue));
        inputScannerPackage.rootNameProperty().setValue(rootNameValue);
        inputScannerPackage.separatePackageDepthProperty().setValue(StringUtils.isEmpty(separatePackageDepthValue) ? null : Integer.valueOf(separatePackageDepthValue));
        inputScannerPackage.getExcludePatterns().setAll(this.parsePatterns(inputScannerNode.get("excludePatterns")));
        inputScannerPackage.getIncludePatterns().setAll(this.parsePatterns(inputScannerNode.get("includePatterns")));
        return inputScannerPackage;

    }

    private Collection<String> parsePatterns(JsonNode patternsNode) {
        if (patternsNode != null && patternsNode.isArray()) {
            List<String> patternsList = new ArrayList<>(patternsNode.size());
            for (int i=0; i < patternsNode.size(); i++) {
                patternsList.add(patternsNode.get(i).asText());
            }
            return patternsList;
        } else {
            return Collections.emptyList();
        }
    }

    public void saveIntoFile(Path targetFile) {
        if (targetFile != null) {
            this.configurationFileProperty().setValue(targetFile);
            try {

                if (!Files.exists(targetFile.getParent())) {
                    try {
                        Files.createDirectories(targetFile.getParent());
                    } catch (IOException e) {
                        throw new IOException("Cannot create parent directory for JSON configuration file at: " + targetFile.getParent(), e);
                    }
                }

                log.debug("Storing input scanners into JSON configuration file at: {}", targetFile);
                ArrayNode inputScannersNode = OBJECT_MAPPER.createArrayNode();
                this.getInputScanners().forEach(inputScanner -> inputScannersNode.add(this.createInputScannerNode(inputScanner)));
                ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
                rootNode.set("inputScanners", inputScannersNode);
                ObjectWriter objectWriter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
                try (OutputStream storageFileStream = new BufferedOutputStream(Files.newOutputStream(targetFile))) {
                    objectWriter.writeValue(storageFileStream, rootNode);
                }

            } catch (IOException e) {
                log.error("Cannot store input scanners into JSON configuration file at: {}", targetFile, e);
            }
        }
    }

    private JsonNode createInputScannerNode(InputScanner inputScanner) {
        ObjectNode packageNode = OBJECT_MAPPER.createObjectNode();
        packageNode.put("rootDirectory", inputScanner.rootDirectoryProperty().getValue() == null ? null : inputScanner.rootDirectoryProperty().getValue().toString());
        packageNode.put("rootName", inputScanner.rootNameProperty().getValue() == null ? null : inputScanner.rootNameProperty().getValue());
        packageNode.put("separatePackageDepth", inputScanner.separatePackageDepthProperty().getValue() == null ? null : String.valueOf(inputScanner.separatePackageDepthProperty().getValue()));
        packageNode.set("excludePatterns", this.createInputScannerPatternsNode(inputScanner.getExcludePatterns()));
        packageNode.set("includePatterns", this.createInputScannerPatternsNode(inputScanner.getIncludePatterns()));
        return packageNode;
    }

    private ArrayNode createInputScannerPatternsNode(List<String> patterns) {
        ArrayNode patternsNode = OBJECT_MAPPER.createArrayNode();
        patterns.forEach(pattern -> patternsNode.add(pattern));
        return patternsNode;
    }

    public ObjectProperty<Path> configurationFileProperty() {
        return this.configurationFile;
    }

    public ObservableList<InputScanner> getInputScanners() {
        return this.inputScanners;
    }

    private boolean isSaveEnabled() {
        return this.saveEnabled;
    }
    private void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
    }

}
