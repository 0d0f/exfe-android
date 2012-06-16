package com.exfe.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.text.TextUtils;

public class Const {

	public static final String username = "stonyw@gmail.com";
	public static final String pwd = "123123";
	public static final boolean debug_login_auto_fill = BuildConfig.DEBUG ? true
			: false;

	public static final String PUSH_SERVER_ACCOUNT = "stony@exfe.com";
	public static final String C2DM_FIELD_APP = "app";
	public static final String C2DM_FIELD_SENDER = "sender";
	public static final String PROVIDER_EMAIL = "email";
	public static final String PROVIDER_ANDROID = "Android";
	public static final String PROVIDER_IOS = "iOSAPN";

	public static final DateFormat STD_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);;
	static {
		STD_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static final String EMPTY = "";

	public static final String TWITTER_OAUTH_PAGE = "https://api.twitter.com/oauth/authenticate?oauth_token=";

	public static final String DEFAULT_API_PROTOCAL = "https";
	public static final String DEFAULT_API_SERVER_DOMAIN = "api.exfe.com";
	public static final String DEFAULT_API_SERVER_PORT = "";
	public static final String DEFAULT_API_SERVER_ROOT_PATH = "/v2";

	public static final String DEFAULT_OAUTH_PROTOCAL = "http";
	public static final String DEFAULT_OAUTH_SERVER_DOMAIN = BuildConfig.DEBUG ? "dev.0d0f.com"
			: "exfe.com";
	public static final String DEFAULT_OAUTH_SERVER_PORT = "";
	public static final String DEFAULT_OAUTH_SERVER_ROOT_PATH = "/oAuth";
	public static final long HALF_HOUR = 30 * 60 * 1000;

	public static URL getDefaultAPIURL(String protocal, String domain,
			String port, String pathRoot) {
		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(protocal)) {
			sb.append(protocal);
		} else {
			sb.append(Const.DEFAULT_API_PROTOCAL);
		}

		sb.append("://");

		if (!TextUtils.isEmpty(domain)) {
			sb.append(domain);
		} else {
			sb.append(Const.DEFAULT_API_SERVER_DOMAIN);
		}

		String p = null;
		if (port != null) {
			p = port;
		} else {
			p = Const.DEFAULT_API_SERVER_PORT;
		}
		if (!TextUtils.isEmpty(p)) {
			sb.append(":");
			sb.append(p);
		}

		sb.append("/");

		String r = null;
		if (!TextUtils.isEmpty(pathRoot)) {
			r = pathRoot;
		} else {
			r = Const.DEFAULT_API_SERVER_ROOT_PATH;
		}
		if (r != null && r.startsWith("/")) {
			r = r.substring(1);
		}
		if (!TextUtils.isEmpty(r)) {
			sb.append(r);
		}

		URL url = null;
		try {
			url = new URL(sb.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String getOAuthURL(String entry) {
		return String.format("%s://%s%s/%s", DEFAULT_OAUTH_PROTOCAL,
				DEFAULT_OAUTH_SERVER_DOMAIN, DEFAULT_OAUTH_SERVER_ROOT_PATH,
				entry);
	}

}
