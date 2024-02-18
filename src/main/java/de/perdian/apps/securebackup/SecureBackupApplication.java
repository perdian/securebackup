package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.modules.collector.Collector;
import de.perdian.apps.securebackup.modules.collector.CollectorJobNodeFactory;
import de.perdian.apps.securebackup.modules.collector.CollectorNodeFactory;
import de.perdian.apps.securebackup.modules.input.InputScannerCollection;
import de.perdian.apps.securebackup.modules.input.InputScannerCollectionNodeFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class SecureBackupApplication extends Application {

    private Collector collector = null;
    private InputScannerCollection inputScannerCollection = null;

    @Override
    public void init() throws Exception {
        SecureBackupPreferences preferences = new SecureBackupPreferences();
        this.setCollector(new Collector(preferences));
        this.setInputScannerCollection(new InputScannerCollection(preferences));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene applicationScene = new Scene(this.createApplicationPane(), 1800, 1200);

        primaryStage.setTitle("SecureBackup by perdian");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/vault-solid.png")));
        primaryStage.setScene(applicationScene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private Pane createApplicationPane() {

        Region inputScannerCollectionNode = InputScannerCollectionNodeFactory.createInputScannerCollectionNode(this.getInputScannerCollection());
        GridPane.setHgrow(inputScannerCollectionNode, Priority.ALWAYS);
        GridPane.setVgrow(inputScannerCollectionNode, Priority.ALWAYS);

        Region collectorNode = CollectorNodeFactory.createCollectorNode(this.getCollector(), this.getInputScannerCollection());
        collectorNode.setPrefWidth(700);

        Region collectorJobsNode = CollectorJobNodeFactory.createCollectorJobsNode(this.getCollector().getActiveJobs());
        GridPane.setVgrow(collectorJobsNode, Priority.ALWAYS);

        GridPane applicationPane = new GridPane();
        applicationPane.add(inputScannerCollectionNode, 0, 0, 1, 2);
        applicationPane.add(collectorNode, 1, 0, 1, 1);
        applicationPane.add(collectorJobsNode, 1, 1, 1, 1);
        applicationPane.setHgap(12);
        applicationPane.setVgap(12);
        applicationPane.setPadding(new Insets(12, 12, 12, 12));
        return applicationPane;

    }

    private Collector getCollector() {
        return this.collector;
    }
    private void setCollector(Collector collector) {
        this.collector = collector;
    }

    private InputScannerCollection getInputScannerCollection() {
        return this.inputScannerCollection;
    }
    private void setInputScannerCollection(InputScannerCollection inputScannerCollection) {
        this.inputScannerCollection = inputScannerCollection;
    }

}
