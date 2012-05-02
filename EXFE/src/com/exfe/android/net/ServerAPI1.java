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
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;

public class ServerAPI1 {

	public static final String TAG = ServerAPI1.class.getSimpleName();
	public static final String lineSeparator = System
			.getProperty("line.separator");

	public static String OVERIDE_PROTOCAL = "http";
	public static String OVERIDE_DOMAIN = "api.0d0f.com";
	public static String OVERIDE_PORT = null;
	public static String OVERIDE_PATHROOT = "/v1";

	private static final String FIELD_API_NAME = "API-Name";
	private static final String FIELD_CONTECT_TYPE = "Content-Type";
	private static final String FIELD_HTTP_TYPE = "HTTP-Type";

	private Model mModel;
	private URL mServerApiRoot;

	private String mUsername;
	private String mAppKey;

	public ServerAPI1(Model model) {
		this(model, Const.getDefaultURL(OVERIDE_PROTOCAL, OVERIDE_DOMAIN,
				OVERIDE_PORT, OVERIDE_PATHROOT));
	}

	public ServerAPI1(Model model, URL apiRoot) {
		mModel = model;
		mUsername = model.getUsername();
		mAppKey = model.getToken();
		mServerApiRoot = apiRoot;
	}

	public URL getServerApiRoot() {
		return mServerApiRoot;
	}

	public void setServerApiRoot(URL serverApiRoot) {
		mServerApiRoot = serverApiRoot;
	}

	public String request(HashMap<String, String> config,
			HashMap<String, String> query) {
		if (config == null) {
			config = new HashMap<String, String>();
			config.put(FIELD_HTTP_TYPE, "GET");
		}

		String method = config.get(FIELD_HTTP_TYPE);
		boolean isGet = (!"POST".equalsIgnoreCase(method));
		// || (query == null || query.isEmpty());

		if (isGet) {
			return getRequest(config, query);
		} else {
			return postRequest(config, query);
		}
	}

	public String getRequest(HashMap<String, String> config,
			HashMap<String, String> query) {
		String result = "";
		String api_name = "";
		StringBuilder query_builder = new StringBuilder();
		// defualt query?

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			}
		}

		if (query != null) {
			for (Entry<String, String> entry : query.entrySet()) {
				if (query_builder.length() > 0) {
					query_builder.append("&");
				}
				try {
					query_builder.append(URLEncoder.encode(entry.getKey(),
							"UTF-8"));
					query_builder.append("=");
					query_builder.append(URLEncoder.encode(entry.getValue(),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			Log.d(TAG, "connect to (%s)", url);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	public String postRequest(HashMap<String, String> config,
			HashMap<String, String> query) {
		String result = "";
		String api_name = "";
		String type = "application/x-www-form-urlencoded;charset=utf-8";
		StringBuilder query_builder = new StringBuilder();

		for (Entry<String, String> entry : config.entrySet()) {
			if (FIELD_API_NAME.equalsIgnoreCase(entry.getKey())) {
				api_name = entry.getValue();
			} else if (FIELD_CONTECT_TYPE.equalsIgnoreCase(entry.getKey())) {
				type = entry.getValue();
			}
		}

		if (query != null) {
			for (Entry<String, String> entry : query.entrySet()) {
				if (query_builder.length() > 0) {
					query_builder.append("&");
				}
				try {
					query_builder.append(URLEncoder.encode(entry.getKey(),
							"UTF-8"));
					query_builder.append("=");
					query_builder.append(URLEncoder.encode(entry.getValue(),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		HttpURLConnection urlConnection = null;
		OutputStream out = null;
		BufferedReader in = null;

		try {
			URL url = new URL(String.format("%s/%s", mServerApiRoot, api_name));
			Log.d(TAG, "connect to (%s)", url);
			Log.d(TAG, "Request Body Length: %d,Body Content: %s",
					query_builder.length(), query_builder);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true); // use POST
			urlConnection.setDoInput(true);
			urlConnection.setFixedLengthStreamingMode(query_builder.length());
			urlConnection.setRequestProperty("Content-Type", type);
			urlConnection.setRequestProperty("Content-Length",
					String.valueOf(query_builder.length()));

			urlConnection.connect();
			out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(query_builder.toString().getBytes());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	public String checkLogin(String email, String password) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		query.put("token", mAppKey);

		config.put(FIELD_API_NAME, "users/login");
		query.put("user", email);
		query.put("password", password);
		return request(config, query);
	}

	public String getProfile() {
		String result = getProfile(mModel.getUserId());
		return result;
	}

	public String getProfile(long userId) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);

		config.put(FIELD_API_NAME, String.format("users/%s/getprofile", userId));
		return request(config, query);
	}

	public String getPosts(int crossid) {
		/*-
		 * NSString *identity_id=[[NSUserDefaults standardUserDefaults]
		 * 	stringForKey:@"device_identity_id"]; 
		 * DBUtil *dbu=[DBUtil sharedManager]; 
		 * NSString *lastUpdateTime=[dbu getLastCommentUpdateTimeWith:crossid];
		 */

		// FIXME device identity id from preference.
		String identity_id = "EWQGEFEAFDSA";
		// FIXME load from db
		String last_update_time = "2012:12:20";
		return getPosts(crossid, identity_id, last_update_time);
	}

	public String getPosts(int crossid, String identityId, String lastUpdateTime) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);
		config.put(FIELD_API_NAME, String.format("x/%d/posts", crossid));

		if (!TextUtils.isEmpty(lastUpdateTime)) {
			query.put("updated_since", lastUpdateTime);
			query.put("ddid", identityId);
		}
		return request(config, query);
	}

	public String getUserEvents() {
		// FIXME need get the user id from somewhere
		int user_id = 0;
		// FIXME load from db
		String last_update_time = "2012:12:20";
		return getUserEvents(user_id, last_update_time);
	}

	public String getUserEvents(int userId, String lastUpdateTime) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);
		config.put(FIELD_API_NAME, String.format("users/%d/x", userId));

		if (!TextUtils.isEmpty(lastUpdateTime)) {
			query.put("updated_since", lastUpdateTime);
		}

		return request(config, query);
	}

	public String getUpdate() {
		/*-
		 * NSString *lastUpdateTime=[[NSUserDefaults standardUserDefaults]
		 * 		stringForKey:@"lastupdatetime"]; 
		 * NSString *identity_id=[[NSUserDefaults standardUserDefaults]
		 * 		stringForKey:@"device_identity_id"]; 
		 * if(lastUpdateTime==nil)
		 * 		lastUpdateTime=@"0000-00-00 00:00:00";
		 */
		// FIXME need get the user id from somewhere
		int user_id = 0;
		// FIXME device identity id from preference.
		String identity_id = "EWQGEFEAFDSA";
		// FIXME load from preference
		String last_update_time = "2012:12:20";
		if (TextUtils.isEmpty(last_update_time)) {
			last_update_time = "0000-00-00 00:00:00";
		}
		return getUpdate(user_id, identity_id, last_update_time);
	}

	public String getUpdate(int userId, String identityId, String lastUpdateTime) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);
		config.put(FIELD_API_NAME, String.format("users/%d/getupdate", userId));

		if (!TextUtils.isEmpty(lastUpdateTime)) {
			query.put("updated_since", lastUpdateTime);
			query.put("ddid", identityId);
		}
		return request(config, query);
	}

	public String getEvent(int eventId) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);
		config.put(FIELD_API_NAME, String.format("crosses/%d.json", eventId));

		return request(config, query);
	}

	public String addComment(int eventid, String comment,
			String externalIdentity) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		try {
			config.put(FIELD_HTTP_TYPE, "POST");
			config.put(FIELD_API_NAME, String.format("x/%d/posts?token=%s",
					mModel.getUserId(), URLEncoder.encode(mAppKey, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		query.put("external_identity", externalIdentity);
		query.put("content", comment);
		query.put("via", "Android");
		return request(config, query);
	}

	public String sendRSVP(int eventId, String rsvp) {
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "GET");
		query.put("token", mAppKey);

		config.put(FIELD_API_NAME, String.format("x/%d/%s", eventId, rsvp));
		return request(config, query);
	}

	// for iOS push only?
	public boolean regDeviceToken(String token) {

		return false;
	}

	public String disconnectDeviceToken(String deviceToken) {
		if (TextUtils.isEmpty(deviceToken)) {
			return "";
		}
		HashMap<String, String> config = new HashMap<String, String>();
		HashMap<String, String> query = new HashMap<String, String>();
		config.put(FIELD_HTTP_TYPE, "POST");
		config.put(FIELD_API_NAME, String.format("users/%d/posts?token=%s",
				mModel.getUserId(), URLEncoder.encode(mAppKey)));

		query.put("device_token", deviceToken);
		return request(config, query);
	}

	// deprecated

	void printRequest(HttpRequestBase request) {
		Log.d(TAG, "HTTP Method: %s", request.getMethod());
		Log.v(TAG, "HTTP URL: %s", request.getURI());
		Log.v(TAG, "HTTP heads:");
		for (Header head : request.getAllHeaders()) {
			Log.v(TAG, "  %s: %s", head.getName(), head.getValue());
		}
		HttpParams params = request.getParams();
		// HttpEntity entity = request.getEntity();
		// Log.v(TAG, "HTTP body", request.);
		try {
			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntityEnclosingRequest er = (HttpEntityEnclosingRequest) request;
				Log.v(TAG, "HTTP body:%s", EntityUtils.toString(er.getEntity()));
				Log.v(TAG, "HTTP contentLength:%s", er.getEntity()
						.getContentLength());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public String checkLogin2(String email, String password) {

		String result = "";
		try {
			String api_name = "users/login";

			HttpClient httpClient = new DefaultHttpClient();
			String url = String.format("%s/%s", mServerApiRoot, api_name);
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("user", email));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			httpPost.setEntity(entity);
			// httpPost.addHeader("Content-Type",
			// "application/x-www-form-urlencoded" );
			// httpPost.addHeader("Content-Length",
			// String.valueOf(entity.getContentLength()));
			printRequest(httpPost);

			HttpResponse response = httpClient.execute(httpPost);
			printResponse(response);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// result = EntityUtils.toString(response.getEntity());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

}
