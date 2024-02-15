package de.perdian.apps.securebackup.modules.preferences;

public interface PreferencesStorageDelegate {

    String loadProperty(String propertyName) throws Exception;

    void writeProperty(String propertyName, String propertyValue) throws Exception;

}
