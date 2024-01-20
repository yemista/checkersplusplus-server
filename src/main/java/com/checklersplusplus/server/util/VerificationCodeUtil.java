package com.checklersplusplus.server.util;

public class VerificationCodeUtil {

	public static String generateVerificationCode() {
		return "AAAAAA";
		//return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
	}
}
