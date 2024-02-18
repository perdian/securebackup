package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.encryptor.EncryptorType;
import de.perdian.apps.securebackup.modules.input.InputScannerCollection;
import de.perdian.apps.securebackup.support.fx.components.ComponentDecorator;
import de.perdian.apps.securebackup.support.fx.components.ComponentFactory;
import de.perdian.apps.securebackup.support.fx.handlers.SelectDirectoryActionEventHandler;
import de.perdian.apps.securebackup.support.fx.properties.converters.PathStringConverter;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

public class CollectorNodeFactory {

    public static Region createCollectorNode(Collector collector, InputScannerCollection inputScannerCollection) {

        TextField targetDirectoryField = ComponentFactory.createValidatedTextField(collector.targetDirectoryProperty(), new PathStringConverter(), collector.targetDirectoryProperty().isNotNull());
        GridPane.setHgrow(targetDirectoryField, Priority.ALWAYS);
        Button targetDirectorySelectButton = new Button(null, new FontIcon(MaterialDesignF.FOLDER));
        targetDirectorySelectButton.setOnAction(new SelectDirectoryActionEventHandler(collector.targetDirectoryProperty()));
        targetDirectorySelectButton.setTooltip(new Tooltip("Select target directory"));
        targetDirectorySelectButton.setMaxHeight(Double.MAX_VALUE);
        GridPane targetDirectoryPane = new GridPane(3, 0);
        targetDirectoryPane.add(targetDirectoryField, 0, 0, 1, 1);
        targetDirectoryPane.add(targetDirectorySelectButton, 1, 0, 1, 1);
        GridPane.setFillHeight(targetDirectorySelectButton, true);
        GridPane.setHgrow(targetDirectoryPane, Priority.ALWAYS);
        Label targetDirectoryLabel = new Label("Target directory");
        targetDirectoryLabel.setLabelFor(targetDirectoryField);

        ComboBox<EncryptorType> encryptorTypeBox = new ComboBox<>(FXCollections.observableArrayList(EncryptorType.values()));
        encryptorTypeBox.setMaxWidth(Double.MAX_VALUE);
        encryptorTypeBox.setPrefWidth(0);
        encryptorTypeBox.valueProperty().bindBidirectional(collector.encryptorTypeProperty());
        GridPane.setHgrow(encryptorTypeBox, Priority.ALWAYS);
        Label encryptorTypeLabel = new Label("Encryption type");
        encryptorTypeLabel.setLabelFor(encryptorTypeBox);

        TextField passwordField = new PasswordField();
        passwordField.textProperty().bindBidirectional(collector.passwordProperty());
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        Label passwordLabel = new Label("Password");
        passwordLabel.setLabelFor(passwordField);

        TextField passwordConfirmationField = new PasswordField();
        passwordConfirmationField.textProperty().bindBidirectional(collector.passwordConfirmationProperty());
        ComponentDecorator.addValidationDecorator(passwordConfirmationField, collector.passwordsMatchProperty());
        GridPane.setHgrow(passwordConfirmationField, Priority.ALWAYS);
        Label passwordConfirmationLabel = new Label("Password confirmation");
        passwordConfirmationLabel.setLabelFor(passwordConfirmationField);

        TextField passwordHintField = new TextField();
        passwordHintField.textProperty().bindBidirectional(collector.passwordHintProperty());
        GridPane.setHgrow(passwordHintField, Priority.ALWAYS);
        Label passwordHintLabel = new Label("Password hint");
        passwordHintLabel.setLabelFor(passwordHintField);

        Button executeButton = new Button("Execute collection", new FontIcon(MaterialDesignP.PLAY_BOX));
        executeButton.setOnAction(action -> collector.startJob(inputScannerCollection));
        executeButton.disableProperty().bind(Bindings.not(collector.readyProperty()).or(Bindings.isEmpty(inputScannerCollection.getInputScanners())));
        executeButton.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(executeButton, Priority.ALWAYS);

        GridPane collectorPane = new GridPane();
        collectorPane.add(targetDirectoryLabel, 0, 0, 1, 1);
        collectorPane.add(targetDirectoryPane, 1, 0, 1, 1);
        collectorPane.add(encryptorTypeLabel, 0, 1, 1, 1);
        collectorPane.add(encryptorTypeBox, 1, 1, 1, 1);
        collectorPane.add(passwordLabel, 0, 2, 1, 1);
        collectorPane.add(passwordField, 1, 2, 1, 1);
        collectorPane.add(passwordConfirmationLabel, 0, 3, 1, 1);
        collectorPane.add(passwordConfirmationField, 1, 3, 1, 1);
        collectorPane.add(passwordHintLabel, 0, 4, 1, 1);
        collectorPane.add(passwordHintField, 1, 4, 1, 1);
        collectorPane.add(executeButton, 0, 5, 2, 1);
        collectorPane.setHgap(12);
        collectorPane.setVgap(3);

        TitledPane collectorTitledPane = new TitledPane("Collector", collectorPane);
        collectorTitledPane.setCollapsible(false);
        collectorTitledPane.setGraphic(new FontIcon(MaterialDesignA.APPLICATION_SETTINGS));
        return collectorTitledPane;

    }

}
