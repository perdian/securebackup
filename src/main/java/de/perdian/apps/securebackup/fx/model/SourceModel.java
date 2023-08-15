package de.perdian.apps.securebackup.fx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class SourceModel {

    private final ObjectProperty<Path> directory = new SimpleObjectProperty<>();
    private final ObservableList<String> includePatterns = FXCollections.observableArrayList();
    private final ObservableList<String> excludePatterns = FXCollections.observableArrayList();

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

}
