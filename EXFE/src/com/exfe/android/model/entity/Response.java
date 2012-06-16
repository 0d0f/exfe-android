package com.exfe.android.model.entity;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class Response {

	private int mCode;
	private String mErrorType;
	private String mErrorDetail;
	private JSONObject mResponse;
	private HashMap<String, String> mConfig;
	private HashMap<String, String> mPayload;

	public Response() {
		init();
	}

	public Response(String response, HashMap<String, String> config, HashMap<String, String> payload) {

		mConfig = config;
		mPayload = payload;
		if (!TextUtils.isEmpty(response)) {
			JSONObject json;
			try {
				json = new JSONObject(response);
				parseJSON(json);
				return;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		mCode = 0;
		mErrorType = "";
		mErrorDetail = "";
		mResponse = null;
	}

	private void parseJSON(JSONObject json) throws JSONException {
		// TODO Auto-generated method stub
		if (json != null) {
			JSONObject meta = json.getJSONObject("meta");

			mCode = meta.getInt("code");
			mErrorType = meta.optString("errorType", "");
			mErrorDetail = meta.optString("errorDetail", "");
			// v1
			if (json.has("error")) {
				mErrorDetail = json.optString("error", "");
			}

			mResponse = json.optJSONObject("response");
		}
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return this.mCode;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(int code) {
		this.mCode = code;
	}

	/**
	 * @return the errorType
	 */
	public String getErrorType() {
		return this.mErrorType;
	}

	/**
	 * @param errorType
	 *            the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.mErrorType = errorType;
	}

	/**
	 * @return the errorDetail
	 */
	public String getErrorDetail() {
		return this.mErrorDetail;
	}

	/**
	 * @param errorDetail
	 *            the errorDetail to set
	 */
	public void setErrorDetail(String errorDetail) {
		this.mErrorDetail = errorDetail;
	}

	/**
	 * @return the response
	 */
	public JSONObject getResponse() {
		return this.mResponse;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(JSONObject response) {
		this.mResponse = response;
	}

	/**
	 * @return the config
	 */
	public HashMap<String, String> getConfig() {
		return mConfig;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(HashMap<String, String> config) {
		this.mConfig = config;
	}

	/**
	 * @return the payload
	 */
	public HashMap<String, String> getPayload() {
		return mPayload;
	}

	/**
	 * @param payload the payload to set
	 */
	public void setPayload(HashMap<String, String> payload) {
		this.mPayload = payload;
	}
}
