package de.perdian.apps.securebackup.modules.preferences.impl;

import de.perdian.apps.securebackup.modules.preferences.PreferencesStorageDelegate;
import org.apache.commons.lang3.StringUtils;
import pt.davidafsilva.apple.OSXKeychain;

import java.util.Objects;
import java.util.Optional;

public class MacOSKeychainStorageDelegate implements PreferencesStorageDelegate {

    public static final String DEFAULT_APPLICATION_NAME = "de.perdian.apps.securebackup";

    private String applicationName = null;
    private OSXKeychain keychain = null;

    public MacOSKeychainStorageDelegate() throws Exception {
        this(DEFAULT_APPLICATION_NAME);
    }

    public MacOSKeychainStorageDelegate(String applicationName) throws Exception {
        this.setApplicationName(StringUtils.defaultIfEmpty(applicationName, DEFAULT_APPLICATION_NAME));
        this.setKeychain(OSXKeychain.getInstance());
    }

    @Override
    public String loadProperty(String propertyName) throws Exception {
        return this.getKeychain().findGenericPassword(this.getApplicationName(), propertyName).orElse(null);
    }

    @Override
    public void writeProperty(String propertyName, String newPropertyValue) throws Exception {
        Optional<String> existingPasswordValue = this.getKeychain().findGenericPassword(this.getApplicationName(), propertyName);
        if (StringUtils.isEmpty(newPropertyValue) && existingPasswordValue.isPresent()) {
            this.getKeychain().deleteGenericPassword(this.getApplicationName(), propertyName);
        } else if (StringUtils.isNotEmpty(newPropertyValue) && existingPasswordValue.isEmpty()) {
            this.getKeychain().addGenericPassword(this.getApplicationName(), propertyName, newPropertyValue);
        } else if (StringUtils.isNotEmpty(newPropertyValue) && existingPasswordValue.isPresent() && !Objects.equals(existingPasswordValue.get(), newPropertyValue)) {
            this.getKeychain().modifyGenericPassword(this.getApplicationName(), propertyName, newPropertyValue);
        }
    }

    public String getApplicationName() {
        return this.applicationName;
    }
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    private OSXKeychain getKeychain() {
        return this.keychain;
    }
    private void setKeychain(OSXKeychain keychain) {
        this.keychain = keychain;
    }

}
