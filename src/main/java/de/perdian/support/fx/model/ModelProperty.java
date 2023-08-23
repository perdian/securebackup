package de.perdian.support.fx.model;

import java.lang.reflect.Field;
import java.util.function.Function;

class ModelProperty {

    private String path = null;
    private Field propertValueField = null;
    private Function<Object, Object> modelToPropertyValueParentFunction = null;
    private ModelPropertyHandler handler = null;

    ModelProperty(String path, Field valueField, Function<Object, Object> parentToValueFunction, ModelPropertyHandler handler) {
        this.setPath(path);
        this.setPropertValueField(valueField);
        this.setModelToPropertyValueParentFunction(parentToValueFunction);
        this.setHandler(handler);
    }

    Object readValue(Object modelInstance) {
        Object propertyValueParent = this.getModelToPropertyValueParentFunction().apply(modelInstance);
        Field propertyValueField = this.getPropertValueField();
        try {
            propertyValueField.setAccessible(true);
            return propertValueField.get(propertyValueParent);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot access value for field '" + propertyValueField.getName() + "' in class '" + propertyValueParent.getClass().getName() + "'", e);
        }
    }

    void addChangeListener(Object modelInstance, Runnable changeListener) {
        Object propertyValue = this.readValue(modelInstance);
        this.getHandler().addChangeListener(propertyValue, changeListener);
    }

    String getPath() {
        return path;
    }
    private void setPath(String path) {
        this.path = path;
    }

    Field getPropertValueField() {
        return propertValueField;
    }
    private void setPropertValueField(Field propertValueField) {
        this.propertValueField = propertValueField;
    }

    Function<Object, Object> getModelToPropertyValueParentFunction() {
        return modelToPropertyValueParentFunction;
    }
    private void setModelToPropertyValueParentFunction(Function<Object, Object> modelToPropertyValueParentFunction) {
        this.modelToPropertyValueParentFunction = modelToPropertyValueParentFunction;
    }

    ModelPropertyHandler getHandler() {
        return this.handler;
    }
    private void setHandler(ModelPropertyHandler handler) {
        this.handler = handler;
    }

}
