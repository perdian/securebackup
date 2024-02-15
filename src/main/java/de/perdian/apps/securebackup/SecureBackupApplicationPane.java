package de.perdian.apps.securebackup;

import de.perdian.apps.securebackup.modules.collector.CollectorActionsPane;
import de.perdian.apps.securebackup.modules.collector.CollectorLogPane;
import de.perdian.apps.securebackup.modules.collector.CollectorSettings;
import de.perdian.apps.securebackup.modules.collector.CollectorSettingsPane;
import de.perdian.apps.securebackup.modules.sources.SourceCollection;
import de.perdian.apps.securebackup.modules.sources.SourceCollectionPane;
import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

class SecureBackupApplicationPane extends GridPane {

    SecureBackupApplicationPane(CollectorSettings collectorSettings, SourceCollection sourcesSettings) {

        CollectorSettingsPane collectorSettingsPane = new CollectorSettingsPane(collectorSettings);
        collectorSettingsPane.setPadding(new Insets(10, 10, 10, 10));
        TitledPane collectorSettingsTitledPane = new TitledPane("Collector", collectorSettingsPane);
        collectorSettingsTitledPane.setGraphic(new FontIcon(MaterialDesignA.APPLICATION_SETTINGS));
        collectorSettingsTitledPane.setCollapsible(false);
        collectorSettingsTitledPane.setMaxWidth(Double.MAX_VALUE);
        collectorSettingsTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setFillHeight(collectorSettingsTitledPane, true);
        GridPane.setHgrow(collectorSettingsTitledPane, Priority.ALWAYS);

        CollectorActionsPane collectorActionsPane = new CollectorActionsPane(collectorSettings);
        collectorActionsPane.setPadding(new Insets(10, 10, 10, 10));
        TitledPane collectorActionsTitledPane = new TitledPane("Actions", collectorActionsPane);
        collectorActionsTitledPane.setGraphic(new FontIcon(MaterialDesignP.PLAY_BOX_OUTLINE));
        collectorActionsTitledPane.setCollapsible(false);
        collectorActionsTitledPane.setMaxWidth(Double.MAX_VALUE);
        collectorActionsTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setFillHeight(collectorActionsTitledPane, true);

        SourceCollectionPane sourcesSettingsPane = new SourceCollectionPane(sourcesSettings);
        sourcesSettingsPane.setPadding(new Insets(10, 10, 10, 10));
        TitledPane sourcesSettingsTitledPane = new TitledPane("Sources", sourcesSettingsPane);
        sourcesSettingsTitledPane.setGraphic(new FontIcon(MaterialDesignF.FOLDER_SETTINGS_OUTLINE));
        sourcesSettingsTitledPane.setCollapsible(false);
        sourcesSettingsTitledPane.setMaxWidth(Double.MAX_VALUE);
        sourcesSettingsTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setHgrow(sourcesSettingsTitledPane, Priority.ALWAYS);
        GridPane.setVgrow(sourcesSettingsTitledPane, Priority.ALWAYS);

        CollectorLogPane collectorLogPane = new CollectorLogPane();
        collectorLogPane.setPadding(new Insets(10, 10, 10, 10));
        TitledPane collectorLogTitledPane = new TitledPane("Collector log", collectorLogPane);
        collectorLogTitledPane.setGraphic(new FontIcon(MaterialDesignC.CLIPBOARD_LIST_OUTLINE));
        collectorLogTitledPane.setMinWidth(600);
        collectorLogTitledPane.setCollapsible(false);
        collectorLogTitledPane.setMaxWidth(1000);
        collectorLogTitledPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setFillHeight(collectorLogTitledPane, true);

        this.add(collectorSettingsTitledPane, 0, 0, 1, 1);
        this.add(collectorActionsTitledPane, 1, 0, 1, 1);
        this.add(sourcesSettingsTitledPane, 0, 1, 1, 1);
        this.add(collectorLogTitledPane, 1, 1, 1, 1);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setHgap(10);
        this.setVgap(10);

    }

}
