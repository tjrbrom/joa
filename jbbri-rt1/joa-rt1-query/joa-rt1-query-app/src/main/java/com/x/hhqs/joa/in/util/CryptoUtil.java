package com.x.hhqs.joa.in.util;

import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RefreshScope
public class CryptoUtil {

	private SecretKeySpec secretKey;

	private IvParameterSpec ivParameterSpec;

	@Value("${core.password.algorithm}")
	private String algorithm;

	@Value("${core.password.key}")
	private String base64EncodedKey;

	@PostConstruct
	private void configureKeyAndAlgorithm() {
		byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);
		secretKey = new SecretKeySpec(keyBytes, "AES");

		byte[] iv = new byte[16];
		ivParameterSpec = new IvParameterSpec(iv);
	}

	public String encrypt(String data) {
		try {
			if (StringUtils.isEmpty(data)) {
				return "";
			}

			Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
			StringBuilder sb = new StringBuilder();
			sb.append("$-0$");
			byte[] iv = cipher.getIV();
			byte[] encrBytes = cipher.doFinal(data.getBytes());
			byte[] merged = new byte[iv.length + encrBytes.length];

			System.arraycopy(iv, 0, merged, 0, iv.length);
			System.arraycopy(encrBytes, 0, merged, iv.length, encrBytes.length);
			sb.append(Base64.getEncoder().encodeToString(merged));
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String decrypt(String encryptedData) {
		try {
			if (StringUtils.isEmpty(encryptedData)) {
				return "";
			}

			String[] parts = encryptedData.split("\\$-\\d+\\$");
			byte[] merged = Base64.getDecoder().decode(parts[1]);
			byte[] iv = new byte[16];
			byte[] encrBytes = new byte[merged.length - 16];

			System.arraycopy(merged, 0, iv, 0, iv.length);
			System.arraycopy(merged, iv.length, encrBytes, 0, encrBytes.length);

			Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			byte[] decryptedBytes = cipher.doFinal(encrBytes);
			return new String(decryptedBytes);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Cipher getCipher(int mode) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(mode, secretKey, ivParameterSpec);
		return cipher;
	}

}
