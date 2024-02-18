package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CollectorJobNodeFactory {

    public static Region createCollectorJobsNode(ObservableList<CollectorJob> jobs) {

        TabPane jobsTabPane = new TabPane();
        jobs.addListener((ListChangeListener<CollectorJob>) change -> {
            while (change.next()) {
                for (CollectorJob addedCollectorJob : change.getAddedSubList()) {
                    Tab addedCollectorTab = CollectorJobNodeFactory.createCollectorJobTab(addedCollectorJob);
                    Platform.runLater(() -> {
                        jobsTabPane.getTabs().add(addedCollectorTab);
                        jobsTabPane.getSelectionModel().select(addedCollectorTab);
                    });
                }
            }
        });

        TitledPane inputScannerCollectionTitledPane = new TitledPane("Collector jobs", jobsTabPane);
        inputScannerCollectionTitledPane.setMaxHeight(Double.MAX_VALUE);
        inputScannerCollectionTitledPane.setCollapsible(false);
        inputScannerCollectionTitledPane.setGraphic(new FontIcon(MaterialDesignC.CLIPBOARD_LIST_OUTLINE));
        return inputScannerCollectionTitledPane;

    }

    private static Tab createCollectorJobTab(CollectorJob collectorJob) {
        Region collectorJobNode = CollectorJobNodeFactory.createCollectorJobNode(collectorJob);
        Tab collectorTab = new Tab(collectorJob.getId(), collectorJobNode);
        collectorTab.closableProperty().bind(Bindings.notEqual(collectorJob.statusProperty(), CollectorJobStatus.NEW).and(Bindings.notEqual(collectorJob.statusProperty(), CollectorJobStatus.RUNNING)));
        return collectorTab;
    }

    private static Region createCollectorJobNode(CollectorJob collectorJob) {

        Region statusNode = CollectorJobNodeFactory.createCollectorJobStatusNode(collectorJob.statusProperty());
        GridPane.setHgrow(statusNode, Priority.ALWAYS);

        Region packagesProgressNode = CollectorJobNodeFactory.createCollectorJobProgressNode("Packages", collectorJob.getPackageProgress());
        GridPane.setHgrow(packagesProgressNode, Priority.ALWAYS);

        Region filesProgressNode = CollectorJobNodeFactory.createCollectorJobProgressNode("Files", collectorJob.getPackageFileProgress());
        GridPane.setHgrow(filesProgressNode, Priority.ALWAYS);

        Region logMessagesNode = CollectorJobNodeFactory.createCollectorJobProgressLogMessagesNode(collectorJob);
        GridPane.setHgrow(logMessagesNode, Priority.ALWAYS);
        GridPane.setVgrow(logMessagesNode, Priority.ALWAYS);

        GridPane jobPane = new GridPane();
        jobPane.add(statusNode, 0, 0, 1, 1);
        jobPane.add(packagesProgressNode, 0, 1, 1, 1);
        jobPane.add(filesProgressNode, 0, 2, 1, 1);
        jobPane.add(logMessagesNode, 0, 3, 1, 1);
        jobPane.setVgap(12);
        jobPane.setPadding(new Insets(12, 12, 12, 12));
        return jobPane;

    }

    private static <T> Region createCollectorJobStatusNode(ReadOnlyObjectProperty<CollectorJobStatus> statusProperty) {

        Label statusTitleLabel = ComponentFactory.createSmallLabel("Status");
        Label statusValueLabel = new Label("...");
        statusValueLabel.setPadding(new Insets(2, 2,2, 2));
        statusValueLabel.setPrefHeight(30);
        statusValueLabel.setAlignment(Pos.CENTER);
        statusValueLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(statusValueLabel, Priority.ALWAYS);

        ChangeListener<CollectorJobStatus> statusChangeListener = (o, oldValue, newValue) -> {
            StringBuilder statusValueStyle = new StringBuilder("-fx-border-color: lightgray;");
            if (StringUtils.isNotEmpty(newValue.getColor())) {
                statusValueStyle.append(" -fx-background-color: ").append(newValue.getColor()).append(";");
            }
            Platform.runLater(() -> {
                statusValueLabel.setText(newValue.getTitle());
                statusValueLabel.setStyle(statusValueStyle.toString());
            });
        };
        statusChangeListener.changed(null, null, statusProperty.getValue());
        statusProperty.addListener(statusChangeListener);

        GridPane statusPane = new GridPane();
        statusPane.add(statusTitleLabel, 0, 0, 1, 1);
        statusPane.add(statusValueLabel, 0, 1, 1, 1);
        statusPane.setVgap(2);
        return statusPane;

    }

    private static <T> Region createCollectorJobProgressNode(String title, CollectorJobProgress<T> progress) {

        ProgressBar progressBar = new ProgressBar(0d);
        progressBar.setMinHeight(20);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(progressBar, Priority.ALWAYS);
        progress.addListener((progressValue, progressMessage, progressException) -> {
            if (progressValue != null) {
                Platform.runLater(() -> progressBar.setProgress(progressValue));
            }
        });

        Label progressTitleLabel = ComponentFactory.createSmallLabel(title);
        progressTitleLabel.setLabelFor(progressBar);

        Label progressItemLabel = ComponentFactory.createSmallLabel(" ");
        GridPane.setHgrow(progressItemLabel, Priority.ALWAYS);
        progress.currentItemProperty().addListener((o, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue == null) {
                    progressItemLabel.setText(" ");
                } else {
                    progressItemLabel.setText(newValue.toString());
                }
            });
        });
        progressItemLabel.setLabelFor(progressBar);

        GridPane progressPane = new GridPane();
        progressPane.add(progressTitleLabel, 0, 0, 1, 1);
        progressPane.add(progressBar, 0, 1, 1, 1);
        progressPane.add(progressItemLabel, 0, 2, 1, 1);
        progressPane.setVgap(2);
        return progressPane;

    }

    private static Region createCollectorJobProgressLogMessagesNode(CollectorJob collectorJob) {

        TextArea logArea = new TextArea();
        logArea.setPrefHeight(200);
        logArea.setEditable(false);
        GridPane.setHgrow(logArea, Priority.ALWAYS);
        GridPane.setVgrow(logArea, Priority.ALWAYS);
        Label logAreaLabel = ComponentFactory.createSmallLabel("Log");
        logAreaLabel.setLabelFor(logArea);

        DateTimeFormatter progressMessageDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        CollectorJobProgressListener progressListener = (progressValue, progressMessage, progressException) -> {
            if (StringUtils.isNotEmpty(progressMessage)) {
                Platform.runLater(() -> logArea.appendText(progressMessageDateTimeFormatter.format(ZonedDateTime.now()) + " | " + progressMessage + "\n"));
            }
            if (progressException != null) {
                Platform.runLater(() -> logArea.appendText(ExceptionUtils.getStackTrace(progressException) + "\n"));
            }
        };
        collectorJob.getPackageProgress().addListener(progressListener);
        collectorJob.getPackageFileProgress().addListener(progressListener);

        GridPane logMessagesNode = new GridPane();
        logMessagesNode.add(logAreaLabel, 0, 0, 1, 1);
        logMessagesNode.add(logArea, 0, 0, 1, 1);
        logMessagesNode.setVgap(2);
        return logMessagesNode;

    }

}
