package de.perdian.apps.securebackup.modules.encryptor;

import de.perdian.apps.securebackup.modules.encryptor.impl.OpenSslEncryptor;
import de.perdian.apps.securebackup.modules.encryptor.impl.NoEncryptor;

import java.util.function.Supplier;

public enum EncryptorType {

    OPENSSL(OpenSslEncryptor::new),
    NONE(NoEncryptor::new);

    private Supplier<Encryptor> encryptorFactory = null;

    EncryptorType(Supplier<Encryptor> encryptorFactory) {
        this.setEncryptorFactory(encryptorFactory);
    }

    public Encryptor createEncryptor() {
        return this.getEncryptorFactory().get();
    }
    private Supplier<Encryptor> getEncryptorFactory() {
        return this.encryptorFactory;
    }
    private void setEncryptorFactory(Supplier<Encryptor> encryptorFactory) {
        this.encryptorFactory = encryptorFactory;
    }
}
