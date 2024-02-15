package de.perdian.apps.securebackup.support.fx.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectFileActionEventHandler implements EventHandler<ActionEvent> {

    private Consumer<Path> targetPathConsumer = null;
    private Supplier<Path> currentPathSupplier = null;
    private SelectFileMode selectFileMode = null;

    public SelectFileActionEventHandler(Consumer<Path> targetPathConsumer, Supplier<Path> currentPathSupplier, SelectFileMode selectFileMode) {
        this.setTargetPathConsumer(targetPathConsumer);
        this.setCurrentPathSupplier(currentPathSupplier == null ? () -> null : currentPathSupplier);
        this.setSelectFileMode(selectFileMode);
    }

    @Override
    public void handle(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");

        Path currentValue = this.getCurrentPathSupplier().get();
        if (currentValue != null && FileSystems.getDefault().equals(currentValue.getFileSystem())) {
            File currentFile = currentValue.toFile();
            if (currentFile.isDirectory()) {
                fileChooser.setInitialDirectory(currentValue.toFile());
            } else {
                fileChooser.setInitialDirectory(currentValue.toFile().getParentFile());
            }
        }

        Window ownerWindow = (event != null && event.getSource() instanceof Node node) ? node.getScene().getWindow() : null;
        File selectedFile = SelectFileMode.WRITE.equals(this.getSelectFileMode()) ? fileChooser.showSaveDialog(ownerWindow) : fileChooser.showOpenDialog(ownerWindow);
        if (selectedFile != null && !selectedFile.isDirectory()) {
            this.getTargetPathConsumer().accept(selectedFile.toPath());
        }

    }

    public enum SelectFileMode {

        READ,
        WRITE;

    }

    private Consumer<Path> getTargetPathConsumer() {
        return this.targetPathConsumer;
    }
    private void setTargetPathConsumer(Consumer<Path> targetPathConsumer) {
        this.targetPathConsumer = targetPathConsumer;
    }

    private Supplier<Path> getCurrentPathSupplier() {
        return this.currentPathSupplier;
    }
    private void setCurrentPathSupplier(Supplier<Path> currentPathSupplier) {
        this.currentPathSupplier = currentPathSupplier;
    }

    private SelectFileMode getSelectFileMode() {
        return this.selectFileMode;
    }
    private void setSelectFileMode(SelectFileMode selectFileMode) {
        this.selectFileMode = selectFileMode;
    }

}
