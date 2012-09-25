package com.exfe.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.text.TextUtils;

public class Const {

	public static final boolean override_domain = false; //BuildConfig.DEBUG;

	public static final String FLURRY_APP_ID = "4B9VJPB4DC7VPTTCS3G6";
	public static final String PUSH_PROJECT_ID = override_domain ? "313001677673"
			: "905549742932";
	public static final String PUSH_SERVER_ACCOUNT = "stony@exfe.com";

	public static final String GCM_FIELD_APP = "app";
	public static final String GCM_FIELD_SENDER = "sender";

	public static final DateFormat UTC_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss Z", Locale.US);
	public static final DateFormat UTC_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.US);
	public static final DateFormat UTC_MONTH_FORMAT = new SimpleDateFormat(
			"MMM", Locale.US);
	public static final DateFormat UTC_DAY_FORMAT = new SimpleDateFormat("dd",
			Locale.US);
	public static final DateFormat UTC_Time_HHMM_FORMAT = new SimpleDateFormat(
			"hh:mm", Locale.US);
	static {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		UTC_DATE_FORMAT.setTimeZone(tz);
		UTC_MONTH_FORMAT.setTimeZone(tz);
		UTC_DAY_FORMAT.setTimeZone(tz);
		UTC_Time_HHMM_FORMAT.setTimeZone(tz);
	}

	public static final DateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat(
			"MMM dd, yyyy", Locale.US);
	public static final DateFormat LOCAL_TIME_FORMAT = new SimpleDateFormat(
			"hh:mm a", Locale.US);
	public static final DateFormat LOCAL_TIME_DATE_FORMAT = new SimpleDateFormat(
			"hh:mm a, MMM dd", Locale.US);
	public static final DateFormat LOCAL_FULL_FORMAT = new SimpleDateFormat(
			"ccc MMM dd hh:mm:ss a z yyyy", Locale.US);

	public static final String EMPTY = "";

	// public static final String TWITTER_OAUTH_PAGE =
	// "https://api.twitter.com/oauth/authenticate?oauth_token=";

	public static final String DEFAULT_API_PROTOCAL = "https";
	public static final String DEFAULT_API_SERVER_DOMAIN = "www.exfe.com";
	public static final String DEFAULT_API_SERVER_PORT = "";
	public static final String DEFAULT_API_SERVER_ROOT_PATH = "/v2";

	public static final String DEFAULT_IMG_DEFAULT_URL = Const.override_domain ? "http://img.dev.0d0f.com/web/80_80_%s"
			: "http://img.exfe.com/web/80_80_%s";
	public static final String DEFAULT_IMG_POOL_URL = Const.override_domain ? "http://img.dev.0d0f.com/%c/%c/80_80_%s"
			: "http://img.exfe.com/%c/%c/80_80_%s";

	public static final String DEFAULT_IMG_WIDGET_URL = Const.override_domain ? "http://dev.0d0f.com/static/img/xbg"
			: "http://exfe.com/static/img/xbg";

	public static final String DEFAULT_OAUTH_PROTOCAL = "http";
	public static final String DEFAULT_OAUTH_SERVER_DOMAIN = Const.override_domain ? "dev.0d0f.com"
			: "exfe.com";
	public static final String DEFAULT_OAUTH_SERVER_PORT = "";
	public static final String DEFAULT_OAUTH_SERVER_ROOT_PATH = "/oAuth";
	public static final long HALF_HOUR = 30 * 60 * 1000;
	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

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

	public static String getWidgetImgURL(String imgName) {
		return String.format("%s/%s", DEFAULT_IMG_WIDGET_URL, imgName);
	}

}
