package de.perdian.apps.securebackup.modules.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class InputPackageFile {

    private static final Logger log = LoggerFactory.getLogger(InputPackageFile.class);

    private Path file = null;
    private String relativeFileName = null;

    public InputPackageFile(Path file, String relativeFileName) {
        this.setFile(file);
        this.setRelativeFileName(relativeFileName);
    }

    @Override
    public String toString() {
        return this.getRelativeFileName();
    }

    public Instant getLastModifiedTime() {
        try {
            return Files.getLastModifiedTime(this.getFile()).toInstant();
        } catch (IOException e) {
            log.warn("Cannot read last modified time from file at: {}", this.getFile(), e);
            return Instant.now();
        }
    }

    public Path getFile() {
        return this.file;
    }
    private void setFile(Path file) {
        this.file = file;
    }

    public String getRelativeFileName() {
        return this.relativeFileName;
    }
    private void setRelativeFileName(String relativeFileName) {
        this.relativeFileName = relativeFileName;
    }

}
