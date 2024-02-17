package de.perdian.apps.securebackup.modules.encryptor.impl;

import de.perdian.apps.securebackup.modules.encryptor.Encryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NoEncryptor implements Encryptor {

    @Override
    public OutputStream createEncryptedOutputStream(String password, OutputStream targetStream) throws IOException {
        return targetStream;
    }

    @Override
    public InputStream createDecryptedInputStream(String password, InputStream sourceStream) throws IOException {
        return sourceStream;
    }

    @Override
    public String createEncryptedFileName(String baseFileName) {
        return baseFileName;
    }

}
