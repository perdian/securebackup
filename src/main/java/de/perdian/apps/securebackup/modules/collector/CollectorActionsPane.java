package de.perdian.apps.securebackup.modules.collector;

import de.perdian.apps.securebackup.modules.sources.SourceCollection;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

public class CollectorActionsPane extends GridPane {

    public CollectorActionsPane(SourceCollection sourcesCollection, CollectorSettings collectorSettings, ObjectProperty<Collector> collectorProperty, BooleanProperty busyProperty) {

        Button executeButton = new Button("Execute backup", new FontIcon(MaterialDesignP.PLAY_BOX));
        executeButton.setOnAction(new CollectorActionEventHandler(sourcesCollection, collectorSettings, collectorProperty, busyProperty));
        executeButton.disableProperty().bind(Bindings.not(collectorSettings.validProperty()));
        this.add(executeButton, 0, 0, 1, 1);

    }

}
