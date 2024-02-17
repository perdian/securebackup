package de.perdian.apps.securebackup.modules.sources;

import de.perdian.apps.securebackup.support.fx.actions.SelectDirectoryIntoPropertyActionEventHandler;
import de.perdian.apps.securebackup.support.fx.bindings.PathBindings;
import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import de.perdian.apps.securebackup.support.fx.converters.PathStringConverter;
import de.perdian.apps.securebackup.support.fx.decoration.TextFieldDecorator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

class SourcePackageDefinitionPane extends GridPane {

    SourcePackageDefinitionPane(SourcePackage sourcePackage, List<SourcePackage> allPackages) {

        TextFormatter<Path> rootDirectoryFormatter = new TextFormatter<>(new PathStringConverter());
        rootDirectoryFormatter.valueProperty().bindBidirectional(sourcePackage.rootDirectoryProperty());
        TextField rootDirectoryField = new TextField();
        TextFieldDecorator.bindErrorBackground(rootDirectoryField, PathBindings.exists(sourcePackage.rootDirectoryProperty()));
        rootDirectoryField.setTextFormatter(rootDirectoryFormatter);
        Label rootDirectoryLabel = ComponentFactory.createSmallLabel("Directory");
        rootDirectoryLabel.setLabelFor(rootDirectoryField);
        HBox.setHgrow(rootDirectoryField, Priority.ALWAYS);

        Button rootDirectorySelectButton = new Button("Select", new FontIcon(MaterialDesignF.FOLDER));
        Button removeButton = new Button("Remove", new FontIcon(MaterialDesignT.TRASH_CAN));
        removeButton.setOnAction(action -> Platform.runLater(() -> allPackages.remove(sourcePackage)));
        rootDirectorySelectButton.setOnAction(new SelectDirectoryIntoPropertyActionEventHandler(sourcePackage.rootDirectoryProperty()));
        HBox rootDirectoryPane = new HBox(2, rootDirectoryField, rootDirectorySelectButton, removeButton);
        GridPane.setHgrow(rootDirectoryPane, Priority.ALWAYS);

        TextFormatter<Integer> depthFormatter = new TextFormatter<>(new IntegerStringConverter());
        depthFormatter.valueProperty().bindBidirectional(sourcePackage.separatePackageDepthProperty());
        TextField depthField = new TextField();
        depthField.setMaxWidth(40);
        depthField.setTextFormatter(depthFormatter);
        Label depthLabel = ComponentFactory.createSmallLabel("Depth");
        depthLabel.setPadding(new Insets(5, 0, 0, 0));
        depthLabel.setLabelFor(depthField);

        TextField rootNameField = new TextField();
        rootNameField.textProperty().bindBidirectional(sourcePackage.rootNameProperty());
        GridPane.setHgrow(rootNameField, Priority.ALWAYS);
        Label rootNameLabel = ComponentFactory.createSmallLabel("Name");
        rootNameLabel.setPadding(new Insets(5, 0, 0, 0));
        rootNameLabel.setLabelFor(rootNameField);

        ColumnConstraints includesConstraints = new ColumnConstraints();
        includesConstraints.setHgrow(Priority.ALWAYS);
        TextFormatter<ObservableList<String>> includesFormatter = new TextFormatter<>(new PatternsStringConverter(), FXCollections.observableArrayList(sourcePackage.getIncludePatterns()));
        SourcePackageDefinitionPane.bindPatterns(includesFormatter.valueProperty(), sourcePackage.getIncludePatterns());
        TextArea includesArea = new TextArea();
        includesArea.setTextFormatter(includesFormatter);
        includesArea.setMaxHeight(70);
        Label includesLabel = ComponentFactory.createSmallLabel("Includes");
        includesLabel.setLabelFor(includesArea);
        ColumnConstraints excludesConstraints = new ColumnConstraints();
        excludesConstraints.setHgrow(Priority.ALWAYS);
        TextFormatter<ObservableList<String>> excludesFormatter = new TextFormatter<>(new PatternsStringConverter(), FXCollections.observableArrayList(sourcePackage.getExcludePatterns()));
        SourcePackageDefinitionPane.bindPatterns(excludesFormatter.valueProperty(), sourcePackage.getExcludePatterns());
        TextArea excludesArea = new TextArea();
        excludesArea.setTextFormatter(excludesFormatter);
        excludesArea.setMaxHeight(70);
        Label excludesLabel = ComponentFactory.createSmallLabel("Excludes");
        excludesLabel.setLabelFor(excludesArea);

        GridPane includesExcludesPane = new GridPane();
        includesExcludesPane.setPadding(new Insets(5, 0, 0, 0));
        includesExcludesPane.setHgap(10);
        includesExcludesPane.setVgap(1);
        includesExcludesPane.getColumnConstraints().setAll(includesConstraints, excludesConstraints);
        includesExcludesPane.add(includesLabel, 0, 0, 1, 1);
        includesExcludesPane.add(includesArea, 0, 1, 1, 1);
        includesExcludesPane.add(excludesLabel, 1, 0, 1, 1);
        includesExcludesPane.add(excludesArea, 1, 1, 1, 1);

        this.add(rootDirectoryLabel, 0, 0, 2, 1);
        this.add(rootDirectoryPane, 0, 1, 2, 1);

        this.add(depthLabel, 0, 2, 1, 1);
        this.add(depthField, 0, 3, 1, 1);
        this.add(rootNameLabel, 1, 2, 1, 1);
        this.add(rootNameField, 1, 3, 1, 1);
        this.add(includesExcludesPane, 0, 4, 2, 1);
        this.setHgap(10);
        this.setVgap(1);

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

    static class PatternsStringConverter extends StringConverter<ObservableList<String>> {

        @Override
        public String toString(ObservableList<String> patterns) {
            if (patterns == null) {
                return null;
            } else {
                return patterns.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("\n"));
            }
        }

        @Override
        public ObservableList<String> fromString(String string) {
            if (StringUtils.isEmpty(string)) {
                return FXCollections.emptyObservableList();
            } else {
                return FXCollections.observableArrayList(
                    string.lines()
                        .filter(StringUtils::isNotBlank)
                        .map(String::strip)
                        .toList()
                );
            }
        }

    }

}
