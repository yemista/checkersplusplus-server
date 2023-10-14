package com.checklersplusplus.server.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtil {

	private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
	
	public static String encryptPassword(String password) {
		try {
			return toHexString(getSHA(password));
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to encrypt password.", e);
			throw new RuntimeException();
		}
	}

	private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}

	private static String toHexString(byte[] hash) {
		BigInteger number = new BigInteger(1, hash);
		StringBuilder hexString = new StringBuilder(number.toString(16));

		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}

		return hexString.toString();
	}

}
