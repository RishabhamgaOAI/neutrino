package com.observeai.platform.realtime.neutrino.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomUtil {
	private static final SecureRandom random = new SecureRandom();
	private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

	public static String random() {
		byte[] buffer = new byte[20];
		random.nextBytes(buffer);
		return encoder.encodeToString(buffer);
	}
}
