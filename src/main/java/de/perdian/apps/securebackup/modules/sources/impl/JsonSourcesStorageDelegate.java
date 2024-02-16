package de.perdian.apps.securebackup.modules.sources.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.perdian.apps.securebackup.modules.sources.SourceCollectionStorageDelegate;
import de.perdian.apps.securebackup.modules.sources.SourcePackage;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonSourcesStorageDelegate implements SourceCollectionStorageDelegate {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(JsonSourcesStorageDelegate.class);

    @Override
    public void loadPackagesFromFile(Path storageFile, ObservableList<SourcePackage> packages) {

        log.debug("Loading packages from JSON file at: {}", storageFile);

        try (InputStream storageFileStream = new BufferedInputStream(Files.newInputStream(storageFile))) {
            ObjectReader objectReader = OBJECT_MAPPER.reader();
            JsonNode rootNode = objectReader.readTree(storageFileStream);
            ArrayNode packagesNode = rootNode.withArray("packages");
            packages.setAll(this.parsePackages(packagesNode));
        } catch (Exception e) {
            log.error("Cannot read packages from JSON file at: {}", storageFile, e);
        }

    }

    private List<SourcePackage> parsePackages(ArrayNode packagesNode) {
        List<SourcePackage> packageList = new ArrayList<>(packagesNode.size());
        for (int i=0; i < packagesNode.size(); i++) {
            packageList.add(this.parsePackage(packagesNode.get(i)));
        }
        return packageList;
    }

    private SourcePackage parsePackage(JsonNode packageNode) {
        String rootDirectoryValue = packageNode.get("rootDirectory") == null ? null : packageNode.get("rootDirectory").asText();
        String rootNameValue = packageNode.get("rootName") == null ? null : packageNode.get("rootName").asText();
        String separatePackageDepthValue = packageNode.get("separatePackageDepth") == null ? null : packageNode.get("separatePackageDepth").asText();
        SourcePackage resultPackage = new SourcePackage();
        resultPackage.rootDirectoryProperty().setValue(StringUtils.isEmpty(rootDirectoryValue) ? null : Path.of(rootDirectoryValue));
        resultPackage.rootNameProperty().setValue(rootNameValue);
        resultPackage.separatePackageDepthProperty().setValue(StringUtils.isEmpty(separatePackageDepthValue) ? null : Integer.valueOf(separatePackageDepthValue));
        resultPackage.getExcludePatterns().setAll(this.parsePatterns(packageNode.get("excludePatterns")));
        resultPackage.getIncludePatterns().setAll(this.parsePatterns(packageNode.get("includePatterns")));
        return resultPackage;
    }

    private Collection<String> parsePatterns(JsonNode patternsNode) {
        if (patternsNode != null && patternsNode.isArray()) {
            List<String> patternsList = new ArrayList<>(patternsNode.size());
            for (int i=0; i < patternsNode.size(); i++) {
                patternsList.add(patternsNode.get(i).asText());
            }
            return patternsList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void savePackagesIntoFile(Path storageFile, ObservableList<SourcePackage> packages) {

        log.debug("Writing packages into JSON file at: {}", storageFile);

        ArrayNode packagesNode = OBJECT_MAPPER.createArrayNode();
        packages.forEach(p -> packagesNode.add(this.createPackagesNode(p)));

        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
        rootNode.set("packages", packagesNode);
        ObjectWriter objectWriter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        try (OutputStream storageFileStream = new BufferedOutputStream(Files.newOutputStream(storageFile))) {
            objectWriter.writeValue(storageFileStream, rootNode);
        } catch (Exception e) {
            log.error("Cannot write packages into JSON file at: {}", storageFile, e);
        }

    }

    private JsonNode createPackagesNode(SourcePackage sourcePackage) {
        ObjectNode packageNode = OBJECT_MAPPER.createObjectNode();
        packageNode.put("rootDirectory", sourcePackage.rootDirectoryProperty().getValue() == null ? null : sourcePackage.rootDirectoryProperty().getValue().toString());
        packageNode.put("rootName", sourcePackage.rootNameProperty().getValue() == null ? null : sourcePackage.rootNameProperty().getValue());
        packageNode.put("separatePackageDepth", sourcePackage.separatePackageDepthProperty().getValue() == null ? null : String.valueOf(sourcePackage.separatePackageDepthProperty().getValue()));
        packageNode.put("excludePatterns", this.createPatternsNode(sourcePackage.getExcludePatterns()));
        packageNode.put("includePatterns", this.createPatternsNode(sourcePackage.getIncludePatterns()));
        return packageNode;
    }

    private ArrayNode createPatternsNode(List<String> patterns) {
        ArrayNode patternsNode = OBJECT_MAPPER.createArrayNode();
        patterns.forEach(pattern -> patternsNode.add(pattern));
        return patternsNode;
    }

}
