package de.perdian.support.fx.preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Preferences {

    private static final Logger log = LoggerFactory.getLogger(Preferences.class);

    private Path storageFile = null;
    private ObservableMap<String, StringProperty> properties = null;

    public Preferences(Path storageFile) {
        this.setStorageFile(storageFile);
        this.setProperties(FXCollections.observableHashMap());
        this.appendPropertiesFromStorageFile();
    }

    private void appendPropertiesFromStorageFile() {
        Properties storageProperties = new Properties();
        if (Files.exists(this.getStorageFile())) {
            log.debug("Loading preferences from storage file at: {}", this.getStorageFile());
            try (InputStream storageFileStream = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(this.getStorageFile())))) {
                storageProperties.loadFromXML(storageFileStream);
            } catch (Exception e) {
                log.warn("Cannot read properties from storage file at: {}", this.getStorageFile(), e);
            }
        }
        this.putProperties(storageProperties);
    }

    private void writePropertiesToStorageFile() {
        try {
            if (this.getStorageFile().getParent() != null && !Files.exists(this.getStorageFile().getParent())) {
                Files.createDirectories(this.getStorageFile().getParent());
            }
            log.debug("Writing preferences into storage file at: {}", this.getStorageFile());
            try (OutputStream storageFileStream = new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(this.getStorageFile())))) {
                this.toProperties().storeToXML(storageFileStream, null);
            }
        } catch (Exception e) {
            log.warn("Cannot write properties into storage file at: {}", this.getStorageFile(), e);
        }
    }

    public synchronized StringProperty resolveStringProperty(String propertyName) {
        StringProperty stringProperty = this.getProperties().get(propertyName);
        if (stringProperty == null) {
            stringProperty = this.createStringProperty(null);
            this.getProperties().put(propertyName, stringProperty);
        }
        return stringProperty;
    }

    private StringProperty createStringProperty(String initialValue) {
        StringProperty stringProperty = new SimpleStringProperty(initialValue);
        stringProperty.addListener((o, oldValue, newValue) -> this.writePropertiesToStorageFile());
        return stringProperty;
    }

    private void putProperties(Properties properties) {
        for (Map.Entry<Object, Object> propertyEntry : properties.entrySet()) {
            String propertyName = (String)propertyEntry.getKey();
            String propertyValue = (String)propertyEntry.getValue();
            this.getProperties().put(propertyName, this.createStringProperty(propertyValue));
        }
    }

    private Properties toProperties() {
        Properties properties = new Properties();
        for (Map.Entry<String, StringProperty> propertyEntry : this.getProperties().entrySet()) {
            if (StringUtils.isNotEmpty(propertyEntry.getValue().getValue())) {
                properties.setProperty(propertyEntry.getKey(), propertyEntry.getValue().getValue());
            }
        }
        return properties;
    }

    private Path getStorageFile() {
        return this.storageFile;
    }
    private void setStorageFile(Path storageFile) {
        this.storageFile = storageFile;
    }

    private ObservableMap<String, StringProperty> getProperties() {
        return this.properties;
    }
    private void setProperties(ObservableMap<String, StringProperty> properties) {
        this.properties = properties;
    }

}
