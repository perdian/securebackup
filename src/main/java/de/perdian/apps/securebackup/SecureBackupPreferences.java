package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.support.fx.properties.converters.PathStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.davidafsilva.apple.OSXKeychain;
import pt.davidafsilva.apple.OSXKeychainException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SecureBackupPreferences {

    private static final Logger log = LoggerFactory.getLogger(SecureBackupPreferences.class);

    private StorageDelegate storageDelegate = null;
    private Map<String, StringProperty> properties = null;

    public SecureBackupPreferences() {
        this.setStorageDelegate(StorageDelegate.createInstance());
        this.setProperties(new HashMap<>());
    }

    public synchronized StringProperty resolveStringProperty(String propertyName) {
        return this.resolveStringProperty(propertyName, null);
    }

    public ObjectProperty<Path> resolvePathProperty(String propertyName, Path initialValue) {
        return this.resolveObjectProperty(propertyName, initialValue == null ? null : initialValue, new PathStringConverter());
    }

    public <T extends Enum<T>> ObjectProperty<T> resolveEnumProperty(String propertyName, T initialValue, Class<T> enumClass) {
        return this.resolveObjectProperty(propertyName, initialValue == null ? null : initialValue, new StringConverter<T>() {
            @Override public String toString(T object) {
                return object == null ? null : object.name();
            }
            @Override
            public T fromString(String string) {
                for (T enumConstant : enumClass.getEnumConstants()) {
                    if (StringUtils.equalsIgnoreCase(enumConstant.name(), string)) {
                        return enumConstant;
                    }
                }
                return null;
            }
        });
    }

    public <T> ObjectProperty<T> resolveObjectProperty(String propertyName, T initialValue, StringConverter<T> stringConverter) {
        String initialStringValue = stringConverter.toString(initialValue);
        StringProperty stringProperty = this.resolveStringProperty(propertyName, initialStringValue);
        ObjectProperty<T> objectProperty = new SimpleObjectProperty<>(stringConverter.fromString(stringProperty.getValue()));
        objectProperty.addListener((o, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                String newStringValue = stringConverter.toString(newValue);
                if (!Objects.equals(newStringValue, stringProperty.getValue())) {
                    stringProperty.setValue(newStringValue);
                }
            }
        });
        stringProperty.addListener((o, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                T newObjectValue = stringConverter.fromString(newValue);
                if (!Objects.equals(newObjectValue, objectProperty.getValue())) {
                    objectProperty.setValue(newObjectValue);
                }
            }
        });
        return objectProperty;
    }

    public synchronized StringProperty resolveStringProperty(String propertyName, String initialValue) {
        StringProperty stringProperty = this.getProperties().get(propertyName);
        if (stringProperty == null) {
            String storageValue = this.loadStorageDelegateValue(propertyName);
            stringProperty = new SimpleStringProperty(storageValue == null ? initialValue : storageValue);
            stringProperty.addListener((o, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    this.writeStorageDelegateValue(propertyName, newValue);
                }
            });
            this.getProperties().put(propertyName, stringProperty);
        }
        return stringProperty;
    }

    private String loadStorageDelegateValue(String propertyName) {
        try {
            return this.getStorageDelegate().loadProperty(propertyName);
        } catch (Exception e) {
            log.warn("Cannot read stored preferences value for property '{}' from storage: {}", propertyName, this.getStorageDelegate(), e);
            return null;
        }
    }

    private void writeStorageDelegateValue(String propertyName, String propertyValue) {
        try {
            this.getStorageDelegate().writeProperty(propertyName, propertyValue);
        } catch (Exception e) {
            log.warn("Cannot update stored preferences value for property '{}' into storage: {}", propertyName, this.getStorageDelegate(), e);
        }
    }

    public StorageDelegate getStorageDelegate() {
        return this.storageDelegate;
    }
    public void setStorageDelegate(StorageDelegate storageDelegate) {
        this.storageDelegate = storageDelegate;
    }

    private Map<String, StringProperty> getProperties() {
        return this.properties;
    }
    private void setProperties(Map<String, StringProperty> properties) {
        this.properties = properties;
    }

    private interface StorageDelegate {

        static StorageDelegate createInstance() {
            return new KeychainStorageDelegate();
        }

        String loadProperty(String propertyName) throws Exception;

        void writeProperty(String propertyName, String propertyValue) throws Exception;

    }

    private static class KeychainStorageDelegate implements StorageDelegate {

        private String applicationName = null;
        private OSXKeychain keychain = null;

        private KeychainStorageDelegate() {
            try {
                this.setApplicationName(SecureBackupPreferences.class.getPackageName());
                this.setKeychain(OSXKeychain.getInstance());
            } catch (OSXKeychainException e) {
                throw new RuntimeException("Cannot access macOS keychain", e);
            }
        }

        @Override
        public String loadProperty(String propertyName) throws Exception {
            return this.getKeychain().findGenericPassword(this.getApplicationName(), propertyName).orElse(null);
        }

        @Override
        public void writeProperty(String propertyName, String newPropertyValue) throws Exception {
            Optional<String> existingPasswordValue = this.getKeychain().findGenericPassword(this.getApplicationName(), propertyName);
            if (StringUtils.isEmpty(newPropertyValue) && existingPasswordValue.isPresent()) {
                this.getKeychain().deleteGenericPassword(this.getApplicationName(), propertyName);
            } else if (StringUtils.isNotEmpty(newPropertyValue) && existingPasswordValue.isEmpty()) {
                this.getKeychain().addGenericPassword(this.getApplicationName(), propertyName, newPropertyValue);
            } else if (StringUtils.isNotEmpty(newPropertyValue) && existingPasswordValue.isPresent() && !Objects.equals(existingPasswordValue.get(), newPropertyValue)) {
                this.getKeychain().modifyGenericPassword(this.getApplicationName(), propertyName, newPropertyValue);
            }
        }

        public String getApplicationName() {
            return this.applicationName;
        }
        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        private OSXKeychain getKeychain() {
            return this.keychain;
        }
        private void setKeychain(OSXKeychain keychain) {
            this.keychain = keychain;
        }

    }

}
