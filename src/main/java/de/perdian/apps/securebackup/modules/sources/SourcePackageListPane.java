package de.perdian.apps.securebackup.modules.sources;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

class SourcePackageListPane extends VBox {

    public SourcePackageListPane(ObservableList<SourcePackage> packages) {

        Map<SourcePackage, SourcePackagePane> packagePanes = new HashMap<>();

        for (SourcePackage initialPackage : packages) {
            SourcePackagePane packageBuilderPane = new SourcePackagePane(initialPackage, packages);
            packagePanes.put(initialPackage, packageBuilderPane);
            this.getChildren().add(packageBuilderPane);
        }

        packages.addListener((ListChangeListener<SourcePackage>) change -> {
            while (change.next()) {
                for (SourcePackage removedPackage : change.getRemoved()) {
                    SourcePackagePane removePackagePane = packagePanes.remove(removedPackage);
                    if (removePackagePane != null) {
                        Platform.runLater(() -> this.getChildren().remove(removePackagePane));
                    }
                }
                for (SourcePackage addedPackage : change.getAddedSubList()) {
                    int newPackagePaneIndex = packages.indexOf(addedPackage);
                    SourcePackagePane newPackagePane = new SourcePackagePane(addedPackage, packages);
                    packagePanes.put(addedPackage, newPackagePane);
                    Platform.runLater(() -> this.getChildren().add(newPackagePaneIndex, newPackagePane));
                }
            }
        });

        this.setSpacing(10);

    }

}
