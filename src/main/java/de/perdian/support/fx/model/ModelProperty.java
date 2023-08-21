package de.perdian.support.fx.model;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

interface ModelProperty {

    void addChangeListener(Object propertyValue, ChangeListener<Object> changeListener);

    class PropertyModelProperty implements ModelProperty {

        @Override
        public void addChangeListener(Object propertyValue, ChangeListener<Object> changeListener) {
            ((Property<?>)propertyValue).addListener(changeListener);
        }

    }

    class ListModelProperty implements ModelProperty {

        @Override
        public void addChangeListener(Object propertyValue, ChangeListener<Object> changeListener) {
            ObservableList<Object> listValue = (ObservableList<Object>)propertyValue;
            listValue.addListener((ListChangeListener.Change<? extends Object> change) -> changeListener.changed(null, propertyValue, propertyValue));
        }

    }

    class ObjectModelProperty implements ModelProperty {

        private ModelClassMetadata propertyClassMetadata = null;

        ObjectModelProperty(Class<?> propertyClass) {
            this.setPropertyClassMetadata(new ModelClassMetadata(propertyClass));
        }

        @Override
        public void addChangeListener(Object propertyValue, ChangeListener<Object> changeListener) {
            this.getPropertyClassMetadata().adChangeListenerToProperties(propertyValue, changeListener);
        }

        private ModelClassMetadata getPropertyClassMetadata() {
            return this.propertyClassMetadata;
        }
        private void setPropertyClassMetadata(ModelClassMetadata propertyClassMetadata) {
            this.propertyClassMetadata = propertyClassMetadata;
        }

    }

}
