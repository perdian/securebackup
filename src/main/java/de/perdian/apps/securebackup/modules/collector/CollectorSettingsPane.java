package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.encryptor.EncryptorType;
import de.perdian.apps.securebackup.support.fx.actions.SelectDirectoryIntoPropertyActionEventHandler;
import de.perdian.apps.securebackup.support.fx.bindings.PathBindings;
import de.perdian.apps.securebackup.support.fx.converters.PathStringConverter;
import de.perdian.apps.securebackup.support.fx.decoration.TextFieldDecorator;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;

import java.nio.file.Path;

public class CollectorSettingsPane extends GridPane {

    public CollectorSettingsPane(CollectorSettings collectorSettings) {

        Label targetDirectoryLabel = new Label("Target directory");
        TextFormatter<Path> targetDirectoryFormatter = new TextFormatter<Path>(new PathStringConverter());
        targetDirectoryFormatter.valueProperty().bindBidirectional(collectorSettings.targetDirectoryProperty());
        TextField targetDirectoryField = new TextField();
        targetDirectoryField.setTextFormatter(targetDirectoryFormatter);
        TextFieldDecorator.bindErrorBackground(targetDirectoryField, PathBindings.exists(collectorSettings.targetDirectoryProperty()));
        GridPane.setHgrow(targetDirectoryField, Priority.ALWAYS);
        Button targetDirectorySelectButton = new Button(null, new FontIcon(MaterialDesignF.FOLDER));
        targetDirectorySelectButton.setOnAction(new SelectDirectoryIntoPropertyActionEventHandler(collectorSettings.targetDirectoryProperty()));
        targetDirectorySelectButton.setTooltip(new Tooltip("Select target directory"));
        targetDirectorySelectButton.setMaxHeight(Double.MAX_VALUE);
        GridPane targetDirectorySelectPane = new GridPane(2, 0);
        targetDirectorySelectPane.add(targetDirectoryField, 0, 0, 1, 1);
        targetDirectorySelectPane.add(targetDirectorySelectButton, 1, 0, 1, 1);
        GridPane.setFillHeight(targetDirectorySelectButton, true);
        GridPane.setHgrow(targetDirectorySelectPane, Priority.ALWAYS);
        targetDirectoryLabel.setLabelFor(targetDirectoryField);

        Label encryptorTypeLabel = new Label("Encryption type");
        ComboBox<EncryptorType> encryptorTypeBox = new ComboBox<>(FXCollections.observableArrayList(EncryptorType.values()));
        encryptorTypeBox.setMaxWidth(Double.MAX_VALUE);
        encryptorTypeBox.setPrefWidth(0);
        encryptorTypeBox.valueProperty().bindBidirectional(collectorSettings.encryptorTypeProperty());
        GridPane.setHgrow(encryptorTypeBox, Priority.ALWAYS);
        encryptorTypeLabel.setLabelFor(encryptorTypeBox);

        Label passwordLabel = new Label("Password");
        TextField passwordField = new PasswordField();
        passwordField.textProperty().bindBidirectional(collectorSettings.passwordProperty());
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        passwordLabel.setLabelFor(passwordField);

        Label passwordConfirmationLabel = new Label("Password confirmation");
        TextField passwordConfirmationField = new PasswordField();
        passwordConfirmationField.textProperty().bindBidirectional(collectorSettings.passwordConfirmationProperty());
        TextFieldDecorator.bindErrorBackground(passwordConfirmationField, collectorSettings.passwordsMatchProperty());
        GridPane.setHgrow(passwordConfirmationField, Priority.ALWAYS);
        passwordConfirmationLabel.setLabelFor(passwordConfirmationField);

collectorSettings.passwordConfirmationProperty().setValue(collectorSettings.passwordProperty().getValue());

        this.add(targetDirectoryLabel, 0, 0, 1, 1);
        this.add(targetDirectorySelectPane, 1, 0, 1, 1);
        this.add(encryptorTypeLabel, 0, 1, 1, 1);
        this.add(encryptorTypeBox, 1, 1, 1, 1);
        this.add(passwordLabel, 0, 2, 1, 1);
        this.add(passwordField, 1, 2, 1, 1);
        this.add(passwordConfirmationLabel, 0, 3, 1, 1);
        this.add(passwordConfirmationField, 1, 3, 1, 1);
        this.setHgap(10);
        this.setVgap(5);

    }

}
