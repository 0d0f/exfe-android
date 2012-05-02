package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Place extends Entity {

	private Long mId;
	private String mTitle;
	private String mDescription;
	private String mLng;
	private String mLat;
	private String mProvider;
	private String mExternalId;
	private String mCreatedAt;
	private String mUpdateAt;
	
	
	public Place(JSONObject json) {
		parseJSON(json);	
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mType = EntityFactory.TYPE_PLACE;
		
		mId = json.optLong("id", 0);
		mTitle = json.optString("title", "");
		mDescription = json.optString("description", "");
		mLng = json.optString("lng", "");
		mLat = json.optString("lat", "");
		mProvider = json.optString("provider", "");
		mExternalId = json.optString("external_id", "");
		mCreatedAt = json.optString("created_at", "");
		mUpdateAt = json.optString("updated_at", "");
	}

	public JSONObject toJSON(){
		JSONObject json = super.toJSON();
		try {
			json.put("id", mId);
			json.put("title", mTitle);
			json.put("description", mDescription);
			json.put("lng", mLng);
			json.put("lat", mLat);
			json.put("provider", mProvider);
			json.put("external_id", mExternalId);
			json.put("created_at", mCreatedAt);
			json.put("updated_at", mUpdateAt);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.mId;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.mId = id;
	}


	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.mTitle;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.mTitle = title;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.mDescription;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}


	/**
	 * @return the lng
	 */
	public String getLng() {
		return this.mLng;
	}


	/**
	 * @param lng the lng to set
	 */
	public void setLng(String lng) {
		this.mLng = lng;
	}


	/**
	 * @return the lat
	 */
	public String getLat() {
		return this.mLat;
	}


	/**
	 * @param lat the lat to set
	 */
	public void setLat(String lat) {
		this.mLat = lat;
	}


	/**
	 * @return the provider
	 */
	public String getProvider() {
		return this.mProvider;
	}


	/**
	 * @param provider the provider to set
	 */
	public void setProvider(String provider) {
		this.mProvider = provider;
	}


	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return this.mExternalId;
	}


	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.mExternalId = externalId;
	}


	/**
	 * @return the createdAt
	 */
	public String getCreatedAt() {
		return this.mCreatedAt;
	}


	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(String createdAt) {
		this.mCreatedAt = createdAt;
	}


	/**
	 * @return the updateAt
	 */
	public String getUpdateAt() {
		return this.mUpdateAt;
	}


	/**
	 * @param updateAt the updateAt to set
	 */
	public void setUpdateAt(String updateAt) {
		this.mUpdateAt = updateAt;
	}
}
