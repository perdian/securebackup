package de.perdian.apps.securebackup.modules.sources;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SourcePackage {

    private final List<String> GLOBAL_EXCLUDES = List.of(".DS_Store");

    private final ObjectProperty<Path> rootDirectory = new SimpleObjectProperty<>();
    private final StringProperty rootName = new SimpleStringProperty();
    private final ObjectProperty<Integer> separatePackageDepth = new SimpleObjectProperty<>(2);
    private final ObservableList<String> includePatterns = FXCollections.observableArrayList();
    private final ObservableList<String> excludePatterns = FXCollections.observableArrayList();
    private final List<ChangeListener<SourcePackage>> changeListeners = new CopyOnWriteArrayList<>();

    public SourcePackage() {
        this(null);
    }

    public SourcePackage(Path rootDirectory) {
        this.rootDirectoryProperty().setValue(rootDirectory);
        this.rootNameProperty().setValue(rootDirectory == null ? null : rootDirectory.getFileName().toString());
        this.rootDirectoryProperty().addListener((o, oldValue, newValue) -> {
            this.rootNameProperty().setValue(newValue == null ? null : newValue.getFileName().toString());
        });
        this.rootDirectoryProperty().addListener((o, oldValue, newValue) -> this.fireChange());
        this.rootNameProperty().addListener((o, oldValue, newValue) -> this.fireChange());
        this.separatePackageDepthProperty().addListener((o, oldValue, newValue) -> this.fireChange());
        for (ObservableList<String> patternList : List.of(this.getIncludePatterns(), this.getExcludePatterns())) {
            patternList.addListener((ListChangeListener<String>) change -> this.fireChange());
        }
    }

    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        toStringBuilder.append("rootDirectory", this.rootDirectoryProperty().getValue());
        toStringBuilder.append("depth", this.separatePackageDepthProperty().getValue());
        return toStringBuilder.toString();
    }

    public List<SourceFileCollection> createSourceFileCollections() {

        Map<String, List<SourceFile>> filesByPackageName = new TreeMap<>();
        this.appendSourcePackages(filesByPackageName, this.rootDirectoryProperty().getValue(), this.rootDirectoryProperty().getValue(), this.rootNameProperty().getValue(), this.separatePackageDepthProperty().getValue() == null ? 1 : this.separatePackageDepthProperty().getValue());

        return filesByPackageName.entrySet().stream()
            .map(filesByRootEntry -> new SourceFileCollection(filesByRootEntry.getKey(), filesByRootEntry.getValue()))
            .toList();

    }

    private void appendSourcePackages(Map<String, List<SourceFile>> targetFilesByPackageName, Path rootDirectory, Path parentDirectory, String packagePrefix, int remainingDepth) {
        if (parentDirectory != null) {
            try {

                PathMatcher pathMatcher = this.createConsolidatedPathMatcher(parentDirectory.getFileSystem(), true, true);
                PathMatcher pathMatcherExcludeOnly = this.createConsolidatedPathMatcher(parentDirectory.getFileSystem(), false, true);

                if (remainingDepth >= 1) {

                    // We only traverse the first level and create a new package by it
                    List<Path> filesInDirectory = !Files.exists(parentDirectory) ? Collections.emptyList() : Files
                        .walk(parentDirectory, 1, FileVisitOption.FOLLOW_LINKS)
                        .filter(path -> !Objects.equals(parentDirectory, path))
                        .filter(path -> pathMatcherExcludeOnly.matches(rootDirectory.relativize(path)))
                        .sorted()
                        .toList();

                    for (Path fileInDirectory : filesInDirectory) {
                        if (Files.isDirectory(fileInDirectory)) {
                            this.appendSourcePackages(targetFilesByPackageName, rootDirectory, fileInDirectory, packagePrefix + "/" + fileInDirectory.getFileName(), remainingDepth - 1);
                        } else if (Files.isRegularFile(fileInDirectory) && pathMatcher.matches(fileInDirectory)) {
                            targetFilesByPackageName.compute(packagePrefix, (k, v) -> v == null ? new ArrayList<>() : v).add(new SourceFile(fileInDirectory, parentDirectory.relativize(fileInDirectory).toString()));
                        }
                    }

                } else {

                    // All the children can be appended directory by their path, we don't have to perform
                    // any sub package creation at all
                    List<Path> filteredFilesInDirectoryRecursively = Files
                        .walk(parentDirectory, FileVisitOption.FOLLOW_LINKS)
                        .filter(path -> !Objects.equals(parentDirectory, path))
                        .filter(path -> Files.isRegularFile(path))
                        .filter(path -> pathMatcher.matches(rootDirectory.relativize(path)))
                        .sorted()
                        .toList();

                    for (Path fileInDirectory : filteredFilesInDirectoryRecursively) {
                        targetFilesByPackageName.compute(packagePrefix, (k, v) -> v == null ? new ArrayList<>() : v).add(new SourceFile(fileInDirectory, parentDirectory.relativize(fileInDirectory).toString()));
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException("Cannot append source packages", e);
            }
        }
    }

    private PathMatcher createConsolidatedPathMatcher(FileSystem fileSystem, boolean includeMatchers, boolean excludeMatchers) {

        PathMatcher sourceIncludeMatcher = this.getIncludePatterns().isEmpty() || !includeMatchers ? file -> true : this.createConsolidatedPathMatherForPatterns(fileSystem, this.getIncludePatterns());
        PathMatcher sourceExcludeMatcher = this.getExcludePatterns().isEmpty() || !excludeMatchers ? file -> false : this.createConsolidatedPathMatherForPatterns(fileSystem, this.getExcludePatterns());
        PathMatcher globalExcludeMatcher = this.createConsolidatedPathMatherForPatterns(fileSystem, GLOBAL_EXCLUDES);

        return file -> sourceIncludeMatcher.matches(file)
            && !sourceExcludeMatcher.matches(file)
            && !globalExcludeMatcher.matches(file)
        ;

    }

    private PathMatcher createConsolidatedPathMatherForPatterns(FileSystem fileSystem, List<String> patterns) {

        List<PathMatcher> pathMatchers = patterns.stream()
            .map(pattern -> !pattern.contains(":") ? "glob:" + pattern : pattern)
            .map(pattern -> fileSystem.getPathMatcher(pattern))
            .toList();

        return path -> pathMatchers.stream().anyMatch(matcher -> matcher.matches(path) || matcher.matches(path.getFileName()));

    }

    public ObjectProperty<Path> rootDirectoryProperty() {
        return this.rootDirectory;
    }

    public StringProperty rootNameProperty() {
        return this.rootName;
    }

    public ObjectProperty<Integer> separatePackageDepthProperty() {
        return this.separatePackageDepth;
    }

    public ObservableList<String> getIncludePatterns() {
        return this.includePatterns;
    }

    public ObservableList<String> getExcludePatterns() {
        return this.excludePatterns;
    }

    private void fireChange() {
        this.getChangeListeners().forEach(changeListener -> {
            changeListener.changed(null, this, this);
        });
    }
    public void addChangeListener(ChangeListener<SourcePackage> changeListener) {
        this.getChangeListeners().add(changeListener);
    }
    public boolean removeChangeListener(ChangeListener<SourcePackage> changeListener) {
        return this.getChangeListeners().remove(changeListener);
    }
    private List<ChangeListener<SourcePackage>> getChangeListeners() {
        return this.changeListeners;
    }

}
