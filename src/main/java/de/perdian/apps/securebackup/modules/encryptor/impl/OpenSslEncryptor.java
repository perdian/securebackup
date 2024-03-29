package de.perdian.apps.securebackup.modules.encryptor.impl;

import de.perdian.apps.securebackup.modules.encryptor.Encryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

/**
 * Encrypts the content using the OpenSSL specific format
 *
 * (inspired by https://gist.github.com/thiamteck/798343b9e4a5d7df748746d995eba53e)
 *
 * Decryption via:
 * $ openssl enc -d -aes-256-cbc -pbkdf2 -pass pass:PASSWORD -in FILENAME
 */

public class OpenSslEncryptor implements Encryptor {

    private static final Logger log = LoggerFactory.getLogger(OpenSslEncryptor.class);

    private String keyAlgorithm = "AES";
    private String keyTransformation = "AES/CBC/PKCS5Padding";
    private int keySize = 32; // Bytes
    private String keyDerivationFunction = "PBKDF2WithHmacSHA256";
    private int keyIterationCount = 10000;
    private int ivSize = 16; // Bytes
    private int saltSize = 8; // Bytes
    private Random random = new SecureRandom();

    @Override
    public OutputStream createEncryptedOutputStream(String password, OutputStream targetStream) throws IOException {
        try {

            byte[] saltBytes = this.createSaltBytes();

            Cipher targetCipher = this.createCipher(Cipher.ENCRYPT_MODE, password, saltBytes);
            targetStream.write("Salted__".getBytes());
            targetStream.write(saltBytes);
            return new CipherOutputStream(targetStream, targetCipher);

        } catch (GeneralSecurityException e) {
            throw new IOException("Cannot initialize encryption environment", e);
        }
    }

    @Override
    public InputStream createDecryptedInputStream(String password, InputStream sourceStream) throws IOException {
        try {

            sourceStream.skip("Salted__".getBytes().length);
            byte[] saltBytes = sourceStream.readNBytes(this.getSaltSize());

            Cipher cipher = this.createCipher(Cipher.DECRYPT_MODE, password, saltBytes);
            return new CipherInputStream(sourceStream, cipher);

        } catch (GeneralSecurityException e) {
            throw new IOException("Cannot initialize encryption environment", e);
        }
    }

    byte[] createSaltBytes() {
        byte[] saltBytes = new byte[8];
        this.getRandom().nextBytes(saltBytes);
        return saltBytes;
    }

    Cipher createCipher(int mode, String password, byte[] saltBytes) throws GeneralSecurityException {

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(this.getKeyDerivationFunction());
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, this.getKeyIterationCount(), (this.getKeySize() + this.getIvSize()) * 8);
        byte[] keyAndIvBytes = keyFactory.generateSecret(keySpec).getEncoded();
        byte[] keyBytes = Arrays.copyOfRange(keyAndIvBytes, 0, this.getKeySize());
        SecretKey key = new SecretKeySpec(keyBytes, this.getKeyAlgorithm());
        byte[] ivBytes = Arrays.copyOfRange(keyAndIvBytes, this.getKeySize(), (this.getKeySize() + this.getIvSize()));

        Cipher encryptionCypher = Cipher.getInstance(this.getKeyTransformation());
        encryptionCypher.init(mode, key, new IvParameterSpec(ivBytes));
        return encryptionCypher;

    }

    @Override
    public String createEncryptedFileName(String baseFileName) {
        return baseFileName + ".encrypted.openssl";
    }

    @Override
    public String createReadme() {
        return
            """
            Decryption via:
            $ openssl enc -d -aes-256-cbc -pbkdf2 -pass pass:PASSWORD -out FILENAME -in FILENAME
            """;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getKeyTransformation() {
        return this.keyTransformation;
    }
    public void setKeyTransformation(String keyTransformation) {
        this.keyTransformation = keyTransformation;
    }

    public int getKeySize() {
        return this.keySize;
    }
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getKeyDerivationFunction() {
        return this.keyDerivationFunction;
    }
    public void setKeyDerivationFunction(String keyDerivationFunction) {
        this.keyDerivationFunction = keyDerivationFunction;
    }

    public int getKeyIterationCount() {
        return this.keyIterationCount;
    }
    public void setKeyIterationCount(int keyIterationCount) {
        this.keyIterationCount = keyIterationCount;
    }

    public int getIvSize() {
        return this.ivSize;
    }
    public void setIvSize(int ivSize) {
        this.ivSize = ivSize;
    }

    public int getSaltSize() {
        return this.saltSize;
    }
    public void setSaltSize(int saltSize) {
        this.saltSize = saltSize;
    }

    public Random getRandom() {
        return this.random;
    }
    public void setRandom(Random random) {
        this.random = random;
    }

}
