package de.perdian.apps.securebackup.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class SourcePackageBuilder {

    private final List<String> GLOBAL_EXCLUDES = List.of(".DS_Store");

    private final ObjectProperty<Path> rootDirectory = new SimpleObjectProperty<>();
    private final StringProperty rootName = new SimpleStringProperty();
    private final IntegerProperty separatePackageDepth = new SimpleIntegerProperty(2);
    private final ObservableList<String> includePatterns = FXCollections.observableArrayList("Alfred/**", "Scrivener/**");
    private final ObservableList<String> excludePatterns = FXCollections.observableArrayList();

    public SourcePackageBuilder(Path rootDirectory) {
        this.rootDirectoryProperty().setValue(rootDirectory);
        this.rootNameProperty().setValue(rootDirectory.getFileName().toString());
    }

    public List<SourcePackage> createSourcePackages() throws IOException {

        Map<String, List<SourceFile>> filesByPackageName = new LinkedHashMap<>();
        this.appendSourcePackages(filesByPackageName, this.rootDirectoryProperty().getValue(), this.rootDirectoryProperty().getValue(), this.rootNameProperty().getValue(), this.separatePackageDepthProperty().intValue());

        return filesByPackageName.entrySet().stream()
            .map(filesByRootEntry -> new SourcePackage(filesByRootEntry.getKey(), filesByRootEntry.getValue()))
            .toList();

    }

    private void appendSourcePackages(Map<String, List<SourceFile>> targetFilesByPackageName, Path rootDirectory, Path parentDirectory, String packagePrefix, int remainingDepth) throws IOException {

        PathMatcher pathMatcher = this.createConsolidatedPathMatcher(parentDirectory.getFileSystem());

        if (remainingDepth >= 1) {

            // We only traverse the first level and create a new package by it
            List<Path> filesInDirectory = Files
                .walk(parentDirectory, 1, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Objects.equals(parentDirectory, path))
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

    }

    private PathMatcher createConsolidatedPathMatcher(FileSystem fileSystem) {

        PathMatcher sourceIncludeMatcher = this.getIncludePatterns().isEmpty() ? file -> true : this.createConsolidatedPathMatherForPatterns(fileSystem, this.getIncludePatterns());
        PathMatcher sourceExcludeMatcher = this.getExcludePatterns().isEmpty() ? file -> false : this.createConsolidatedPathMatherForPatterns(fileSystem, this.getExcludePatterns());
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

    public IntegerProperty separatePackageDepthProperty() {
        return this.separatePackageDepth;
    }

    public ObservableList<String> getIncludePatterns() {
        return this.includePatterns;
    }

    public ObservableList<String> getExcludePatterns() {
        return this.excludePatterns;
    }








//
//    @SuppressWarnings("resource")
//    public SourcePackage createSourcePackage() throws IOException {
//
//        Path sourceDirectory = this.getDirectory().getValue();
//        FileSystem sourceFileSystem = sourceDirectory.getFileSystem();
//        PathMatcher sourceIncludeMatcher = this.getIncludePatterns().isEmpty() ? file -> true : this.createConsolidatedPathMatcher(sourceFileSystem, this.getIncludePatterns());
//        PathMatcher sourceExcludeMatcher = this.getExcludePatterns().isEmpty() ? file -> false : this.createConsolidatedPathMatcher(sourceFileSystem, this.getExcludePatterns());
//
//        List<Path> files = Files
//            .walk(sourceDirectory, FileVisitOption.FOLLOW_LINKS)
//            .filter(path -> sourceIncludeMatcher.matches(path))
//            .filter(path -> !sourceExcludeMatcher.matches(path))
//            .sorted()
//            .toList();
//
//        SourcePackage sourcePackage = new SourcePackage();
//        return sourcePackage;
//
//    }
//

}
