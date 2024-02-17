package de.perdian.securebackup.examples;

import de.perdian.apps.securebackup.modules.encryptor.impl.OpenSslEncryptor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// https://gist.github.com/thiamteck/798343b9e4a5d7df748746d995eba53e
// https://blog.idrsolutions.com/how-to-use-cipher-streams-in-java/

public class EncryptionExample {

    public static void main(String[] args) throws Exception {

        OpenSslEncryptor encryptor = new OpenSslEncryptor();

        String message = "this is a 🤣 test\n";
        String password = "test";

        String targetFileName = encryptor.createEncryptedFileName("example.txt");
        Path targetDirectory = Paths.get(System.getProperty("user.home"), "Downloads/");
        Path targetFile = targetDirectory.resolve(targetFileName);

        try (OutputStream fileStream = Files.newOutputStream(targetFile)) {
            try (OutputStream targetStream = encryptor.createEncryptedOutputStream(password, fileStream)) {
                targetStream.write(message.getBytes());
            }
        }

        try (InputStream fileStream = Files.newInputStream(targetFile)) {
            try (InputStream sourceStream = encryptor.createDecryptedInputStream(password, fileStream)) {
                ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
                IOUtils.copy(sourceStream, targetStream);
                String targetString = targetStream.toString(StandardCharsets.UTF_8);

                System.err.println("Cleartext (before):     " + message.strip());
                System.err.println("Cleartext (before) HEX: " + Hex.encodeHexString(message.getBytes()));
                System.err.println("Encrypted:              " + Hex.encodeHexString(Files.readAllBytes(targetFile)));
                System.err.println("Cleartext (after):      " + targetString.strip());
                System.err.println("Cleartext (after) HEX:  " + Hex.encodeHexString(targetString.getBytes()));
            }
        }

    }

}
