package de.perdian.apps.securebackup.fx;

import de.perdian.apps.securebackup.fx.model.ArchiverModel;
import de.perdian.support.fx.model.ModelBuilder;
import de.perdian.support.fx.preferences.Preferences;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SecureBackupApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(SecureBackupApplication.class);

    private ArchiverModel model = null;
    private Preferences preferences = null;

    @Override
    public void init() {
        Path storageDirectory = Path.of(System.getProperty("user.home"), ".securebackup/");
        log.debug("Loading model and preferences from storage directory: {}", storageDirectory);
        this.setModel(new ModelBuilder<>(ArchiverModel.class).createModel(storageDirectory.resolve(ArchiverModel.class.getSimpleName() + ".object")));
        this.setPreferences(new Preferences(storageDirectory.resolve("preferences")));
    }

    @Override
    public void start(Stage primaryStage) {

        SecureBackupApplicationPane applicationPane = new SecureBackupApplicationPane(this.getModel(), this.getPreferences());
        Scene applicationScene = new Scene(applicationPane, 1400, 1100);

        primaryStage.setTitle("SecureBackup by perdian");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icons/vault-solid.png")));
        primaryStage.setScene(applicationScene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private ArchiverModel getModel() {
        return this.model;
    }
    private void setModel(ArchiverModel model) {
        this.model = model;
    }

    private Preferences getPreferences() {
        return this.preferences;
    }
    private void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

}
