package de.perdian.apps.securebackup.modules.sources.impl;

import de.perdian.apps.securebackup.modules.sources.SourceCollectionStorageDelegate;
import de.perdian.apps.securebackup.modules.sources.SourcePackage;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class JsonSourcesStorageDelegate implements SourceCollectionStorageDelegate {

    private static final Logger log = LoggerFactory.getLogger(JsonSourcesStorageDelegate.class);

    @Override
    public void loadPackagesFromFile(Path storageFile, ObservableList<SourcePackage> packages) {
        log.debug("Loading JSON sources from file at: {}", storageFile);
    }

    @Override
    public void savePackagesIntoFile(Path storageFile, ObservableList<SourcePackage> packages) {
        log.debug("Writing JSON sources into file at: {}", storageFile);
    }

}
