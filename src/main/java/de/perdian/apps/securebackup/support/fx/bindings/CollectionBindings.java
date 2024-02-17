package de.perdian.apps.securebackup.support.fx.bindings;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

public class CollectionBindings {

    public static <T> void bindCollectionWithinFxThread(ObservableList<T> sourceCollection, List<T> targetCollection) {
        sourceCollection.addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                Platform.runLater(() -> targetCollection.removeAll(change.getRemoved()));
                Platform.runLater(() -> targetCollection.addAll(change.getAddedSubList()));
            }
        });
    }

}
