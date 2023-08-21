package de.perdian.support.fx.model;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class ModelClassMetadata<T> {

    private static final Logger log = LoggerFactory.getLogger(ModelClassMetadata.class);

    private Class<T> modelClass = null;
    private Map<Field, ModelProperty> properties = null;

    ModelClassMetadata(Class<T> modelClass) {

        Map<Field, ModelProperty> properties = new HashMap<>();
        for (Class<?> currentClass = modelClass; !Object.class.equals(currentClass); currentClass = currentClass.getSuperclass()) {
            for (Field currentField : currentClass.getDeclaredFields()) {
                int currentFieldModifiers = currentField.getModifiers();
                if (!Modifier.isStatic(currentFieldModifiers) && !Modifier.isTransient(currentFieldModifiers)) {
                    ModelProperty property = ModelClassMetadata.buildProperty(currentClass, currentField);
                    if (property != null) {
                        properties.put(currentField, property);
                    }
                }
            }
        }

        this.setModelClass(modelClass);
        this.setProperties(properties);

    }

    private static ModelProperty buildProperty(Class<?> owningClass, Field field) {
        Class<?> fieldType = field.getType();
        if (Property.class.isAssignableFrom(fieldType)) {
            return new ModelProperty.PropertyModelProperty();
        } else if (ObservableList.class.isAssignableFrom(fieldType)) {
            return new ModelProperty.ListModelProperty();
        } else {
            return new ModelProperty.ObjectModelProperty(fieldType);
        }
    }

    private ModelClassMetadata() {
    }

    T createModel(Path modelFile) {
        T modelInstance = this.createModelInstance(modelFile);
        this.adChangeListenerToProperties(modelInstance, (o, oldValue, newValue) -> this.saveModelToFile(modelInstance, modelFile));
        return modelInstance;
    }

    private T createModelInstance(Path modelFile) {
        T modelInstance = this.loadModelFromFile(modelFile);
        if (modelInstance == null) {
            try {
                Constructor<T> modelConstructor = this.getModelClass().getDeclaredConstructor();
                modelConstructor.setAccessible(true);
                modelInstance = modelConstructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Cannot create model class: " + this.getModelClass(), e);
            }
        }
        return modelInstance;
    }

    T loadModelFromFile(Path modelFile) {
        try {
            if (Files.exists(modelFile)) {
                throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            log.error("Cannot load model for class '{}' from file at: {}", modelClass.getName(), modelFile, e);
        }
        return null;
    }

    void adChangeListenerToProperties(T modelInstance, ChangeListener<Object> changeListener) {
        for (Map.Entry<Field, ModelProperty> propertyEntry : this.getProperties().entrySet()) {
            try {
                Field propertyField = propertyEntry.getKey();
                propertyField.setAccessible(true);
                Object propertyValue = propertyField.get(modelInstance);
                propertyEntry.getValue().addChangeListener(propertyValue, changeListener);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Cannot retrieve field value for field '" + propertyEntry.getKey().getName() + "' from object of class '" + modelInstance.getClass().getName() + "'", e);
            }
        }
    }

    void saveModelToFile(T model, Path modelFile) {
        log.trace("Writing model of class '{}' into file at: {}", model.getClass().getName(), modelFile);
        try {
            log.error("TODO: IMPLEMENT!");
        } catch (Exception e) {
            log.error("Cannot save model for class '{}' into file at: {}", model.getClass().getName(), modelFile, e);
        }
    }

    Class<T> getModelClass() {
        return this.modelClass;
    }
    private void setModelClass(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    Map<Field, ModelProperty> getProperties() {
        return this.properties;
    }
    private void setProperties(Map<Field, ModelProperty> properties) {
        this.properties = properties;
    }

}
