package de.perdian.apps.securebackup.fx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SourceModel implements Externalizable {

    static final long serialVersionUID = 1L;

    private final ObjectProperty<Path> directory = new SimpleObjectProperty<>();
    private final ObservableList<String> includePatterns = FXCollections.observableArrayList();
    private final ObservableList<String> excludePatterns = FXCollections.observableArrayList();
    private final List<ChangeListener<? super SourceModel>>  changeListeners = new ArrayList<>();

    public SourceModel() {
        ChangeListener<Object> changeListener = (o, oldValue, newValue) -> this.getChangeListeners().forEach(listener -> listener.changed(null, this, this));
        this.getDirectory().addListener(changeListener);
        this.getIncludePatterns().addListener((ListChangeListener.Change<?> change) -> changeListener.changed(null, this, this));
        this.getExcludePatterns().addListener((ListChangeListener.Change<?> change) -> changeListener.changed(null, this, this));
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.getDirectory().toString());
        out.writeObject(new ArrayList<>(this.getIncludePatterns()));
        out.writeObject(new ArrayList<>(this.getExcludePatterns()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.getDirectory().setValue(Paths.get(in.readUTF()));
        this.getIncludePatterns().addAll(((List<String>)in.readObject()));
        this.getExcludePatterns().addAll(((List<String>)in.readObject()));
    }

    public List<Path> collectFiles() throws IOException  {

        Path sourceDirectory = this.getDirectory().getValue();
        FileSystem sourceFileSystem = sourceDirectory.getFileSystem();
        PathMatcher sourceIncludeMatcher = this.getIncludePatterns().isEmpty() ? file -> true : this.createConsolidatedPathMatcher(sourceFileSystem, this.getIncludePatterns());
        PathMatcher sourceExcludeMatcher = this.getExcludePatterns().isEmpty() ? file -> false : this.createConsolidatedPathMatcher(sourceFileSystem, this.getExcludePatterns());

        return Files
            .walk(sourceDirectory, FileVisitOption.FOLLOW_LINKS)
            .filter(path -> sourceIncludeMatcher.matches(path))
            .filter(path -> !sourceExcludeMatcher.matches(path))
            .sorted()
            .toList();

    }

    private PathMatcher createConsolidatedPathMatcher(FileSystem fileSystem, List<String> patterns) {

        List<PathMatcher> pathMatchers = patterns.stream()
            .map(pattern -> pattern.indexOf(":") < 0 ? "glob:" + pattern : pattern)
            .map(pattern -> fileSystem.getPathMatcher(pattern))
            .toList();

        return file -> pathMatchers.stream().filter(matcher -> matcher.matches(file)).findAny().isPresent();

    }

    public ObjectProperty<Path> getDirectory() {
        return this.directory;
    }

    public ObservableList<String> getIncludePatterns() {
        return this.includePatterns;
    }

    public ObservableList<String> getExcludePatterns() {
        return this.excludePatterns;
    }

    void addChangeListener(ChangeListener<? super SourceModel> changeListener) {
        this.getChangeListeners().add(changeListener);
    }
    List<ChangeListener<? super SourceModel>> getChangeListeners() {
        return changeListeners;
    }

}
