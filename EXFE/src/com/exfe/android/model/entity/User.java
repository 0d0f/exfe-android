package com.exfe.android.model.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class User extends Entity {

	private long mId;
	private String mName;
	private String mBio;
	private Identity mDefaultIdentity;
	private String mAvatarFilename;
	private String mAvatarUpdatedAt;
	private String mTimeZone;
	private List<Identity> mIdentities;

	public User() {
		this(null);
	}

	public User(JSONObject json) {
		parseJSON(json);
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mType = EntityFactory.getType(json.optString("type", "user"));

		mId = json.optLong("id", 0);
		mName = json.optString("name", "");
		mBio = json.optString("bio", "");
		mDefaultIdentity = new Identity(json.optJSONObject("default_identity"));
		mAvatarFilename = json.optString("avatar_filename", "");
		mAvatarUpdatedAt = json.optString("avatar_updated_at", "");
		mTimeZone = json.optString("timezone", "");

		mIdentities = new ArrayList<Identity>();
		JSONArray idArray = json.optJSONArray("identities");
		if (idArray != null) {
			for (int i = 0; i < idArray.length(); i++) {
				try {
					JSONObject obj = idArray.getJSONObject(i);
					Identity ident = new Identity(obj);
					mIdentities.add(ident);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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
	 * @return the defaultIdentity
	 */
	public Identity getDefaultIdentity() {
		return this.mDefaultIdentity;
	}

	/**
	 * @param defaultIdentity
	 *            the defaultIdentity to set
	 */
	public void setDefaultIdentity(Identity defaultIdentity) {
		this.mDefaultIdentity = defaultIdentity;
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
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return this.mTimeZone;
	}

	/**
	 * @param timeZone
	 *            the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.mTimeZone = timeZone;
	}

	/**
	 * @return the identities
	 */
	public List<Identity> getIdentities() {
		return this.mIdentities;
	}

	/**
	 * @param identities
	 *            the identities to set
	 */
	public void setIdentities(List<Identity> identities) {
		this.mIdentities = identities;
	}

}
