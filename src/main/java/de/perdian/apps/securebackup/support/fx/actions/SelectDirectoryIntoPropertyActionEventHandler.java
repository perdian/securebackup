package de.perdian.apps.securebackup.support.fx.actions;

import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class SelectDirectoryIntoPropertyActionEventHandler implements EventHandler<ActionEvent> {

    private Property<Path> targetProperty = null;

    public SelectDirectoryIntoPropertyActionEventHandler(Property<Path> targetProperty) {
        this.setTargetProperty(targetProperty);
    }

    @Override
    public void handle(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select directory");

        Path currentValue = this.getTargetProperty().getValue();
        if (currentValue != null && Files.isDirectory(currentValue) && FileSystems.getDefault().equals(currentValue.getFileSystem())) {
            directoryChooser.setInitialDirectory(currentValue.toFile());
        }

        Window ownerWindow = (event != null && event.getSource() instanceof Node node) ? node.getScene().getWindow() : null;
        File selectedFile = directoryChooser.showDialog(ownerWindow);
        if (selectedFile != null && selectedFile.isDirectory()) {
            this.getTargetProperty().setValue(selectedFile.toPath());
        }

    }

    private Property<Path> getTargetProperty() {
        return this.targetProperty;
    }
    private void setTargetProperty(Property<Path> targetProperty) {
        this.targetProperty = targetProperty;
    }

}
