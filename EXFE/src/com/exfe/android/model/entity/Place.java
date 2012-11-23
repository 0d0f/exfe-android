package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.Const;
import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.util.Tool;
import com.j256.ormlite.dao.Dao;

public class Place extends Entity {

	private long mId;
	private String mTitle;
	private String mDescription;
	private String mLng;
	private String mLat;
	private String mProvider;
	private String mExternalId;
	private Date mCreatedAt;
	private Date mUpdateAt;

	public Place() {
		mType = EntityFactory.TYPE_PLACE;
		mId = 0;
		mTitle = "";
		mDescription = "";
		mLng = "";
		mLng = "";
		mProvider = "";
		mExternalId = "";

	}

	public Place(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_PLACE;

		mId = json.optLong("id", 0);
		mTitle = json.optString("title", "");
		mDescription = json.optString("description", "");
		mLng = json.optString("lng", "");
		mLat = json.optString("lat", "");
		mProvider = json.optString("provider", "");
		mExternalId = json.optString("external_id", "");
		mCreatedAt = Tool.parseDate(json, "created_at");
		mUpdateAt = Tool.parseDate(json, "updated_at", mCreatedAt);
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);
			json.put("title", mTitle);
			json.put("description", mDescription);
			json.put("lng", mLng);
			json.put("lat", mLat);
			json.put("provider", mProvider);
			json.put("external_id", mExternalId);
			if (deep) {
				if (mCreatedAt == null) {
					json.put("created_at", "");
				} else {
					json.put("created_at", Const.UTC_DATE_TIME_TIMEZONE_FORMAT
							.format(mCreatedAt));
				}
				if (mUpdateAt == null) {
					json.put("updated_at", "");
				} else {
					json.put("updated_at", Const.UTC_DATE_TIME_TIMEZONE_FORMAT
							.format(mUpdateAt));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return this.mId;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.mId = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.mTitle;
	}

	/**
	 * @param title
	 *            the title to set
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
	 * @param description
	 *            the description to set
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
	 * @param lng
	 *            the lng to set
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
	 * @param lat
	 *            the lat to set
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
	 * @param provider
	 *            the provider to set
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
	 * @param externalId
	 *            the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.mExternalId = externalId;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return this.mCreatedAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.mCreatedAt = createdAt;
	}

	/**
	 * @return the updateAt
	 */
	public Date getUpdateAt() {
		return this.mUpdateAt;
	}

	/**
	 * @param updateAt
	 *            the updateAt to set
	 */
	public void setUpdateAt(Date updateAt) {
		this.mUpdateAt = updateAt;
	}

	@Override
	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Place, Long> dao = dbhelper.getCachedDao(getClass());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadFromDao(DatabaseHelper dbhelper) {
		// TODO Auto-generated method stub

	}
}
