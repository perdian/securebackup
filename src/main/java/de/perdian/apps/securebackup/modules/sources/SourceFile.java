package de.perdian.apps.securebackup.modules.sources;

import java.nio.file.Path;

public class SourceFile {

    private Path file = null;
    private String relativeFileName = null;

    public SourceFile(Path file, String relativeFileName) {
        this.setFile(file);
        this.setRelativeFileName(relativeFileName);
    }

    @Override
    public String toString() {
        return this.getRelativeFileName();
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
