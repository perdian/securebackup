package de.perdian.apps.securebackup.fx.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArchiverModelFactory {

    private static final Logger log = LoggerFactory.getLogger(ArchiverModelFactory.class);

    public static ArchiverModel createArchiverModel() {
        Path modelSourceFile = Paths.get(System.getProperty("user.home"), ".securebackup/archivermodel.object");
        ArchiverModel model = ArchiverModelFactory.loadArchiverModel(modelSourceFile);
        if (model != null) {
        } else {
            log.debug("No archiver model found at '{}'. Will create a new model");
            model = new ArchiverModel();
        }
        model.addChangeListener((o, oldValue, newValue) -> ArchiverModelFactory.writeArchiverModel(newValue, modelSourceFile));
        return model;
    }

    private static ArchiverModel loadArchiverModel(Path sourceFile) {
        if (Files.exists(sourceFile)) {
            try {
                log.trace("Archiver model loaded from '{}'", sourceFile);
                throw new UnsupportedOperationException();
            } catch (Exception e) {
                log.warn("Cannot load archiver model from: '{}'", sourceFile, e);
            }
        }
        return null;
    }

    private static void writeArchiverModel(ArchiverModel model, Path targetFile) {
        try {
            log.trace("Archiver model written into '{}'", targetFile);
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            log.warn("Cannot write archiver model into: '{}'", targetFile, e);
        }
    }

}
