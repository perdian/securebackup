package de.perdian.apps.securebackup.modules.preferences;

import de.perdian.apps.securebackup.support.fx.converters.PathStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

public class Preferences {

    private static final Logger log = LoggerFactory.getLogger(Preferences.class);

    private PreferencesStorageDelegate storageDelegate = null;
    private ObservableMap<String, StringProperty> properties = null;

    public Preferences(PreferencesStorageDelegate storageDelegate) {
        this.setStorageDelegate(storageDelegate);
        this.setProperties(FXCollections.observableHashMap());
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

    public PreferencesStorageDelegate getStorageDelegate() {
        return this.storageDelegate;
    }
    public void setStorageDelegate(PreferencesStorageDelegate storageDelegate) {
        this.storageDelegate = storageDelegate;
    }

    private ObservableMap<String, StringProperty> getProperties() {
        return this.properties;
    }
    private void setProperties(ObservableMap<String, StringProperty> properties) {
        this.properties = properties;
    }

}
