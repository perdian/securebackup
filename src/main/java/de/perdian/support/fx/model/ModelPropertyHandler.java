package de.perdian.support.fx.model;

import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

interface ModelPropertyHandler {

    void addChangeListener(Object propertyValue, Runnable changeListener);
    Object createStorageValue(Object propertyValue);
    void updateFromStorageValue(Object propertyValue, Object storageValue);

    class FxPropertyHandler implements ModelPropertyHandler {

        @Override
        public void addChangeListener(Object propertyValue, Runnable changeListener) {
            ((Property<?>)propertyValue).addListener((o, oldValue, newValue) -> changeListener.run());
        }

        @Override
        public Object createStorageValue(Object propertyValue) {
            return ((Property<?>)propertyValue).getValue();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void updateFromStorageValue(Object propertyValue, Object storageValue) {
            ((Property<Object>)propertyValue).setValue(storageValue);
        }

    }

    class ListPropertyHandler implements ModelPropertyHandler {

        @SuppressWarnings("unchecked")
        @Override
        public void addChangeListener(Object propertyValue, Runnable changeListener) {
            ObservableList<Object> propertyValueList = (ObservableList<Object>)propertyValue;
            propertyValueList.forEach(listItem -> this.addChangeListenerToListItem(listItem, changeListener));
            propertyValueList.addListener((ListChangeListener.Change<?> change) -> {
                while (change.next()) {
                    change.getAddedSubList().forEach(removedListItem -> this.addChangeListenerToListItem(removedListItem, changeListener));
                }
                changeListener.run();
            });
        }

        private void addChangeListenerToListItem(Object listItem, Runnable changeListener) {
            if (listItem != null) {
                ModelBuilder<?> listItemModelBuilder = new ModelBuilder<>(listItem.getClass());
                listItemModelBuilder.getProperties().forEach(listItemProperty -> listItemProperty.addChangeListener(listItem, changeListener));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object createStorageValue(Object propertyValue) {
            List<Object> propertyValueList = (List<Object>)propertyValue;
            List<ListPropertyStorageWrapper> storageValueList = new ArrayList<>(propertyValueList.size());
            for (Object listItem : propertyValueList) {
                if (SimpleListPropertyStorageWrapper.isSimpleValue(listItem)) {
                    storageValueList.add(new SimpleListPropertyStorageWrapper(listItem));
                } else {
                    Class<Object> listItemClass = (Class<Object>) listItem.getClass();
                    ModelBuilder<Object> listItemModelBuilder = new ModelBuilder<>(listItemClass);
                    Map<String, Object> listItemStorageValues = listItemModelBuilder.extractStorageValues(listItem);
                    ComplexListPropertyStorageWrapper listItemStorageWrapper = new ComplexListPropertyStorageWrapper();
                    listItemStorageWrapper.setItemClass(listItemClass);
                    listItemStorageWrapper.setItemStorageValues(listItemStorageValues);
                    storageValueList.add(listItemStorageWrapper);
                }
            }
            return storageValueList;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void updateFromStorageValue(Object propertyValue, Object storageValue) {
            ObservableList<Object> propertyValueList = (ObservableList<Object>)propertyValue;
            List<ListPropertyStorageWrapper> storageWrapperList = (List<ListPropertyStorageWrapper>)storageValue;
            List<Object> propertyValues = new ArrayList<>(storageWrapperList.size());
            for (ListPropertyStorageWrapper storageWrapper : storageWrapperList) {
                propertyValues.add(storageWrapper.toOriginalObject());
            }
            propertyValueList.setAll(propertyValues);
        }

        interface ListPropertyStorageWrapper extends Serializable {

            Object toOriginalObject();

        }

        static class SimpleListPropertyStorageWrapper implements ListPropertyStorageWrapper {

            private Object itemValue = null;

            SimpleListPropertyStorageWrapper(Object itemValue) {
                this.setItemValue(itemValue);
            }

            @Override
            public Object toOriginalObject() {
                return this.getItemValue();
            }

            static boolean isSimpleValue(Object value) {
                return value == null
                    || value.getClass().isPrimitive()
                    || value.getClass().isArray()
                    || value.getClass().getPackageName().startsWith("java.")
                ;
            }

            private Object getItemValue() {
                return itemValue;
            }
            private void setItemValue(Object itemValue) {
                this.itemValue = itemValue;
            }

        }

        static class ComplexListPropertyStorageWrapper implements ListPropertyStorageWrapper {

            @Serial
            private static final long serialVersionUID = 1L;

            private Class<?> itemClass = null;
            private Map<String, Object> itemStorageValues = null;

            @SuppressWarnings("unchecked")
            @Override
            public Object toOriginalObject() {
                try {
                    Object objectInstance = this.getItemClass().getDeclaredConstructor().newInstance();
                    Class<Object> objectClass = (Class<Object>)this.getItemClass();
                    ModelBuilder<Object> modelBuilder = new ModelBuilder<>(objectClass);
                    modelBuilder.populateModelFromStorageModel(objectInstance, this.getItemStorageValues());
                    return objectInstance;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Cannot restore object of class: " + this.getItemClass().getName(), e);
                }
            }

            private Class<?> getItemClass() {
                return itemClass;
            }
            private void setItemClass(Class<?> itemClass) {
                this.itemClass = itemClass;
            }

            private Map<String, Object> getItemStorageValues() {
                return itemStorageValues;
            }
            private void setItemStorageValues(Map<String, Object> itemStorageValues) {
                this.itemStorageValues = itemStorageValues;
            }

        }

    }

}
