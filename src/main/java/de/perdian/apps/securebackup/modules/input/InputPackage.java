package de.perdian.apps.securebackup.modules.input;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InputPackage {

    private String name = null;
    private List<InputPackageFile> files = null;

    public InputPackage(String name, List<InputPackageFile> files) {
        this.setName(name);
        this.setFiles(files == null ? Collections.emptyList() : files);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public Instant getLastModifiedTime() {
        return this.getFiles().stream()
            .map(file -> file.getLastModifiedTime())
            .max(Comparator.naturalOrder())
            .orElseGet(Instant::now);
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<InputPackageFile> getFiles() {
        return this.files;
    }
    public void setFiles(List<InputPackageFile> files) {
        this.files = files;
    }

}
