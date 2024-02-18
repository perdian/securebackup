package de.perdian.apps.securebackup;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureBackupApplicationLauncher {

    private static final Logger log = LoggerFactory.getLogger(SecureBackupApplicationLauncher.class);

    public static void main(String[] args) {

        log.debug("Launching JavaFX application [{}]", SecureBackupApplication.class.getName());
        Application.launch(SecureBackupApplication.class);

    }

}
