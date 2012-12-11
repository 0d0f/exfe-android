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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TimeZone;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.Rsvp;
import com.exfe.android.util.Tool;

public class ServerAPI2 {
	public static final String TAG = ServerAPI2.class.getSimpleName();
	// public static final String lineSeparator =
	// System.getProperty("line.separator");

	public static String OVERIDE_PROTOCAL = Const.override_domain ? "http"
			: null;
	public static String OVERIDE_DOMAIN = Const.override_domain ? "api.0d0f.com"
			: null;
	public static String OVERIDE_PORT = Const.override_domain ? null : null;
	public static String OVERIDE_PATHROOT = Const.override_domain ? null : null;

	private static final String FIELD_API_NAME = "API-Name";
	private static final String FIELD_CONTECT_TYPE = "Content-Type";
	private static final String FIELD_HTTP_TYPE = "HTTP-Type";

	private static final String FIELD_QUERY_PREFEIX = "QUERY_";
	private static final String FIELD_TOKEN = "QUERY_token";

	private static final DateFormat STD_DATETIME_FMT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);
	static {
		STD_DATETIME_FMT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	// private Model mModel;
	private URL mServerApiRoot;

	// private String mUsername;
	private long mUserId;
	private String mAppKey;

	public ServerAPI2(Model model) {
		this(model, Const.getDefaultAPIURL(OVERIDE_PROTOCAL, OVERIDE_DOMAIN,
				OVERIDE_PORT, OVERIDE_PATHROOT));
	}

	public ServerAPI2(Model model, URL apiRoot) {
		// mModel = model;
		// mUsername = model.Me().getUsername();
		mUserId = model.Me().getUserId();
		mAppKey = model.Me().getToken();
		mServerApiRoot = apiRoot;
	}

	public URL getServerApiRoot() {
		return mServerApiRoot;
	}

	protected Response request(HashMap<String, String> config,
			HashMap<String, String> payload) {
		if (config == null) {
			config = new LinkedHashMap<String, String>();
			config.put(FIELD_HTTP_TYPE, "GET");
		}

		String method = config.get(FIELD_HTTP_TYPE);
		boolean isGet = (!"POST".equalsIgnoreCase(method));
		// || (query == null || query.isEmpty());

		if (isGet) {
			return sendHttpGetRequest(config, payload);
		} else {
			return sendHttpPostRequest(config, payload);
			// return sendHttpClientPost(config, query);
		}
	}

	protected static void appendQuery(StringBuilder query_builder, String key,
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

	protected static void appendBody(StringBuilder query_builder, String key,
			String value, boolean needEncoding) {
		if (query_builder.length() > 0) {
			query_builder.append("&");
		}
		String k;
		String v;
		try {
			if (needEncoding) {
				k = URLEncoder.encode(key, "UTF-8");
			} else {
				k = key.replace("&", URLEncoder.encode("&", "UTF-8")).replace(
						"=", URLEncoder.encode("=", "UTF-8"));
			}

			if (needEncoding) {
				v = URLEncoder.encode(value, "UTF-8");
			} else {
				v = value;
			}
			if (k.length() > 0) {
				query_builder.append(k);
				query_builder.append("=");
			}
			query_builder.append(v);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	protected Response sendHttpGetRequest(HashMap<String, String> config,
			HashMap<String, String> payload) {
		String result = "";
		String api_name = "";
		StringBuilder query_builder = new StringBuilder();
		// default query?

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (entry.getKey().startsWith(FIELD_QUERY_PREFEIX)) {
				String key = entry.getKey().substring(
						FIELD_QUERY_PREFEIX.length());
				if (payload == null) {
					payload = new LinkedHashMap<String, String>();
				}
				if (!payload.containsKey(key)) {
					payload.put(key, entry.getValue());
				}
			}
		}

		if (payload != null) {
			for (Entry<String, String> entry : payload.entrySet()) {
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
			if (!Tool.isJson(result)) {
				result = String
						.format("{meta: {code: 600, errorType: \"failed\",errorDetail:\"%s\"},response: { }}",
								result);

			}
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

		return new Response(result, config, payload);
	}

	protected String sendHttpClientPost(HashMap<String, String> config,
			HashMap<String, String> payload) {
		String result = "";
		String api_name = "";
		// String type = "application/x-www-form-urlencoded;charset=utf-8";

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (FIELD_CONTECT_TYPE.equalsIgnoreCase(entry.getKey())) {
				// type = entry.getValue();
			}
		}

		HttpClient httpClient = new DefaultHttpClient();
		String url = String.format("%s/%s", mServerApiRoot, api_name);
		Log.d(TAG, "connect to (%s)", url);
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if (payload != null) {
			for (Entry<String, String> entry : payload.entrySet()) {
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
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	protected Response sendHttpPostRequest(HashMap<String, String> config,
			HashMap<String, String> payload) {
		String result = "";
		String api_name = "";
		String type = "application/x-www-form-urlencoded;charset=utf-8";
		boolean needURLEncoding = true;
		StringBuilder query_builder = new StringBuilder();
		StringBuilder body_builder = new StringBuilder();

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (FIELD_CONTECT_TYPE.equalsIgnoreCase(entry.getKey())) {
				type = entry.getValue();
				needURLEncoding = type
						.contains("application/x-www-form-urlencoded");
			} else if (entry.getKey().startsWith(FIELD_QUERY_PREFEIX)) {
				String key = entry.getKey().substring(
						FIELD_QUERY_PREFEIX.length());
				appendQuery(query_builder, key, entry.getValue());
			}
		}

		if (payload != null) {
			for (Entry<String, String> entry : payload.entrySet()) {
				appendBody(body_builder, entry.getKey(), entry.getValue(),
						needURLEncoding);
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
			Log.d(TAG, "connect (HTTP POST) to ( %s )", url);
			Log.d(TAG, "Request Body Length: %d,Body Content: %s",
					body_builder.length(), body_builder);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true); // use POST
			urlConnection.setDoInput(true);
			byte[] bytes = body_builder.toString().getBytes();
			urlConnection.setFixedLengthStreamingMode(bytes.length);
			urlConnection.setRequestProperty("Content-Type", type);
			urlConnection.setRequestProperty("Content-Length",
					String.valueOf(bytes.length));

			urlConnection.connect();
			out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(bytes);
			out.flush();

			// Log.d(TAG, "Response Code: %d", urlConnection.getResponseCode());
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			result = sb.toString();
			if (!Tool.isJson(result)) {
				result = String
						.format("{meta: {code: 600, errorType: \"failed\",errorDetail:\"%s\"},response: { }}",
								result);

			}
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

		return new Response(result, config, payload);
	}

	public Response signIn(String externalId, String provider, String password) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_API_NAME, "users/signin");
		if (externalId != null) {
			payload.put("external_username", externalId);
		}
		if (provider != null) {
			payload.put("provider", provider);
		}
		if (password != null) {
			payload.put("password", password);
		}
		return request(config, payload);
	}

	public Response setUpNewUser(String externalId, String provider,
			String password, String username) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_API_NAME, "users/signin");
		if (externalId != null) {
			payload.put("external_username", externalId);
		}
		if (provider != null) {
			payload.put("provider", provider);
		}
		if (password != null) {
			payload.put("password", password);
		}
		if (password != null) {
			payload.put("name", username);
		}
		return request(config, payload);
	}

	public Response signOut(String deviceToken, String udid) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/signout");
		if (deviceToken != null) {
			payload.put("device_token", deviceToken);
		}
		if (udid != null) {
			payload.put("udid", udid);// mModel.getDeviceId()
		}
		payload.put("os_name", Provider.STR_ANDROID);

		return request(config, payload);
	}

	public Response getMyCrosses() {
		return getCrossesByUser(mUserId, null);
	}

	public Response getNewCrossesAfter(Date d) {
		return getCrossesByUser(mUserId, d);
	}

	public Response getCrossesByUser(long userId, Date d) {

		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("users/%d/crosses", userId));
		if (d != null) {
			payload.put("updated_at", STD_DATETIME_FMT.format(d));
		}
		if (userId == 0) {
			String result = String
					.format("{meta: {code: 601, errorType: \"parameter error\",errorDetail:\"%s\"},response: { }}",
							"userId is 0");
			return new Response(result, config, payload);
		} else {
			return request(config, payload);
		}
	}

	public Response addIdentity(String externalId, String provider,
			String password) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/addidentity");
		payload.put("external_username", externalId);
		payload.put("provider", provider);
		payload.put("password", password);
		return request(config, payload);
	}

	public Response deleteIdentity(String externalId, String password) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/deleteidentity");
		payload.put("external_username", externalId);
		payload.put("password", password);
		return request(config, payload);
	}

	public Response setDefaultIdentity(String externalId, String password) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/setdeefaultidentity");
		payload.put("external_username", externalId);
		payload.put("password", password);
		return request(config, payload);
	}

	public Response getCrossById(long crossId, Date lastUpdate) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("crosses/%d", crossId));
		if (lastUpdate != null) {
			payload.put("updated_at", STD_DATETIME_FMT.format(lastUpdate));
		}
		return request(config, payload);
	}

	public Response addCross(Cross cross) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "crosses/gather");
		config.put(FIELD_CONTECT_TYPE, "application/json; charset=utf-8");
		payload.put("", cross.toJSON().toString());
		return request(config, payload);
	}

	public Response editCross(Cross cross) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME,
				String.format("crosses/%d/edit", cross.getId()));
		config.put(FIELD_CONTECT_TYPE, "application/json; charset=utf-8");
		payload.put("", cross.toJSON().toString());
		return request(config, payload);
	}

	public Response getMyProfile() {
		return getProfile(mUserId);
	}

	public Response getProfile(long userId) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("users/%d", userId));
		return request(config, payload);
	}

	public Response getIdentity(String provider, String external_id,
			String external_username) {

		List<HashMap<String, String>> idents = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("provider", provider);
		if (external_id != null) {
			map.put("external_id", external_id);
		}
		if (external_username != null) {
			map.put("external_username", external_username);
		}
		idents.add(map);
		return getIdentities(idents);
	}

	public Response getIdentities(List<HashMap<String, String>> list) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "identities/get");

		try {
			JSONArray array = new JSONArray();
			JSONObject json = new JSONObject();
			for (HashMap<String, String> map : list) {
				String p = map.get("provider");
				String i = map.get("external_id");
				String n = map.get("external_username");

				if (!TextUtils.isEmpty(p)) {
					json.put("provider", p);

					if (i != null) {
						json.put("external_id", i);
					} else {
						json.remove("external_id");
					}
					if (n != null) {
						json.put("external_username", n);
					} else {
						json.remove("external_username");
					}
					array.put(json);
				}
			}
			payload.put("identities", array.toString(2));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request(config, payload);
	}

	public Response editExfee(Identity myIdentity, Exfee exfee) {
		return editExfee(myIdentity.getId(), exfee);
	}

	public Response editExfee(long identityId, Exfee exfee) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME,
				String.format("exfee/%d/edit", exfee.getId()));
		payload.put("by_identity_id", String.valueOf(identityId));
		payload.put("exfee", exfee.toJSON().toString());
		return request(config, payload);
	}

	public Response updateRSVP(long exfeeId, List<Rsvp> rsvps) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("exfee/%d/rsvp", exfeeId));
		JSONArray array = new JSONArray();
		for (Rsvp rsvp : rsvps) {
			if (rsvp != null) {
				array.put(rsvp.toJSON(false));
			}
		}
		payload.put("rsvp", array.toString());
		return request(config, payload);
	}

	public Response getConversation(Exfee exfee) {
		return getConversation(exfee.getId());
	}

	public Response getConversation(long exfee_id) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("conversation/%d", exfee_id));
		return request(config, payload);
	}

	public Response addConversation(Post post) {
		return addConversation(post.getExfeeId(), post);
	}

	public Response addConversation(long exfee_id, Post post) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME,
				String.format("conversation/%d/add", exfee_id));
		config.put(FIELD_CONTECT_TYPE, "application/json; charset=utf-8");
		payload.put("", post.toJSON(false).toString());
		return request(config, payload);
	}

	public Response delConversation(Post post) {
		return delConversation(post.getExfeeId(), post.getId());
	}

	public Response delConversation(long exfee_id, long post_id) {
		HashMap<String, String> config = new LinkedHashMap<String, String>();
		HashMap<String, String> payload = new LinkedHashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME,
				String.format("conversation/%d/del", exfee_id));
		payload.put("post_id", String.valueOf(post_id));
		return request(config, payload);
	}

	public Response regDevice(String udid, String pushToken, String name) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> payload = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, String.format("users/%d/regdevice", mUserId));
		payload.put("udid", udid);
		payload.put("push_token", pushToken);
		payload.put("os_name", Provider.STR_ANDROID);
		if (name != null) {
			payload.put("name", name.replace(' ', '_'));
		}
		payload.put("brand", Build.BRAND.replace(' ', '_'));
		payload.put("model", Build.MODEL.replace(' ', '_'));
		payload.put("os_version", Build.VERSION.RELEASE);
		return request(config, payload);
	}

	public Response updateUser(String name) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> payload = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/update");
		payload.put("name", name);
		return request(config, payload);
	}

	public Response getRegistrationFlag(String external_username,
			String provider) {

		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> payload = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "users/getRegistrationFlag");
		payload.put("external_username", external_username);
		payload.put("provider", provider);
		return request(config, payload);
	}

	public Response searchIdentities(String keyword) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> payload = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "identities/complete");
		payload.put("key", keyword.trim());
		return request(config, payload);
	}

	public Response formatTime(String rawTime) {
		return formatTime(rawTime,
				Tool.gmtWalkaround(Tool.localTimeZoneString()));
	}

	public Response formatTime(String rawTime, String timezone) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> payload = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		// config.put(FIELD_TOKEN, mAppKey);
		config.put(FIELD_API_NAME, "time/recognize");
		payload.put("time_string", rawTime.trim());
		payload.put("timezone", timezone.trim());
		return request(config, payload);
	}

	protected void printResponse(HttpResponse response) {
		Log.d(TAG, "HTTP code: %d", response.getStatusLine().getStatusCode());
		Log.v(TAG, "HTTP heads:");
		for (Header head : response.getAllHeaders()) {
			Log.v(TAG, "  %s: %s", head.getName(), head.getValue());
		}
		try {
			Log.v(TAG, "HTTP body:%s",
					EntityUtils.toString(response.getEntity()));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
