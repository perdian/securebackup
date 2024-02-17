package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.modules.collector.CollectorSettings;
import de.perdian.apps.securebackup.modules.preferences.Preferences;
import de.perdian.apps.securebackup.modules.preferences.PreferencesStorageDelegate;
import de.perdian.apps.securebackup.modules.preferences.impl.MacOSKeychainStorageDelegate;
import de.perdian.apps.securebackup.modules.sources.SourceCollection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SecureBackupApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(SecureBackupApplication.class);

    private CollectorSettings collectorSettings = null;
    private SourceCollection sourceCollection = null;

    @Override
    public void init() throws Exception {

        Path storageDirectory = Path.of(System.getProperty("user.home"), ".securebackup/");
        log.debug("Using application storage directory: {}", storageDirectory);

        PreferencesStorageDelegate preferencesStorageDelegate = new MacOSKeychainStorageDelegate();
        Preferences preferences = new Preferences(preferencesStorageDelegate);

        this.setCollectorSettings(new CollectorSettings(preferences));
        this.setSourceCollection(new SourceCollection(preferences));

    }

    @Override
    public void start(Stage primaryStage) {

        SecureBackupApplicationPane applicationPane = new SecureBackupApplicationPane(this.getSourceCollection(), this.getCollectorSettings());
        Scene applicationScene = new Scene(applicationPane, 1800, 1200);

        primaryStage.setTitle("SecureBackup by perdian");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/vault-solid.png")));
        primaryStage.setScene(applicationScene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private CollectorSettings getCollectorSettings() {
        return this.collectorSettings;
    }
    private void setCollectorSettings(CollectorSettings collectorSettings) {
        this.collectorSettings = collectorSettings;
    }

    private SourceCollection getSourceCollection() {
        return this.sourceCollection;
    }
    private void setSourceCollection(SourceCollection sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

}
