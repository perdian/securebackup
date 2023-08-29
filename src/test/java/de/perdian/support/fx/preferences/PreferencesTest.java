package de.perdian.support.fx.preferences;

import com.google.common.jimfs.Jimfs;
import javafx.beans.property.StringProperty;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class PreferencesTest {

    @Test
    public void shouldCreatePreferencesFromStorageFile() throws IOException  {
        try (FileSystem fileSystem = Jimfs.newFileSystem()) {

            Path preferencesFile = fileSystem.getPath("preferences");
            Preferences originalPreferences = new Preferences(preferencesFile);
            StringProperty fooProperty = originalPreferences.resolveStringProperty("foo");
            StringProperty fooPropertyAgain = originalPreferences.resolveStringProperty("foo");
            MatcherAssert.assertThat(fooProperty.getValue(), IsNull.nullValue());

            fooProperty.setValue("fooValue");
            MatcherAssert.assertThat(fooProperty.getValue(), IsEqual.equalTo("fooValue"));
            MatcherAssert.assertThat(fooPropertyAgain.getValue(), IsEqual.equalTo("fooValue"));

            Preferences reloadedPreferences = new Preferences(preferencesFile);
            MatcherAssert.assertThat(reloadedPreferences.resolveStringProperty("foo").getValue(), IsEqual.equalTo("fooValue"));

        }
    }

}
