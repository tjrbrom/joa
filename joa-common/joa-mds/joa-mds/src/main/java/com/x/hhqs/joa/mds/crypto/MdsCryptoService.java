package com.x.hhqs.joa.mds.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class MdsCryptoService implements CryptoService {
    private static final String ENCODING = "UTF-8";
    private static final Pattern ENCR_STR_PATTERN = Pattern.compile("(\\$\\-([0-9])+\\$(.+))");

    private Map<Integer, CipherSpec> cipherSpecs = new LinkedHashMap<>();

    private boolean disabled = false;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${secretkey.path}")
    String secretKeyPath;

    @PostConstruct
    public void init() {
        Resource resource = resourceLoader.getResource(secretKeyPath);
        if (resource.exists()) {
            try {
                loadKeys(resource);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load keys from " + secretKeyPath);
            }
        } else {
            throw new RuntimeException("Not a valid file " + secretKeyPath);
        }
    }

    protected void loadKeys(Resource resource) throws IOException {
        synchronized (cipherSpecs) {
            try (BufferedReader br =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String keyIndexStr;
                while ((keyIndexStr = br.readLine()) != null && keyIndexStr.length() != 0) {
                    int keyIndex = Integer.parseInt(keyIndexStr);
                    ByteBuffer decoded = getKeyBytesSafely(br);
                    byte[] array = decoded.array();
                    // clear
                    // clearByteBuffer(decoded);

                    SecretKeySpec keySpec = new SecretKeySpec(array, "AES");
                    // clear
                    // Arrays.fill(array, (byte)0);

                    int ivlength = Integer.parseInt(br.readLine());
                    String transformation = br.readLine();
                    cipherSpecs.put(keyIndex, new CipherSpec(keySpec, ivlength, transformation));
                }

            }
        }
    }

    private ByteBuffer getKeyBytesSafely(BufferedReader br) throws IOException {
        char[] temp = new char[1000];
        int offset = 0;
        br.read(temp, offset, 1);
        while (temp[offset] != '\n') {
            offset++;
            br.read(temp, offset, 1);
        }

        char[] keyChars = Arrays.copyOf(temp, offset);
        // clear
        Arrays.fill(temp, '0');

        CharBuffer charBuffer = CharBuffer.wrap(keyChars);

        ByteBuffer byteBuffer = Charset.forName(ENCODING).encode(charBuffer);

        // clear
        Arrays.fill(keyChars, '0');
        clearCharBuffer(keyChars, charBuffer);

        ByteBuffer decoded = Base64.getDecoder().decode(byteBuffer);
        clearByteBuffer(byteBuffer);
        return decoded;
    }

    private static void clearCharBuffer(char[] keyChars, CharBuffer charBuffer) {
        charBuffer.clear();
        charBuffer.put(keyChars);
        charBuffer.clear();
    }

    private void clearByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.put(new byte[byteBuffer.limit()]);
        byteBuffer.clear();
    }

    public String encrypt(String data) {
        if (disabled) {
            return data;
        }
        Set<Integer> keyIndices = getEncrCipherSpecs().keySet();
        if (keyIndices.isEmpty()) {
            throw new RuntimeException("Encryption key not found");
        }
        int keyIndex = keyIndices.iterator().next(); // Pick the first key. When key rotation is supported, this would
                                                     // be made configurable
        return encrypt(keyIndex, data);
    }

    protected String encrypt(int keyIndex, String data) {
        if (disabled) {
            return data;
        }
        byte[] dataBytes = new byte[0];
        try {
            dataBytes = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return encrypt(keyIndex, dataBytes);
    }

    private String encrypt(int keyIndex, byte[] dataBytes) {
        Cipher cipher = getCipherSpec(keyIndex).getEncrCipher();
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("$-" + keyIndex + "$");
            byte[] iv = cipher.getIV();
            byte[] encrBytes = cipher.doFinal(dataBytes);
            byte[] merged = new byte[iv.length + encrBytes.length];

            System.arraycopy(iv, 0, merged, 0, iv.length);
            System.arraycopy(encrBytes, 0, merged, iv.length, encrBytes.length);
            sb.append(Base64.getEncoder().encodeToString(merged));
            return sb.toString();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String decrypt(String data) {
        if (disabled) {
            return data;
        }
        Matcher matcher = ENCR_STR_PATTERN.matcher(data);
        if (matcher.matches()) {
            try {
                int keyIndex = Integer.valueOf(matcher.group(2));
                byte[] ivEncr = Base64.getDecoder().decode(matcher.group(3).getBytes());
                byte[] decry = getCipherSpec(keyIndex).decrypt(ivEncr);
                return new String(decry);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot decrypt the string ", e);
            }
        }
        throw new IllegalArgumentException("Cannot decrypt the string " + data);
    }

    private CipherSpec getCipherSpec(int keyIndex) {
        return getEncrCipherSpecs().get(keyIndex);
    }

    public Map<Integer, CipherSpec> getEncrCipherSpecs() {
        return cipherSpecs;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
