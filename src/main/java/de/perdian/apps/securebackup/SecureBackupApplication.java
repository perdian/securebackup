package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.model.BackupSettings;
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

    private BackupSettings backupSettings = null;
    private Preferences preferences = null;

    @Override
    public void init() throws Exception {

        Path storageDirectory = Path.of(System.getProperty("user.home"), ".securebackup/");
        log.debug("Loading model and preferences from storage directory: {}", storageDirectory);

        PreferencesStorageDelegate preferencesStorageDelegate = new MacOSKeychainStorageDelegate();
        Preferences preferences = new Preferences(preferencesStorageDelegate);
        BackupSettings backupSettings = new BackupSettings(preferences);
        this.setBackupSettings(backupSettings);
        this.setPreferences(preferences);

    }

    @Override
    public void start(Stage primaryStage) {

        SecureBackupApplicationPane applicationPane = new SecureBackupApplicationPane(this.getBackupSettings(), this.getPreferences());
        Scene applicationScene = new Scene(applicationPane, 1400, 1100);

        primaryStage.setTitle("SecureBackup by perdian");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/vault-solid.png")));
        primaryStage.setScene(applicationScene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private BackupSettings getBackupSettings() {
        return this.backupSettings;
    }
    private void setBackupSettings(BackupSettings backupSettings) {
        this.backupSettings = backupSettings;
    }

    private Preferences getPreferences() {
        return this.preferences;
    }
    private void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

}
