package de.perdian.apps.securebackup.support.fx.converters;

import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

public class PathStringConverter extends StringConverter<Path> {

    @Override
    public String toString(Path object) {
        return object == null ? null : object.toString();
    }

    @Override
    public Path fromString(String string) {
        return StringUtils.isEmpty(string) ? null : Path.of(string);
    }

}
