package de.perdian.apps.securebackup.modules.sources;

import de.perdian.apps.securebackup.support.fx.actions.SelectFileActionEventHandler;
import de.perdian.apps.securebackup.support.fx.converters.PathStringConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import java.nio.file.Path;
import java.util.function.Consumer;

public class SourceCollectionPane extends GridPane {

    public SourceCollectionPane(SourceCollection sourceCollection) {

        Pane fileAccessPane = SourceCollectionPane.createFileAccessPane(sourceCollection);
        GridPane.setHgrow(fileAccessPane, Priority.ALWAYS);

        Pane packagesPane = SourceCollectionPane.createPackagesPane(sourceCollection);
        GridPane.setHgrow(packagesPane, Priority.ALWAYS);
        GridPane.setVgrow(packagesPane, Priority.ALWAYS);

        this.add(fileAccessPane, 0, 0, 1, 1);
        this.add(packagesPane, 0, 1, 1, 1);
        this.setVgap(10);

    }

    private static Pane createFileAccessPane(SourceCollection sourceCollection) {

        TextFormatter<Path> storageFileFormatter = new TextFormatter<Path>(new PathStringConverter());
        storageFileFormatter.valueProperty().bindBidirectional(sourceCollection.storageFileProperty());
        TextField storageFileField = new TextField();
        storageFileField.setEditable(false);
        storageFileField.setTextFormatter(storageFileFormatter);
        Label storageFileLabel = new Label("Storage file");
        storageFileLabel.setLabelFor(storageFileField);

        Consumer<Path> storageFileLoadConsumer = path -> sourceCollection.loadFromFile(path);
        Button storageFileLoadButton = new Button("Load", new FontIcon(MaterialDesignF.FILE_DOWNLOAD));
        storageFileLoadButton.setOnAction(new SelectFileActionEventHandler(storageFileLoadConsumer, () -> sourceCollection.storageFileProperty().getValue(), SelectFileActionEventHandler.SelectFileMode.READ));
        Consumer<Path> storageFileWriteConsumer = path -> sourceCollection.saveIntoFile(path);
        Button storageFileSaveButton = new Button("Save", new FontIcon(MaterialDesignF.FILE_UPLOAD));
        storageFileSaveButton.setOnAction(new SelectFileActionEventHandler(storageFileWriteConsumer, () -> sourceCollection.storageFileProperty().getValue(), SelectFileActionEventHandler.SelectFileMode.WRITE));
        HBox storageFilePane = new HBox(2, storageFileField, storageFileLoadButton, storageFileSaveButton);
        HBox.setHgrow(storageFileField, Priority.ALWAYS);
        GridPane.setHgrow(storageFilePane, Priority.ALWAYS);

        GridPane fileAccessPane = new GridPane();
        fileAccessPane.setHgap(10);
        fileAccessPane.add(storageFileLabel, 0, 0, 1, 1);
        fileAccessPane.add(storageFilePane, 1, 0, 1, 1);
        return fileAccessPane;

    }

    private static Pane createPackagesPane(SourceCollection sourceCollection) {

        Button addPackageButton = new Button("Add package", new FontIcon(MaterialDesignP.PLUS_BOX));
        addPackageButton.setOnAction(event -> sourceCollection.getPackages().add(new SourcePackage()));
        HBox actionsPane = new HBox(5, addPackageButton);
        actionsPane.setAlignment(Pos.CENTER);
        GridPane.setHgrow(actionsPane, Priority.ALWAYS);

        SourcePackageListPane packageListPane = new SourcePackageListPane(sourceCollection.getPackages());
        packageListPane.setMaxWidth(Double.MAX_VALUE);
        packageListPane.setPadding(new Insets(10, 10, 10, 10));
        ScrollPane packageListScrollPane = new ScrollPane(packageListPane);
        packageListScrollPane.setFitToWidth(true);
        packageListScrollPane.setFocusTraversable(false);
        packageListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        packageListScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        GridPane.setHgrow(packageListScrollPane, Priority.ALWAYS);
        GridPane.setVgrow(packageListScrollPane, Priority.ALWAYS);

        GridPane packagesPane = new GridPane();
        packagesPane.setVgap(10);
        packagesPane.add(actionsPane, 0, 0, 1, 1);
        packagesPane.add(packageListScrollPane, 0, 1, 1, 1);
        return packagesPane;

    }


}
