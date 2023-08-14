package de.perdian.apps.securebackup.fx;

import de.perdian.apps.securebackup.fx.model.SourceModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;

public class SecureBackupModel {

    private final ObjectProperty<Path> targetBaseDirectory = new SimpleObjectProperty<>();
    private final ObservableList<SourceModel> sources = FXCollections.observableArrayList();

    public ObjectProperty<Path> targetBaseDirectoryProperty() {
        return targetBaseDirectory;
    }

    public ObservableList<SourceModel> getSources() {
        return sources;
    }

}
