package de.perdian.apps.securebackup.modules.sources;

import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

class SourcePackagePreviewPane extends GridPane {

    private static final Logger log = LoggerFactory.getLogger(SourcePackagePreviewPane.class);

    private boolean keepReloading = false;
    private boolean reloadActive = false;
    private BorderPane contentPane = null;

    SourcePackagePreviewPane(SourcePackage sourcePackage) {

        BorderPane contentPane = new BorderPane();
        GridPane.setVgrow(contentPane, Priority.ALWAYS);
        GridPane.setHgrow(contentPane, Priority.ALWAYS);
        this.setContentPane(contentPane);

        this.setVgap(2);
        this.add(ComponentFactory.createSmallLabel("Preview"), 0, 0, 1, 1);
        this.add(contentPane, 0, 1, 1, 1);

        this.reloadPreview(sourcePackage);
        sourcePackage.addChangeListener((o, oldValue, newValue) -> this.reloadPreview(sourcePackage));

    }

    private void reloadPreview(SourcePackage sourcePackage) {
        synchronized(this) {
            this.setKeepReloading(true);
            if (!this.isReloadActive()) {
                this.setReloadActive(true);
                Thread.ofVirtual().start(() -> {
                    while (true) {
                        synchronized(SourcePackagePreviewPane.this) {
                            if (!SourcePackagePreviewPane.this.isKeepReloading()) {
                                SourcePackagePreviewPane.this.setReloadActive(false);
                                break;
                            } else {
                                SourcePackagePreviewPane.this.setKeepReloading(false);
                            }
                        }
                        this.reloadPreviewOffloaded(sourcePackage);
                    }
                });
            }
        }
    }

    private void reloadPreviewOffloaded(SourcePackage sourcePackage) {

        log.debug("Reloading preview for package: {}", sourcePackage);
        this.updateContentPaneWithMessage("Reloading package preview...", MaterialDesignR.RELOAD);

        try {

            TreeItem<String> treeRootItem = new TreeItem<>();
            List<SourceFileCollection> sourceFileCollections = sourcePackage.createSourceFileCollections();
            for (SourceFileCollection sourceFileCollection : sourceFileCollections) {
                List<String> sourceFileCollectionPath = List.of(sourceFileCollection.getName().split("/"));
                CollectionTreeItem sourceFileCollectionItem = this.appendSourceFileCollectionItem(sourceFileCollectionPath, treeRootItem);
                for (SourceFile sourceFile : sourceFileCollection.getFiles()) {
                    List<String> sourceFilePath = List.of(sourceFile.getRelativeFileName().split("/"));
                    this.appendSourceFileItem(sourceFilePath, sourceFileCollectionItem);
                }
            }

            TreeView<String> treeView = new TreeView<>(treeRootItem);
            treeView.setPrefHeight(0);
            treeView.setShowRoot(false);
            this.updateContentPane(treeView);

        } catch (Exception e) {
            this.updateContentPaneWithMessage("Cannot load preview", MaterialDesignA.ALERT);
        }

    }

    private CollectionTreeItem appendSourceFileCollectionItem(List<String> collectionNamePath, TreeItem<String> parentItem) {

        CollectionTreeItem collectionNameItem = parentItem.getChildren()
            .stream()
            .filter(item -> (item instanceof CollectionTreeItem))
            .map(item -> (CollectionTreeItem)item)
            .filter(childItem -> Objects.equals(collectionNamePath.get(0), childItem.getValue()))
            .findFirst()
            .orElse(null);

        if (collectionNameItem == null) {
            collectionNameItem = new CollectionTreeItem();
            collectionNameItem.setExpanded(true);
            collectionNameItem.setValue(collectionNamePath.get(0));
            collectionNameItem.setGraphic(new FontIcon(MaterialDesignA.ARCHIVE));
            parentItem.getChildren().add(collectionNameItem);
        }

        if (collectionNamePath.size() > 1) {
            return this.appendSourceFileCollectionItem(collectionNamePath.subList(1, collectionNamePath.size()), collectionNameItem);
        } else {
            return collectionNameItem;
        }

    }

    private PathTreeItem appendSourceFileItem(List<String> fileNamePath, TreeItem<String> parentItem) {

        PathTreeItem pathNameItem = parentItem.getChildren()
            .stream()
            .filter(item -> (item instanceof PathTreeItem))
            .map(item -> (PathTreeItem)item)
            .filter(childItem -> Objects.equals(fileNamePath.get(0), childItem.getValue()))
            .findFirst()
            .orElse(null);

        if (pathNameItem == null) {
            pathNameItem = new PathTreeItem();
            pathNameItem.setValue(fileNamePath.get(0));
            pathNameItem.setGraphic(new FontIcon(fileNamePath.size() > 1 ? MaterialDesignF.FOLDER_OUTLINE : MaterialDesignF.FILE_OUTLINE));
            parentItem.getChildren().add(pathNameItem);
        }

        if (fileNamePath.size() > 1) {
            return this.appendSourceFileItem(fileNamePath.subList(1, fileNamePath.size()), pathNameItem);
        } else {
            return pathNameItem;
        }

    }

    private void updateContentPaneWithMessage(String message, Ikon icon) {
        Label messageLabel = new Label(message);
        messageLabel.setGraphic(new FontIcon(icon));
        messageLabel.setAlignment(Pos.CENTER);
        BorderPane messagePane = new BorderPane(messageLabel);
        messagePane.setStyle("-fx-border-color: lightgray;");
        this.updateContentPane(messagePane);
    }

    private static class CollectionTreeItem extends TreeItem<String> {
    }

    private static class PathTreeItem extends TreeItem<String> {
    }

    private void updateContentPane(Node newContent) {
        Platform.runLater(() -> this.getContentPane().setCenter(newContent));
    }

    private TreeItem<Object> lookupSourceFileCollectionParentItem(String name, TreeItem<Object> parentItem) {
        return parentItem;
    }

    private BorderPane getContentPane() {
        return this.contentPane;
    }
    private void setContentPane(BorderPane contentPane) {
        this.contentPane = contentPane;
    }

    private boolean isReloadActive() {
        return this.reloadActive;
    }
    private void setReloadActive(boolean reloadActive) {
        this.reloadActive = reloadActive;
    }

    private boolean isKeepReloading() {
        return this.keepReloading;
    }
    private void setKeepReloading(boolean keepReloading) {
        this.keepReloading = keepReloading;
    }

}
