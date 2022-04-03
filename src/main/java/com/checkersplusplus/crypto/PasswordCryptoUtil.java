package com.checkersplusplus.crypto;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordCryptoUtil {

	public static String encryptPasswordForDatabase(String password) {
		return DigestUtils.md5Hex(password).toUpperCase();
	}
}
