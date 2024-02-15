package de.perdian.apps.securebackup.modules.encryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface Encryptor {

    OutputStream createEncryptedOutputStream(String password, Path targetFile) throws IOException;

    InputStream createDecryptedInputStream(String password, Path sourceFile) throws IOException;

    String createEncryptedFileName(String baseFileName);

}
