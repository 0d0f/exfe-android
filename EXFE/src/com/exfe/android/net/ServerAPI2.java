package com.exfe.android.net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Response;

public class ServerAPI2 {
	public static final String TAG = ServerAPI2.class.getSimpleName();
	// public static final String lineSeparator =
	// System.getProperty("line.separator");

	public static String OVERIDE_PROTOCAL = "http";
	public static String OVERIDE_DOMAIN = "api.0d0f.com";
	public static String OVERIDE_PORT = null;
	public static String OVERIDE_PATHROOT = null;

	private static final String FIELD_API_NAME = "API-Name";
	private static final String FIELD_CONTECT_TYPE = "Content-Type";
	private static final String FIELD_HTTP_TYPE = "HTTP-Type";

	private static final String FIELD_QUERY_PREFEIX = "QUERY_";
	private static final String FIELD_TOKEN = "QUERY_token";

	private Model mModel;
	private URL mServerApiRoot;

	private String mUsername;
	private String mAppKey;

	public ServerAPI2(Model model) {
		this(model, Const.getDefaultURL(OVERIDE_PROTOCAL, OVERIDE_DOMAIN,
				OVERIDE_PORT, OVERIDE_PATHROOT));
	}

	public ServerAPI2(Model model, URL apiRoot) {
		mModel = model;
		mUsername = model.getUsername();
		mAppKey = model.getToken();
		mServerApiRoot = apiRoot;
	}

	public URL getServerApiRoot() {
		return mServerApiRoot;
	}

	public Response request(HashMap<String, String> config,
			HashMap<String, String> query) {
		if (config == null) {
			config = new HashMap<String, String>();
			config.put(FIELD_HTTP_TYPE, "GET");
		}

		String method = config.get(FIELD_HTTP_TYPE);
		boolean isGet = (!"POST".equalsIgnoreCase(method));
		// || (query == null || query.isEmpty());

		if (isGet) {
			return sendHttpGetRequest(config, query);
		} else {
			return sendHttpPostRequest(config, query);
			// return sendHttpClientPost(config, query);
		}
	}

	public static void appendQuery(StringBuilder query_builder, String key,
			String value) {
		if (query_builder.length() > 0) {
			query_builder.append("&");
		}
		try {
			String k = URLEncoder.encode(key, "UTF-8");
			String v = URLEncoder.encode(value, "UTF-8");
			query_builder.append(k);
			query_builder.append("=");
			query_builder.append(v);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public Response sendHttpGetRequest(HashMap<String, String> config,
			HashMap<String, String> query) {
		String result = "";
		String api_name = "";
		StringBuilder query_builder = new StringBuilder();
		// default query?

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (entry.getKey().startsWith(FIELD_QUERY_PREFEIX)) {
				String key = entry.getKey().substring(
						FIELD_QUERY_PREFEIX.length() );
				if (query != null) {
					query = new HashMap<String, String>();
				}
				if (!query.containsKey(key)) {
					query.put(key, entry.getValue());
				}
			}
		}

		if (query != null) {
			for (Entry<String, String> entry : query.entrySet()) {
				appendQuery(query_builder, entry.getKey(), entry.getValue());
			}
		}

		HttpURLConnection urlConnection = null;
		BufferedReader in = null;

		try {
			URL url = null;
			if (query_builder.length() > 0) {
				url = new URL(String.format("%s/%s?%s", mServerApiRoot,
						api_name, query_builder));
			} else {
				url = new URL(String.format("%s/%s", mServerApiRoot, api_name));
			}
			Log.d(TAG, "connect (HTTP GET) to ( %s )", url);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.connect();
			Log.d(TAG, "Response Code: %d", urlConnection.getResponseCode());
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			result = sb.toString();
			Log.d(TAG, "Response String: %s", result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new Response(result);
	}

	public String sendHttpClientPost(HashMap<String, String> config,
			HashMap<String, String> query) {
		String result = "";
		String api_name = "";
		String type = "application/x-www-form-urlencoded;charset=utf-8";

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (FIELD_CONTECT_TYPE.equalsIgnoreCase(entry.getKey())) {
				type = entry.getValue();
			}
		}

		HttpClient httpClient = new DefaultHttpClient();
		String url = String.format("%s/%s", mServerApiRoot, api_name);
		Log.d(TAG, "connect to (%s)", url);
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if (query != null) {
			for (Entry<String, String> entry : query.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}
		try {
			HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			printResponse(response);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public Response sendHttpPostRequest(HashMap<String, String> config,
			HashMap<String, String> query) {
		String result = "";
		String api_name = "";
		String type = "application/x-www-form-urlencoded;charset=utf-8";
		StringBuilder query_builder = new StringBuilder();
		StringBuilder body_builder = new StringBuilder();

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (FIELD_CONTECT_TYPE.equalsIgnoreCase(entry.getKey())) {
				type = entry.getValue();
			} else if (entry.getKey().startsWith(FIELD_QUERY_PREFEIX)) {
				String key = entry.getKey().substring(
						FIELD_QUERY_PREFEIX.length() );
				appendQuery(query_builder, key, entry.getValue());
			}
		}

		if (query != null) {
			for (Entry<String, String> entry : query.entrySet()) {
				appendQuery(body_builder, entry.getKey(), entry.getValue());
			}
		}

		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection urlConnection = null;
		OutputStream out = null;
		BufferedReader in = null;

		try {
			URL url = null;
			if (query_builder.length() > 0) {
				url = new URL(String.format("%s/%s?%s", mServerApiRoot,
						api_name, query_builder));
			} else {
				url = new URL(String.format("%s/%s", mServerApiRoot, api_name));
			}
			Log.d(TAG, "connect (HTTP PUT) to ( %s )", url);
			Log.d(TAG, "Request Body Length: %d,Body Content: %s",
					body_builder.length(), body_builder);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true); // use POST
			urlConnection.setDoInput(true);
			urlConnection.setFixedLengthStreamingMode(body_builder.length());
			urlConnection.setRequestProperty("Content-Type", type);
			urlConnection.setRequestProperty("Content-Length",
					String.valueOf(body_builder.length()));

			urlConnection.connect();
			out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(body_builder.toString().getBytes());
			out.flush();

			Log.d(TAG, "Response Code: %d", urlConnection.getResponseCode());
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			result = sb.toString();
			Log.d(TAG, "Response String: %s", result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new Response(result);
	}

	public Response signIn(String externalId, String provider, String password) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");

		config.put(FIELD_API_NAME, "user/signin");
		if (externalId != null) {
			query.put("external_id", externalId);
		}
		if (provider != null) {
			query.put("provider", provider);
		}
		if (password != null) {
			query.put("password", password);
		}
		return request(config, query);
	}

	public Response signOut(String deviceToken) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "user/signout");
		if (deviceToken != null) {
			query.put("device_token", deviceToken);
		}
		return request(config, query);
	}

	public Response getCrossesByUser(long userId) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, String.format("user/%d/crosses", userId));
		return request(config, query);
	}

	public Response addIdentity(String externalId, String provider,
			String password) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "user/addidentity");
		query.put("external_id", externalId);
		query.put("provider", provider);
		query.put("password", password);
		return request(config, query);
	}

	public Response deleteIdentity(String externalId, String password) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "user/deleteidentity");
		query.put("external_id", externalId);
		query.put("password", password);
		return request(config, query);
	}

	public Response setDefaultIdentity(String externalId, String password) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "user/setdeefaultidentity");
		query.put("external_id", externalId);
		query.put("password", password);
		return request(config, query);
	}

	public Response getUserCrossById(long crossId) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, String.format("cross/%d", crossId));
		return request(config, query);
	}

	public Response addCross(Cross cross) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "cross/add");
		query.put("cross", cross.toJSON().toString());
		return request(config, query);
	}

	public Response editCross(Cross cross) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME,
				String.format("cross/%d/edit", cross.getId()));
		query.put("cross", cross.toJSON().toString());
		return request(config, query);
	}

	public Response getProfile() {
		return getProfile(mModel.getUserId());
	}

	public Response getProfile(long userId) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, String.format("user/%s/getprofile", userId));
		return request(config, query);
	}

	public Response getIdentity(String provider, String external_id) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);

		config.put(FIELD_API_NAME, "cross/add");
		// query.put("identities", cross.toJSON().toString());
		return request(config, query);
	}

	void printResponse(HttpResponse response) {
		Log.d(TAG, "HTTP code: %d", response.getStatusLine().getStatusCode());
		Log.v(TAG, "HTTP heads:");
		for (Header head : response.getAllHeaders()) {
			Log.v(TAG, "  %s: %s", head.getName(), head.getValue());
		}
		try {
			Log.v(TAG, "HTTP body:%s",
					EntityUtils.toString(response.getEntity()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
