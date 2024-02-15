package de.perdian.apps.securebackup.modules.collector;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

public class CollectorActionsPane extends GridPane {

    public CollectorActionsPane(CollectorSettings collectorSettings) {

        Button executeButton = new Button("Execute backup", new FontIcon(MaterialDesignP.PLAY_BOX));
        executeButton.disableProperty().bind(Bindings.not(collectorSettings.validProperty()));
        this.add(executeButton, 0, 0, 1, 1);

    }

}
