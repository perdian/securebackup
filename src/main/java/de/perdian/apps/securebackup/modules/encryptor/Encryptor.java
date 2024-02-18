package de.perdian.apps.securebackup.modules.encryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Encryptor {

    OutputStream createEncryptedOutputStream(String password, OutputStream targetStream) throws IOException;

    InputStream createDecryptedInputStream(String password, InputStream sourceStream) throws IOException;

    String createEncryptedFileName(String baseFileName);

    default String createReadme() {
        return null;
    }

}
