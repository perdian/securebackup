package de.perdian.apps.securebackup.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface BackupEncryptor {

    OutputStream createEncryptedOutputStream(String password, Path targetFile) throws IOException;

    InputStream createDecryptedInputStream(String password, Path sourceFile) throws IOException;

    String createEncryptedFileName(String baseFileName);

}
