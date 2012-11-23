package com.exfe.android.model.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class Provider {

	public static final String STR_UNKNOWN = "";
	public static final String STR_EMAIL = "email";
	public static final String STR_TWITTER = "twitter";
	public static final String STR_FACEBOOK = "facebook";
	public static final String STR_ANDROID = "Android";
	public static final String STR_IOS = "iOS";

	public static final int UNKNOWN = 0;
	public static final int EMAIL = 1;
	public static final int TWITTER = 2;
	public static final int FACEBOOK = 3;
	public static final int ANDROID = -1;
	public static final int IOS = -2;

	public static Pattern PATTERN_TWITTER = Pattern.compile("@[\\w_]+");
	public static Pattern PATTERN_IDENTITY_PRIVIDER = Pattern
			.compile("^([\\w!#$%^&*+_=\\-{}|'?/.~]+)@([\\w!#$%^&*+_=\\-{}|'?/~]+)$");
	public static Pattern PATTERN_EMAIL = Pattern
			.compile("^[\\w!#$%^&*+_=\\-{}|'?/.~]+@[\\w!#$%^&*+_=\\-{}|'?/~]+(\\.[\\w!#$%^&*+_=\\-{}|'?/~]+)+$");

	public static String getString(int provider) {
		switch (provider) {
		case EMAIL:
			return STR_EMAIL;
		case TWITTER:
			return STR_TWITTER;
		case ANDROID:
			return STR_ANDROID;
		case IOS:
			return STR_IOS;
		default:
			return STR_UNKNOWN;
		}
	}

	public static int getValue(String provider) {
		if (STR_EMAIL.equalsIgnoreCase(provider)) {
			return EMAIL;
		} else if (STR_TWITTER.equalsIgnoreCase(provider)) {
			return TWITTER;
		} else if (STR_ANDROID.equalsIgnoreCase(provider)) {
			return ANDROID;
		} else if (STR_IOS.equalsIgnoreCase(provider)) {
			return IOS;
		} else {
			return UNKNOWN;
		}
	}

	public static int checkType(String identity) {
		if (TextUtils.isEmpty(identity)) {
			return UNKNOWN;
		}

		// email, username@provider
		if (identity.contains("@")) {

			Matcher m = PATTERN_EMAIL.matcher(identity);
			if (m.matches()) {
				return EMAIL;
			}

			m = PATTERN_TWITTER.matcher(identity);
			if (m.matches()) {
				return TWITTER;
			}

			m = PATTERN_IDENTITY_PRIVIDER.matcher(identity);
			if (m.matches()) {
				return getValue(m.group(2));
			}
			
			return UNKNOWN;
		}

		// phone number?

		return UNKNOWN;
	}
	
	public static String extraExternalUsername(String identity, int provider){
		switch(provider){
		case EMAIL:
			return identity;
			//break;
		case TWITTER:
			if (identity.startsWith("@")){
				return identity.substring("@".length());
			}
			// fall back to common patter 
		case FACEBOOK:
			return identity.substring(0, identity.lastIndexOf("@"));
			//break;
		}
		
		return STR_UNKNOWN;
	}
}
