package de.perdian.apps.securebackup.modules.sources;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

class SourcePackagePane extends GridPane {

    SourcePackagePane (SourcePackage sourcePackage) {

        SourcePackageDefinitionPane definitionPane = new SourcePackageDefinitionPane(sourcePackage);
        definitionPane.setPrefWidth(800);
        GridPane.setHgrow(definitionPane, Priority.ALWAYS);
        SourcePackagePreviewPane previewPane = new SourcePackagePreviewPane(sourcePackage);
        previewPane.setPrefWidth(500);

        this.add(definitionPane, 0, 0, 1, 1);
        this.add(previewPane, 1, 0, 1, 1);
        this.setHgap(10);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setStyle("-fx-border-color: lightgray");

    }

}
