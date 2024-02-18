package de.perdian.apps.securebackup.modules.input;

import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import de.perdian.apps.securebackup.support.fx.handlers.SelectDirectoryActionEventHandler;
import de.perdian.apps.securebackup.support.fx.properties.converters.LinesStringConverter;
import de.perdian.apps.securebackup.support.fx.properties.converters.PathStringConverter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class InputScannerCollectionNodeFactory {

    public static Region createInputScannerCollectionNode(InputScannerCollection inputScannerCollection) {

        Region configurationNode = InputScannerCollectionNodeFactory.createInputScannerCollectionConfigurationNode(inputScannerCollection);
        GridPane.setHgrow(configurationNode, Priority.ALWAYS);

        Region inputScannersNode = InputScannerCollectionNodeFactory.createInputScannersNode(inputScannerCollection);
        GridPane.setHgrow(inputScannersNode, Priority.ALWAYS);
        GridPane.setVgrow(inputScannersNode, Priority.ALWAYS);

        GridPane inputScannerCollectionPane = new GridPane();
        inputScannerCollectionPane.setVgap(24);
        inputScannerCollectionPane.add(configurationNode, 0, 0, 1, 1);
        inputScannerCollectionPane.add(inputScannersNode, 0, 1, 1, 1);

        TitledPane inputScannerCollectionTitledPane = new TitledPane("Input scanners", inputScannerCollectionPane);
        inputScannerCollectionTitledPane.setMaxHeight(Double.MAX_VALUE);
        inputScannerCollectionTitledPane.setCollapsible(false);
        inputScannerCollectionTitledPane.setGraphic(new FontIcon(MaterialDesignF.FOLDER_SETTINGS_OUTLINE));
        return inputScannerCollectionTitledPane;

    }

    public static Region createInputScannerCollectionConfigurationNode(InputScannerCollection inputScannerCollection) {

        Region actionsNode = InputScannerCollectionNodeFactory.createInputScannerCollectionConfigurationActionsNode(inputScannerCollection);
        GridPane.setHgrow(actionsNode, Priority.ALWAYS);

        Button addInputScannerButton = new Button("Add new input scanner", new FontIcon(MaterialDesignP.PLUS_BOX));
        addInputScannerButton.setOnAction(InputScannerCollectionNodeFactory.createAddInputScannerActionEventHandler(inputScannerCollection));
        addInputScannerButton.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(addInputScannerButton, Priority.ALWAYS);

        GridPane configurationNode = new GridPane();
        configurationNode.add(actionsNode, 0, 0, 1, 1);
        configurationNode.add(addInputScannerButton, 0, 1, 1, 1);
        configurationNode.setHgap(12);
        configurationNode.setVgap(3);
        return configurationNode;

    }

    private static Region createInputScannerCollectionConfigurationActionsNode(InputScannerCollection inputScannerCollection) {

        TextField configurationFileField = ComponentFactory.createTextField(inputScannerCollection.configurationFileProperty(), new PathStringConverter());
        Label configurationFileLabel = new Label("Configuration file");
        configurationFileLabel.setLabelFor(configurationFileField);

        Button loadConfigurationButton = new Button("Load", new FontIcon(MaterialDesignF.FILE_DOWNLOAD));
        loadConfigurationButton.setOnAction(InputScannerCollectionNodeFactory.createSelectionConfigurationFileActionEventHandler(inputScannerCollection::loadFromFile, inputScannerCollection.configurationFileProperty().getValue(), FileChooser::showOpenDialog));
        Button saveConfigurationButton = new Button("Save", new FontIcon(MaterialDesignF.FILE_UPLOAD));
        saveConfigurationButton.setOnAction(InputScannerCollectionNodeFactory.createSelectionConfigurationFileActionEventHandler(inputScannerCollection::saveIntoFile, inputScannerCollection.configurationFileProperty().getValue(), FileChooser::showSaveDialog));

        HBox configurationFilePane = new HBox(3, configurationFileField, loadConfigurationButton, saveConfigurationButton);
        HBox.setHgrow(configurationFileField, Priority.ALWAYS);
        GridPane.setHgrow(configurationFilePane, Priority.ALWAYS);

        GridPane actionsPane = new GridPane();
        actionsPane.setHgap(12);
        actionsPane.add(configurationFileLabel, 0, 0, 1, 1);
        actionsPane.add(configurationFilePane, 1, 0, 1, 1);
        return actionsPane;

    }

    private static EventHandler<ActionEvent> createSelectionConfigurationFileActionEventHandler(Consumer<Path> pathConsumer, Path currentPath, BiFunction<FileChooser, Window, File> fileChooserFunction) {
        return event -> {

            FileChooser configurationFileChooser = new FileChooser();
            configurationFileChooser.setTitle("Select file");

            if (currentPath != null && FileSystems.getDefault().equals(currentPath.getFileSystem())) {
                configurationFileChooser.setInitialDirectory(currentPath.getParent().toFile());
            }

            Window ownerWindow = (event != null && event.getSource() instanceof Node node) ? node.getScene().getWindow() : null;
            File selectedFile = fileChooserFunction.apply(configurationFileChooser, ownerWindow);
            if (selectedFile != null && !selectedFile.isDirectory()) {
                pathConsumer.accept(selectedFile.toPath());
            }

        };
    }

    private static EventHandler<ActionEvent> createAddInputScannerActionEventHandler(InputScannerCollection inputScannerCollection) {
        return event -> {
            InputScanner newInputScanner = new InputScanner(null);
            inputScannerCollection.getInputScanners().add(newInputScanner);
        };
    }

    private static Region createInputScannersNode(InputScannerCollection inputScannerCollection) {

        VBox inputScannersPane = new VBox(12);
        inputScannersPane.setPadding(new Insets(12, 12, 12, 12));

        Map<InputScanner, Pane> inputScannerPanes = new HashMap<>();
        for (InputScanner initialScanner : inputScannerCollection.getInputScanners()) {
            Pane inputScannerPane = InputScannerCollectionNodeFactory.createInputScannerPane(initialScanner, inputScannerCollection.getInputScanners());
            inputScannerPanes.put(initialScanner, inputScannerPane);
            inputScannersPane.getChildren().add(inputScannerPane);
        }

        inputScannerCollection.getInputScanners().addListener((ListChangeListener<InputScanner>) change -> {
            while (change.next()) {
                for (InputScanner removedInputScanner : change.getRemoved()) {
                    Pane removeInputScannerPane = inputScannerPanes.remove(removedInputScanner);
                    if (removeInputScannerPane != null) {
                        Platform.runLater(() -> inputScannersPane.getChildren().remove(removeInputScannerPane));
                    }
                }
                for (InputScanner addedInputScanner : change.getAddedSubList()) {
                    int newInputScannerPaneIndex = inputScannerCollection.getInputScanners().indexOf(addedInputScanner);
                    Pane newInputScannerPane =  InputScannerCollectionNodeFactory.createInputScannerPane(addedInputScanner, inputScannerCollection.getInputScanners());
                    inputScannerPanes.put(addedInputScanner, newInputScannerPane);
                    Platform.runLater(() -> inputScannersPane.getChildren().add(newInputScannerPaneIndex, newInputScannerPane));
                }
            }
        });

        ScrollPane inputScannersScrollPane = new ScrollPane(inputScannersPane);
        inputScannersScrollPane.setFitToWidth(true);
        inputScannersScrollPane.setFocusTraversable(false);
        inputScannersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        inputScannersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return inputScannersScrollPane;

    }

    private static Pane createInputScannerPane(InputScanner inputScanner, ObservableList<InputScanner> allInputScanners) {

        Region definitionNode = InputScannerCollectionNodeFactory.createInputScannerDefinitionNode(inputScanner, allInputScanners);
        definitionNode.setPrefWidth(300);
        GridPane.setHgrow(definitionNode, Priority.ALWAYS);

        Region previewNode = InputScannerCollectionNodeFactory.createInputScannerPreviewNode(inputScanner);
        previewNode.setPrefWidth(350);

        GridPane inputScannerPane = new GridPane();
        inputScannerPane.add(definitionNode, 0, 0, 1, 1);
        inputScannerPane.add(previewNode, 1, 0, 1, 1);
        inputScannerPane.setHgap(12);
        inputScannerPane.setPadding(new Insets(12, 12, 12, 12));
        inputScannerPane.setStyle("-fx-border-color: lightgray");
        return inputScannerPane;

    }

    private static Region createInputScannerDefinitionNode(InputScanner inputScanner, ObservableList<InputScanner> allInputScanners) {

        TextField rootDirectoryField = ComponentFactory.createValidatedTextField(inputScanner.rootDirectoryProperty(), new PathStringConverter(), inputScanner.rootDirectoryProperty().isNotNull());
        HBox.setHgrow(rootDirectoryField, Priority.ALWAYS);
        Button rootDirectorySelectButton = new Button("Select", new FontIcon(MaterialDesignF.FOLDER));
        Button removeButton = new Button("Remove", new FontIcon(MaterialDesignT.TRASH_CAN));
        removeButton.setOnAction(action -> Platform.runLater(() -> allInputScanners.remove(inputScanner)));
        rootDirectorySelectButton.setOnAction(new SelectDirectoryActionEventHandler(inputScanner.rootDirectoryProperty()));
        HBox rootDirectoryPane = new HBox(2, rootDirectoryField, rootDirectorySelectButton, removeButton);
        GridPane.setHgrow(rootDirectoryPane, Priority.ALWAYS);
        Label rootDirectoryLabel = ComponentFactory.createSmallLabel("Directory");
        rootDirectoryLabel.setLabelFor(rootDirectoryField);

        TextField depthField = ComponentFactory.createTextField(inputScanner.separatePackageDepthProperty(), new IntegerStringConverter());
        depthField.setMaxWidth(40);
        Label depthLabel = ComponentFactory.createSmallLabel("Depth");
        depthLabel.setPadding(new Insets(5, 0, 0, 0));
        depthLabel.setLabelFor(depthField);

        TextField rootNameField = new TextField();
        rootNameField.textProperty().bindBidirectional(inputScanner.rootNameProperty());
        GridPane.setHgrow(rootNameField, Priority.ALWAYS);
        Label rootNameLabel = ComponentFactory.createSmallLabel("Name");
        rootNameLabel.setPadding(new Insets(5, 0, 0, 0));
        rootNameLabel.setLabelFor(rootNameField);

        ColumnConstraints includesConstraints = new ColumnConstraints();
        includesConstraints.setHgrow(Priority.ALWAYS);
        TextFormatter<ObservableList<String>> includesFormatter = new TextFormatter<>(new LinesStringConverter(), FXCollections.observableArrayList(inputScanner.getIncludePatterns()));
        InputScannerCollectionNodeFactory.bindPatterns(includesFormatter.valueProperty(), inputScanner.getIncludePatterns());
        TextArea includesArea = new TextArea();
        includesArea.setTextFormatter(includesFormatter);
        includesArea.setMaxHeight(70);
        includesArea.setPrefWidth(0);
        Label includesLabel = ComponentFactory.createSmallLabel("Includes");
        includesLabel.setLabelFor(includesArea);
        ColumnConstraints excludesConstraints = new ColumnConstraints();
        excludesConstraints.setHgrow(Priority.ALWAYS);
        TextFormatter<ObservableList<String>> excludesFormatter = new TextFormatter<>(new LinesStringConverter(), FXCollections.observableArrayList(inputScanner.getExcludePatterns()));
        InputScannerCollectionNodeFactory.bindPatterns(excludesFormatter.valueProperty(), inputScanner.getExcludePatterns());
        TextArea excludesArea = new TextArea();
        excludesArea.setTextFormatter(excludesFormatter);
        excludesArea.setMaxHeight(70);
        excludesArea.setPrefWidth(0);
        Label excludesLabel = ComponentFactory.createSmallLabel("Excludes");
        excludesLabel.setLabelFor(excludesArea);

        GridPane includesExcludesPane = new GridPane();
        includesExcludesPane.setPadding(new Insets(5, 0, 0, 0));
        includesExcludesPane.setHgap(12);
        includesExcludesPane.setVgap(1);
        includesExcludesPane.getColumnConstraints().setAll(includesConstraints, excludesConstraints);
        includesExcludesPane.add(includesLabel, 0, 0, 1, 1);
        includesExcludesPane.add(includesArea, 0, 1, 1, 1);
        includesExcludesPane.add(excludesLabel, 1, 0, 1, 1);
        includesExcludesPane.add(excludesArea, 1, 1, 1, 1);

        GridPane definitionPane = new GridPane();
        definitionPane.add(rootDirectoryLabel, 0, 0, 2, 1);
        definitionPane.add(rootDirectoryPane, 0, 1, 2, 1);
        definitionPane.add(depthLabel, 0, 2, 1, 1);
        definitionPane.add(depthField, 0, 3, 1, 1);
        definitionPane.add(rootNameLabel, 1, 2, 1, 1);
        definitionPane.add(rootNameField, 1, 3, 1, 1);
        definitionPane.add(includesExcludesPane, 0, 4, 2, 1);
        definitionPane.setHgap(12);
        definitionPane.setVgap(1);
        return definitionPane;

    }

    private static void bindPatterns(ObjectProperty<ObservableList<String>> patternsProperty, ObservableList<String> packagePatterns) {
        patternsProperty.addListener((o, oldValue, newValue) -> {
            if (!packagePatterns.containsAll(newValue) || !newValue.containsAll(packagePatterns)) {
                packagePatterns.setAll(newValue);
            }
        });
        packagePatterns.addListener((ListChangeListener<String>) change -> {
            List<String> patternsValue = patternsProperty.getValue();
            if (!packagePatterns.containsAll(patternsValue) || !patternsValue.containsAll(patternsValue)) {
                patternsProperty.setValue(FXCollections.observableArrayList(change.getList()));
            }
        });
    }

    private static Region createInputScannerPreviewNode(InputScanner inputScanner) {
        return new InputScannerPreviewPane(inputScanner);
    }

}
