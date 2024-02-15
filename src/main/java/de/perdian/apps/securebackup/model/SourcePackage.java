package de.perdian.apps.securebackup.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.List;

public class SourcePackage {

    private String name = null;
    private List<SourceFile> files = null;

    public SourcePackage(String name, List<SourceFile> files) {
        this.setName(name);
        this.setFiles(files == null ? Collections.emptyList() : files);
    }

    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        toStringBuilder.append("name", this.getName());
        toStringBuilder.append("files.size", this.getFiles().size());
        return toStringBuilder.toString();
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<SourceFile> getFiles() {
        return this.files;
    }
    public void setFiles(List<SourceFile> files) {
        this.files = files;
    }

}
