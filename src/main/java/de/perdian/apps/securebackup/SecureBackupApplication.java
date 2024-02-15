package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.modules.collector.CollectorSettings;
import de.perdian.apps.securebackup.modules.preferences.Preferences;
import de.perdian.apps.securebackup.modules.preferences.PreferencesStorageDelegate;
import de.perdian.apps.securebackup.modules.preferences.impl.MacOSKeychainStorageDelegate;
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

    @Override
    public void init() throws Exception {

        Path storageDirectory = Path.of(System.getProperty("user.home"), ".securebackup/");
        log.debug("Using application storage directory: {}", storageDirectory);

        PreferencesStorageDelegate preferencesStorageDelegate = new MacOSKeychainStorageDelegate();
        Preferences preferences = new Preferences(preferencesStorageDelegate);

        this.setCollectorSettings(new CollectorSettings(preferences));

    }

    @Override
    public void start(Stage primaryStage) {

        SecureBackupApplicationPane applicationPane = new SecureBackupApplicationPane(this.getCollectorSettings());
        Scene applicationScene = new Scene(applicationPane, 1400, 1100);

        primaryStage.setTitle("SecureBackup by perdian");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/vault-solid.png")));
        primaryStage.setScene(applicationScene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private CollectorSettings getCollectorSettings() {
        return this.collectorSettings;
    }
    private void setCollectorSettings(CollectorSettings collectorSettings) {
        this.collectorSettings = collectorSettings;
    }

}
