package com.exfe.android.model.entity;

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
}
