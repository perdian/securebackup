package de.perdian.apps.securebackup.support.fx.properties.converters;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

public class LinesStringConverter extends StringConverter<ObservableList<String>> {

    @Override
    public String toString(ObservableList<String> lines) {
        if (lines == null) {
            return null;
        } else {
            return lines.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
        }
    }

    @Override
    public ObservableList<String> fromString(String string) {
        if (StringUtils.isEmpty(string)) {
            return FXCollections.emptyObservableList();
        } else {
            return FXCollections.observableArrayList(
                string.lines()
                    .filter(StringUtils::isNotBlank)
                    .map(String::strip)
                    .toList()
            );
        }
    }

}
