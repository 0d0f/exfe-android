package com.exfe.android.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	public static String getMD5(String val) throws NoSuchAlgorithmException,
			IOException {
		InputStream in = new ByteArrayInputStream(val.getBytes());
		byte[] digest = getMD5(in);
		return getString(digest);
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	public static byte[] getMD5(InputStream in)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest digester = MessageDigest.getInstance("MD5");
		byte[] bytes = new byte[8192];
		int byteCount;
		while ((byteCount = in.read(bytes)) > 0) {
			digester.update(bytes, 0, byteCount);
		}
		return digester.digest();
	}
}
