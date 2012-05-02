package com.exfe.android;

import java.net.MalformedURLException;
import java.net.URL;

import android.text.TextUtils;

public class Const {
	public static final String username = "stonyw@gmail.com";
	public static final String pwd = "123123";
	
	public static final String EMPTY = "";
	
	public static final String DEFAULT_API_PROTOCAL = "https";
	public static final String DEFAULT_API_SERVER_DOMAIN = "api.exfe.com";
	public static final String DEFAULT_API_SERVER_PORT = ""; 
	public static final String DEFAULT_API_SERVER_ROOT_PATH = "/v2"; 
	
	public static URL getDefaultURL(String protocal, String domain,
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
	
	
}
