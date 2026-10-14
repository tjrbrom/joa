package com.x.hhqs.joa.mds.crypto;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CipherSpec {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final SecretKeySpec keySpec;
    private final int ivlength;
    private final String transformation;

    public CipherSpec(SecretKeySpec keySpec, int ivlength, String transformation) {
        this.keySpec = keySpec;
        this.ivlength = ivlength;
        this.transformation = transformation;

        // validating input params
        getEncrCipher();
        getDecrCipher(new byte[ivlength]);
    }

    public Cipher getDecrCipher(byte[] ivEncr) throws IllegalStateException {
        try {
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(ivEncr, 0, ivlength));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec, new SecureRandom());
            return cipher;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private IvParameterSpec getIVSpec() {
        byte iv[] = new byte[ivlength];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // self-seeded randomizer to generate IV
        return new IvParameterSpec(iv);
    }

    public Cipher getEncrCipher() throws IllegalStateException {
        try {
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = getIVSpec();
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec, new SecureRandom());
            return cipher;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] decrypt(byte[] ivEncr) {
        try {
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(ivEncr, 0, ivlength));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec, new SecureRandom());
            return cipher.doFinal(Arrays.copyOfRange(ivEncr, ivlength, ivEncr.length));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public int getIvlength() {
        return ivlength;
    }

    public String getTransformation() {
        return transformation;
    }
}
