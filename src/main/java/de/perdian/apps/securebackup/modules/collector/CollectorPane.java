package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.sources.SourceFileCollection;
import de.perdian.apps.securebackup.support.fx.bindings.CollectionBindings;
import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import java.util.function.Function;

class CollectorPane extends GridPane {

    CollectorPane(Collector collector) {

        Label targetDirectoryLabel = ComponentFactory.createSmallLabel("Target directory");
        targetDirectoryLabel.disableProperty().bind(collector.busyProperty().not());
        TextField targetDirectoryField = new TextField(collector.targetDirectoryProperty().getValue().toString());
        targetDirectoryField.disableProperty().bind(collector.busyProperty().not());
        targetDirectoryField.setEditable(false);
        GridPane.setHgrow(targetDirectoryField, Priority.ALWAYS);

        Pane collectionsProgressPane = CollectorPane.createProgressPane("Collections", collector.activeCollectionProgressProperty(), collector.activeCollectionProperty(), collection -> collection.getName(), collector.cancelledProperty());
        collectionsProgressPane.disableProperty().bind(collector.busyProperty().not());
        collectionsProgressPane.setPadding(new Insets(8, 0, 0, 0));
        Pane filesProgressPane = CollectorPane.createProgressPane("Files in collection", collector.activeFileProgressProperty(), collector.activeFileProperty(), file -> file.getRelativeFileName(), null);
        filesProgressPane.disableProperty().bind(collector.busyProperty().not());
        filesProgressPane.setPadding(new Insets(8, 0, 0, 0));

        ObservableList<SourceFileCollection> backlogCollections = FXCollections.observableArrayList(collector.getBacklogCollections());
        CollectionBindings.bindCollectionWithinFxThread(collector.getBacklogCollections(), backlogCollections);
        ListView<SourceFileCollection> backlogConnectionsView = new ListView<>(backlogCollections);
        backlogConnectionsView.disableProperty().bind(collector.busyProperty().not());
        backlogConnectionsView.setPrefHeight(0);
        backlogConnectionsView.setPadding(new Insets(8, 0, 0, 0));
        GridPane.setHgrow(backlogConnectionsView, Priority.ALWAYS);
        GridPane.setVgrow(backlogConnectionsView, Priority.ALWAYS);
        Label backlogConnectionsLabel = ComponentFactory.createSmallLabel("Backlog collections");
        backlogConnectionsLabel.disableProperty().bind(collector.busyProperty().not());
        backlogConnectionsLabel.setPadding(new Insets(8, 0, 0, 0));
        backlogConnectionsLabel.setLabelFor(backlogConnectionsView);

        ObservableList<SourceFileCollection> completedCollections = FXCollections.observableArrayList(collector.getCompletedCollections());
        CollectionBindings.bindCollectionWithinFxThread(collector.getCompletedCollections(), completedCollections);
        ListView<SourceFileCollection> completedConnectionsView = new ListView<>(completedCollections);
        completedConnectionsView.disableProperty().bind(collector.busyProperty().not());
        completedConnectionsView.setPrefHeight(0);
        GridPane.setHgrow(completedConnectionsView, Priority.ALWAYS);
        GridPane.setVgrow(completedConnectionsView, Priority.ALWAYS);
        Label completedConnectionsLabel = ComponentFactory.createSmallLabel("Completed collections");
        completedConnectionsLabel.disableProperty().bind(collector.busyProperty().not());
        completedConnectionsLabel.setPadding(new Insets(8, 0, 0, 0));
        completedConnectionsLabel.setLabelFor(completedConnectionsView);

        Label resultTitleLabel = ComponentFactory.createSmallLabel("Result");
        resultTitleLabel.disableProperty().bind(collector.busyProperty());
        resultTitleLabel.setPadding(new Insets(8, 0, 0, 0));
        Label resultValueLabel = ComponentFactory.createSmallLabel("...");
        resultValueLabel.setPrefHeight(30);
        resultValueLabel.setAlignment(Pos.CENTER);
        resultValueLabel.setMaxWidth(Double.MAX_VALUE);
        resultValueLabel.disableProperty().bind(collector.busyProperty());
        resultValueLabel.setStyle("-fx-border-color: lightgray;");
        GridPane.setHgrow(resultValueLabel, Priority.ALWAYS);
        collector.resultProperty().addListener((o, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (CollectorResult.SUCCESS.equals(newValue)) {
                    resultValueLabel.setText("Success");
                    resultValueLabel.setStyle("-fx-border-color: lightgray; -fx-background-color: #A5D6A7; "); // GREEN 200
                } else if (CollectorResult.CANCELLED.equals(newValue)) {
                    resultValueLabel.setText("Cancelled");
                    resultValueLabel.setStyle("-fx-border-color: lightgray; -fx-background-color: #FFF59D; "); // YELLOW 200
                } else if (CollectorResult.ERROR.equals(newValue)) {
                    resultValueLabel.setText("Error");
                    resultValueLabel.setStyle("-fx-border-color: lightgray; -fx-background-color: #EF9A9A; "); // RED 200
                }
            });
        });

        this.add(targetDirectoryLabel, 0, 0, 1, 1);
        this.add(targetDirectoryField, 0, 1, 1, 1);
        this.add(collectionsProgressPane, 0, 2, 1, 1);
        this.add(filesProgressPane, 0, 3, 1, 1);
        this.add(backlogConnectionsLabel, 0, 4, 1, 1);
        this.add(backlogConnectionsView, 0, 5, 1, 1);
        this.add(completedConnectionsLabel, 0, 6, 1, 1);
        this.add(completedConnectionsView, 0, 7, 1, 1);
        this.add(resultTitleLabel, 0, 8, 1, 1);
        this.add(resultValueLabel, 0, 9, 1, 1);
        this.setVgap(2);

    }

    private static <T> Pane createProgressPane(String title, DoubleProperty progressProperty, ObjectProperty<T> activeItemProperty, Function<T, String> itemToStringFunction, BooleanProperty cancelledProperty) {

        Label progressTitleLabel = ComponentFactory.createSmallLabel(title);

        ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setMinHeight(20);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressProperty.addListener((o, oldValue, newValue) -> Platform.runLater(() -> progressBar.setProgress(newValue == null ? ProgressBar.INDETERMINATE_PROGRESS : newValue.doubleValue())));
        GridPane.setHgrow(progressBar, Priority.ALWAYS);

        Label progressItemLabel = ComponentFactory.createSmallLabel(" ");
        GridPane.setHgrow(progressItemLabel, Priority.ALWAYS);
        activeItemProperty.addListener((o, oldValue, newValue) -> Platform.runLater(() -> progressItemLabel.setText(newValue == null ? " " : itemToStringFunction.apply(newValue))));

        GridPane progressPane = new GridPane();
        progressPane.add(progressTitleLabel, 0, 0, 1, 1);
        progressPane.add(progressBar, 0, 1, 1, 1);
        progressPane.add(progressItemLabel, 0, 2, 1, 1);
        progressPane.setVgap(2);

        BorderPane resultPane = new BorderPane(progressPane);
        if (cancelledProperty != null) {
            Button cancelButton = new Button("Cancel", new FontIcon(MaterialDesignS.STOP));
            cancelButton.setOnAction(event -> cancelledProperty.setValue(true));
            cancelButton.setMaxHeight(Double.MAX_VALUE);
            cancelButton.disableProperty().bind(cancelledProperty);
            BorderPane.setMargin(cancelButton, new Insets(0, 0, 0, 10));
            resultPane.setRight(cancelButton);
        }
        return resultPane;

    }

}
