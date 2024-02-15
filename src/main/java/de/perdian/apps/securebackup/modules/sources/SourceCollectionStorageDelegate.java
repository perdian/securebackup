package de.perdian.apps.securebackup.modules.sources;

import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceCollectionStorageDelegate {

    void loadPackagesFromFile(Path storageFile, ObservableList<SourcePackage> packages) throws IOException;

    void savePackagesIntoFile(Path storageFile, ObservableList<SourcePackage> packages) throws IOException;

}
