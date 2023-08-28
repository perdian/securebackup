package de.perdian.support.fx.model;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ModelBuilder<T> {

    private static final Logger log = LoggerFactory.getLogger(ModelBuilder.class);

    private Class<T> type = null;
    private Supplier<? extends T> initialModelSupplier = null;
    private List<ModelProperty> properties = null;

    public ModelBuilder(Class<T> type) {
        this(type, new InitialModelFromDefaultClassConstructorSupplier<>(type));
    }

    public ModelBuilder(Class<T> type, Supplier<? extends T> initialModelSupplier) {
        this.setType(type);
        this.setInitialModelSupplier(initialModelSupplier);
        this.setProperties(new ArrayList<>());
        this.appendProperties(type, root -> root,"");
    }

    private void appendProperties(Class<?> type, Function<Object, Object> rootToObjectFunction, String rootToObjectPath) {
        for (Class<?> currentType = type; currentType != null && !Object.class.equals(currentType); currentType = currentType.getSuperclass()) {
            for (Field currentField : currentType.getDeclaredFields()) {
                int currentFieldModifiers = currentField.getModifiers();
                if (!Modifier.isStatic(currentFieldModifiers) && !Modifier.isTransient(currentFieldModifiers)) {

                    String currentFieldName = rootToObjectPath + (StringUtils.isEmpty(rootToObjectPath) ? "" : ".") + currentField.getName();
                    if (Property.class.isAssignableFrom(currentField.getType())) {
                        this.getProperties().add(new ModelProperty(currentFieldName, currentField, rootToObjectFunction, new ModelPropertyHandler.FxPropertyHandler()));
                    } else if (ObservableList.class.isAssignableFrom(currentField.getType())) {
                        this.getProperties().add(new ModelProperty(currentFieldName, currentField, rootToObjectFunction, new ModelPropertyHandler.ListPropertyHandler()));
                    } else {
                        Function<Object, Object> newRootToObjectFunction = rootToObjectFunction.andThen(object -> {
                            try {
                                currentField.setAccessible(true);
                                return currentField.get(object);
                            } catch (ReflectiveOperationException e) {
                                throw new IllegalArgumentException("Cannot get value from field '" + currentField.getName() + "' inside of class '" + type + "'", e);
                            }
                        });
                        this.appendProperties(currentField.getType(), newRootToObjectFunction, currentFieldName);
                    }

                }
            }
        }
    }

    public T createModel(Path modelFile) {
        T modelInstance = this.getInitialModelSupplier().get();
        this.populateModelFromFile(modelInstance, modelFile);
        this.getProperties().forEach(property -> property.addChangeListener(modelInstance, () -> this.saveModelToFile(modelInstance, modelFile)));
        return modelInstance;
    }

    void populateModelFromStorageModel(T modelInstance, Map<String, Object> storageValues) {
        for (ModelProperty property : this.getProperties()) {
            Object propertyValue = property.readValue(modelInstance);
            Object storageValue = storageValues.get(property.getPath());
            property.getHandler().updateFromStorageValue(propertyValue, storageValue);
        }
    }

    @SuppressWarnings("unchecked")
    void populateModelFromFile(T modelInstance, Path modelFile) {
        if (Files.exists(modelFile)) {
            try (ObjectInputStream objectStream = new ObjectInputStream(new GZIPInputStream(Files.newInputStream(modelFile)))) {
                this.populateModelFromStorageModel(modelInstance, (Map<String, Object>)objectStream.readObject());
            } catch (Exception e) {
                log.error("Cannot load model for class '{}' from file at: {}", this.getType(), modelFile, e);
            }
        }
    }

    void saveModelToFile(T model, Path modelFile) {
        log.trace("Writing model of class '{}' into file at: {}", model.getClass().getName(), modelFile);
        try {
            try (ObjectOutputStream objectStream = new ObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(modelFile)))) {
                objectStream.writeObject(this.extractStorageValues(model));
            }
        } catch (Exception e) {
            log.error("Cannot save model for class '{}' into file at: {}", model.getClass().getName(), modelFile, e);
        }
    }

    Map<String, Object> extractStorageValues(T model) {
        Map<String, Object> storageValues = new HashMap<>();
        for (ModelProperty property : this.getProperties()) {
            Object propertyValue = property.readValue(model);
            Object storageValue = property.getHandler().createStorageValue(propertyValue);
            storageValues.put(property.getPath(), storageValue);
        }
        return storageValues;
    }

    private static class InitialModelFromDefaultClassConstructorSupplier<T> implements Supplier<T> {

        private Class<T> type = null;

        private InitialModelFromDefaultClassConstructorSupplier(Class<T> type) {
            this.setType(type);
        }

        public T get() {
            try {
                Constructor<T> modelConstructor = this.getType().getDeclaredConstructor();
                modelConstructor.setAccessible(true);
                return modelConstructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Cannot create model class: " + this.getType(), e);
            }
        }

        private Class<T> getType() {
            return this.type;
        }
        private void setType(Class<T> type) {
            this.type = type;
        }

    }

    public Class<T> getType() {
        return this.type;
    }
    private void setType(Class<T> type) {
        this.type = type;
    }

    private Supplier<? extends T> getInitialModelSupplier() {
        return this.initialModelSupplier;
    }
    private void setInitialModelSupplier(Supplier<? extends T> initialModelSupplier) {
        this.initialModelSupplier = initialModelSupplier;
    }

    List<ModelProperty> getProperties() {
        return this.properties;
    }
    private void setProperties(List<ModelProperty> properties) {
        this.properties = properties;
    }

}
