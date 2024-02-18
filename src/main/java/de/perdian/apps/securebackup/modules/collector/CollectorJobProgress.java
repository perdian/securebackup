package de.perdian.apps.securebackup.modules.collector;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectorJobProgress<T> {

    private final List<CollectorJobProgressListener> listeners = new CopyOnWriteArrayList<>();
    private final ObjectProperty<T> currentItem = new SimpleObjectProperty<>();
    private final ObservableList<T> backlogItems = FXCollections.observableArrayList();
    private final ObservableList<T> completedItems = FXCollections.observableArrayList();

    public void initializeItems(List<T> items) {
        this.currentItem.setValue(null);
        this.backlogItems.setAll(items);
        this.completedItems.clear();
    }

    void fireProgress(Double progress, String message, Throwable exception) {
        Platform.runLater(() -> this.getListeners().forEach(listener -> listener.onProgress(progress, message, exception)));
    }
    public void addListener(CollectorJobProgressListener listener) {
        this.getListeners().add(listener);
    }
    public List<CollectorJobProgressListener> getListeners() {
        return this.listeners;
    }

    public ObjectProperty<T> currentItemProperty() {
        return this.currentItem;
    }

    public ObservableList<T> getBacklogItems() {
        return this.backlogItems;
    }

    public ObservableList<T> getCompletedItems() {
        return this.completedItems;
    }

}
