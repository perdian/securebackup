package de.perdian.securebackup.examples;

import de.perdian.apps.securebackup.modules.encryptor.impl.OpenSslEncryptor;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

// https://gist.github.com/thiamteck/798343b9e4a5d7df748746d995eba53e
// https://blog.idrsolutions.com/how-to-use-cipher-streams-in-java/

public class EncryptionExample {


    public static void main(String[] args) throws Exception {

        String encryptorPassword = "test";
        OpenSslEncryptor encryptor = new OpenSslEncryptor();
        encryptor.setRandom(new Random(4711));

        byte[] cleartextBytesOriginal = "this is a 🤣 test\n".getBytes();

        ByteArrayOutputStream encryptedBytesOutputStream = new ByteArrayOutputStream();
        try (OutputStream encryptorOutputStream = encryptor.createEncryptedOutputStream(encryptorPassword, encryptedBytesOutputStream)) {
            encryptorOutputStream.write(cleartextBytesOriginal);
            encryptorOutputStream.flush();
        }
        byte[] encryptedBytes = encryptedBytesOutputStream.toByteArray();

        ByteArrayOutputStream reloadedBytesOutputStream = new ByteArrayOutputStream();
        try (ByteArrayInputStream encryptedBytesStream = new ByteArrayInputStream(encryptedBytes)) {
            try (InputStream encryptorInputStream = encryptor.createDecryptedInputStream(encryptorPassword, encryptedBytesStream)) {
                IOUtils.copy(encryptorInputStream, reloadedBytesOutputStream);
            }
        }
        byte[] reloadedBytes = reloadedBytesOutputStream.toByteArray();

        System.out.println(""
            + "\n- original  [" + cleartextBytesOriginal.length + "] = " // + Hex.encodeHexString(cleartextBytesOriginal)
            + "\n- encrypted [" + encryptedBytes.length         + "] = " //+ Hex.encodeHexString(encryptedBytes)
            + "\n- reloaded  [" + reloadedBytes.length          + "] = " //+ Hex.encodeHexString(reloadedBytes)
        );

        if (!Arrays.equals(cleartextBytesOriginal, reloadedBytes)) {
            throw new IllegalArgumentException("Reloaded byte array doesn't equal original byte array");
        }

    }

}
