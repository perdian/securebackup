package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.sources.SourceCollection;
import de.perdian.apps.securebackup.modules.sources.SourceFileCollection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class CollectorActionEventHandler implements EventHandler<ActionEvent> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss");

    private SourceCollection sourcesCollection = null;
    private CollectorSettings collectorSettings = null;
    private ObjectProperty<Collector> collectorProperty = null;
    private BooleanProperty busyProperty = null;

    CollectorActionEventHandler(SourceCollection sourcesCollection, CollectorSettings collectorSettings, ObjectProperty<Collector> collectorProperty, BooleanProperty busyProperty) {
        this.setSourcesCollection(sourcesCollection);
        this.setCollectorSettings(collectorSettings);
        this.setCollectorProperty(collectorProperty);
        this.setBusyProperty(busyProperty);
    }

    @Override
    public void handle(ActionEvent actionEvent) {

        ZonedDateTime collectorDateTime = Instant.now().atZone(ZoneId.systemDefault());
        String collectorTargetDirectoryName = DATE_TIME_FORMATTER.format(collectorDateTime);
        Path collectorTargetDirectory = this.getCollectorSettings().targetDirectoryProperty().getValue().resolve(collectorTargetDirectoryName);
        List<SourceFileCollection> sourceFileCollections = this.getSourcesCollection().createFileCollections();

        Collector collector = collectorSettings.createCollector(sourceFileCollections, this.getBusyProperty());
        collector.passwordProperty().setValue(this.getCollectorSettings().passwordProperty().getValue());
        collector.encryporProperty().setValue(this.getCollectorSettings().encryptorTypeProperty().getValue().createEncryptor());
        collector.targetDirectoryProperty().setValue(collectorTargetDirectory);
        this.getCollectorProperty().setValue(collector);

        Thread.ofVirtual().start(() -> {
            try {
                collector.execute();
            } catch (Exception e) {

            }
        });

    }

    private SourceCollection getSourcesCollection() {
        return this.sourcesCollection;
    }
    private void setSourcesCollection(SourceCollection sourcesCollection) {
        this.sourcesCollection = sourcesCollection;
    }

    private CollectorSettings getCollectorSettings() {
        return this.collectorSettings;
    }
    private void setCollectorSettings(CollectorSettings collectorSettings) {
        this.collectorSettings = collectorSettings;
    }

    private ObjectProperty<Collector> getCollectorProperty() {
        return this.collectorProperty;
    }
    private void setCollectorProperty(ObjectProperty<Collector> collectorProperty) {
        this.collectorProperty = collectorProperty;
    }

    private BooleanProperty getBusyProperty() {
        return this.busyProperty;
    }
    private void setBusyProperty(BooleanProperty busyProperty) {
        this.busyProperty = busyProperty;
    }

}
