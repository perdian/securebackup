package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CollectorLogPane extends GridPane {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CollectorLogPane(ObjectProperty<Collector> collectorProperty) {

        Label logAreaLabel = ComponentFactory.createSmallLabel("Log");
        logAreaLabel.setPadding(new Insets(8, 0, 0, 0));
        TextArea logArea = new TextArea();
        logArea.setPrefHeight(200);
        logArea.setEditable(false);
        GridPane.setHgrow(logArea, Priority.ALWAYS);

        BorderPane collectorParentPane = new BorderPane();
        collectorProperty.addListener((o, oldCollector, newCollector) -> {

            newCollector.addProgressListener((progressMessage, progressException) -> {
                if (StringUtils.isNotEmpty(progressMessage)) {
                    logArea.appendText(DATE_TIME_FORMATTER.format(ZonedDateTime.now()) + " | " + progressMessage + "\n");
                }
                if (progressException != null) {
                    logArea.appendText(ExceptionUtils.getStackTrace(progressException) + "\n");
                }
            });

            CollectorPane collectorPane = new CollectorPane(newCollector);
            Platform.runLater(() -> collectorParentPane.setCenter(collectorPane));

        });
        GridPane.setHgrow(collectorParentPane, Priority.ALWAYS);
        GridPane.setVgrow(collectorParentPane, Priority.ALWAYS);

        this.setVgap(2);
        this.add(collectorParentPane, 0, 0, 1, 1);
        this.add(logAreaLabel, 0, 1, 1, 1);
        this.add(logArea, 0, 2, 1, 1);

    }

}
