package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Identity extends Entity {

	private long mId;
	private String mName;
	private String mNickname;
	private String mBio;
	private String mProvider;
	private long mConnectedUserId;
	private String mExternalId;
	private String mExternalUsername;
	private String mAvatarFilename;
	private String mAvatarUpdatedAt;
	private String mCreatedAt;
	private String mUpdatedAt;

	public Identity(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_IDENTITY;
		if (json != null) {
			mId = json.optLong("id", 0);
			mName = json.optString("name", "");
			mNickname = json.optString("nickname", "");
			mBio = json.optString("bio", "");
			mProvider = json.optString("provider", "");
			mConnectedUserId = json.optLong("connected_user_id", 0);
			mExternalId = json.optString("external_id", "");
			mExternalUsername = json.optString("external_username", "");
			mAvatarFilename = json.optString("avatar_filename", "");
			mAvatarUpdatedAt = json.optString("avatar_updated_at", "");
			mCreatedAt = json.optString("created_at", "");
			if (!json.isNull("updated_at")) {
				mUpdatedAt = json.optString("updated_at", null);
			} else {
				mUpdatedAt = null;
			}
		}
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("id", mId);
			json.put("name", mName);
			json.put("nickname", mNickname);
			json.put("bio", mBio);
			json.put("provider", mProvider);
			json.put("connected_user_id", mConnectedUserId);
			json.put("external_id", mExternalId);
			json.put("external_username", mExternalUsername);
			json.put("avatar_filename", mAvatarFilename);
			json.put("avatar_updated_at", mAvatarUpdatedAt);
			json.put("created_at", mCreatedAt);
			if (mUpdatedAt == null) {
				json.put("updated_at", "");
			} else {
				json.put("updated_at", mUpdatedAt);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	public Identity() {
		this(null);
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
	 * @return the name
	 */
	public String getName() {
		return this.mName;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return this.mNickname;
	}

	/**
	 * @param nickname
	 *            the nickname to set
	 */
	public void setNickname(String nickname) {
		this.mNickname = nickname;
	}

	/**
	 * @return the bio
	 */
	public String getBio() {
		return this.mBio;
	}

	/**
	 * @param bio
	 *            the bio to set
	 */
	public void setBio(String bio) {
		this.mBio = bio;
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
	 * @return the connectedUserId
	 */
	public long getConnectedUserId() {
		return this.mConnectedUserId;
	}

	/**
	 * @param connectedUserId
	 *            the connectedUserId to set
	 */
	public void setConnectedUserId(long connectedUserId) {
		this.mConnectedUserId = connectedUserId;
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
	 * @return the externalUsername
	 */
	public String getExternalUsername() {
		return this.mExternalUsername;
	}

	/**
	 * @param externalUsername
	 *            the externalUsername to set
	 */
	public void setExternalUsername(String externalUsername) {
		this.mExternalUsername = externalUsername;
	}

	/**
	 * @return the avatarFilename
	 */
	public String getAvatarFilename() {
		return this.mAvatarFilename;
	}

	/**
	 * @param avatarFilename
	 *            the avatarFilename to set
	 */
	public void setAvatarFilename(String avatarFilename) {
		this.mAvatarFilename = avatarFilename;
	}

	/**
	 * @return the avatarUpdatedAt
	 */
	public String getAvatarUpdatedAt() {
		return this.mAvatarUpdatedAt;
	}

	/**
	 * @param avatarUpdatedAt
	 *            the avatarUpdatedAt to set
	 */
	public void setAvatarUpdatedAt(String avatarUpdatedAt) {
		this.mAvatarUpdatedAt = avatarUpdatedAt;
	}

	/**
	 * @return the createdAt
	 */
	public String getCreatedAt() {
		return this.mCreatedAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(String createdAt) {
		this.mCreatedAt = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public String getUpdatedAt() {
		return this.mUpdatedAt;
	}

	/**
	 * @param updatedAt
	 *            the updatedAt to set
	 */
	public void setUpdatedAt(String updatedAt) {
		this.mUpdatedAt = updatedAt;
	}

}
