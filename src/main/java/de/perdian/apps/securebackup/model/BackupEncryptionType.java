package de.perdian.apps.securebackup.model;

import de.perdian.apps.securebackup.model.impl.OpenSslEncryptor;

import java.util.function.Supplier;

public enum BackupEncryptionType {

    OPENSSL(OpenSslEncryptor::new);

    private Supplier<BackupEncryptor> encryptorFactory = null;

    BackupEncryptionType(Supplier<BackupEncryptor> encryptorFactory) {
        this.setEncryptorFactory(encryptorFactory);
    }

    public BackupEncryptor createEncryptor() {
        return this.getEncryptorFactory().get();
    }
    private Supplier<BackupEncryptor> getEncryptorFactory() {
        return this.encryptorFactory;
    }
    private void setEncryptorFactory(Supplier<BackupEncryptor> encryptorFactory) {
        this.encryptorFactory = encryptorFactory;
    }
}
