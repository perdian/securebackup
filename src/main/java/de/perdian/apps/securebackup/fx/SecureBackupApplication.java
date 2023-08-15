package de.perdian.apps.securebackup.fx;

import de.perdian.apps.securebackup.fx.model.ArchiverModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureBackupApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(SecureBackupApplication.class);

    private ArchiverModel model = null;

    @Override
    public void init() throws Exception {
        log.debug("Loading last used model");
        ArchiverModel model = new ArchiverModel();
        this.setModel(model);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        SecureBackupApplicationPane applicationPane = new SecureBackupApplicationPane(this.getModel());
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

}
