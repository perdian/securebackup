package de.perdian.apps.securebackup.modules.sources;

import de.perdian.apps.securebackup.support.fx.actions.SelectDirectoryIntoPropertyActionEventHandler;
import de.perdian.apps.securebackup.support.fx.bindings.PathBindings;
import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import de.perdian.apps.securebackup.support.fx.converters.PathStringConverter;
import de.perdian.apps.securebackup.support.fx.decoration.TextFieldDecorator;
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

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

class SourcePackagePane extends GridPane {

    SourcePackagePane(SourcePackage sourcePackage) {

        TextFormatter<Path> rootDirectoryFormatter = new TextFormatter<>(new PathStringConverter());
        rootDirectoryFormatter.valueProperty().bindBidirectional(sourcePackage.rootDirectoryProperty());
        TextField rootDirectoryField = new TextField();
        TextFieldDecorator.bindErrorBackground(rootDirectoryField, PathBindings.exists(sourcePackage.rootDirectoryProperty()));
        rootDirectoryField.setTextFormatter(rootDirectoryFormatter);
        Label rootDirectoryLabel = ComponentFactory.createSmallLabel("Directory");
        rootDirectoryLabel.setLabelFor(rootDirectoryField);
        HBox.setHgrow(rootDirectoryField, Priority.ALWAYS);

        Button rootDirectorySelectButton = new Button("Select", new FontIcon(MaterialDesignF.FOLDER));
        rootDirectorySelectButton.setOnAction(new SelectDirectoryIntoPropertyActionEventHandler(sourcePackage.rootDirectoryProperty()));
        HBox rootDirectoryPane = new HBox(2, rootDirectoryField, rootDirectorySelectButton);
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
        SourcePackagePane.bindPatterns(includesFormatter.valueProperty(), sourcePackage.getIncludePatterns());
        TextArea includesArea = new TextArea();
        includesArea.setTextFormatter(includesFormatter);
        includesArea.setMaxHeight(70);
        Label includesLabel = ComponentFactory.createSmallLabel("Includes");
        includesLabel.setLabelFor(includesArea);
        ColumnConstraints excludesConstraints = new ColumnConstraints();
        excludesConstraints.setHgrow(Priority.ALWAYS);
        TextFormatter<ObservableList<String>> excludesFormatter = new TextFormatter<>(new PatternsStringConverter(), FXCollections.observableArrayList(sourcePackage.getExcludePatterns()));
        SourcePackagePane.bindPatterns(excludesFormatter.valueProperty(), sourcePackage.getExcludePatterns());
        TextArea excludesArea = new TextArea();
        excludesArea.setTextFormatter(excludesFormatter);
        excludesArea.setMaxHeight(70);
        Label excludesLabel = ComponentFactory.createSmallLabel("Excludes");
        excludesLabel.setLabelFor(excludesArea);

        GridPane includesExcludesPane = new GridPane();
        includesExcludesPane.setPadding(new Insets(5, 0,0, 0));
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
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setStyle("-fx-border-color: lightgray");

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
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining("\n"));
            }
        }

        @Override
        public ObservableList<String> fromString(String string) {
            if (StringUtils.isEmpty(string)) {
                return FXCollections.emptyObservableList();
            } else {
                return FXCollections.observableArrayList(string.lines().filter(StringUtils::isNotEmpty).toList());
            }
        }

    }

}
