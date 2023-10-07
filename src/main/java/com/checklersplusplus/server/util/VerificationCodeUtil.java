package com.checklersplusplus.server.util;

import java.util.UUID;

public class VerificationCodeUtil {

	public static String generateVerificationCode() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
	}
}
