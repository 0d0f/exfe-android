package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "identities")
public class Identity extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	@DatabaseField
	private String name;
	@DatabaseField
	private String nickname;
	@DatabaseField
	private String bio;
	@DatabaseField
	private String provider;
	@DatabaseField
	private long connected_user_id;
	@DatabaseField
	private String external_id;
	@DatabaseField
	private String external_username;
	@DatabaseField
	private String avatar_filename;
	@DatabaseField
	private String avatar_updated_at;
	@DatabaseField
	private Date created_at;
	@DatabaseField(version = true)
	private Date updated_at;

	public Identity() {
		mType = EntityFactory.TYPE_IDENTITY;
	}

	public Identity(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_IDENTITY;
		if (json != null) {
			mId = json.optLong("id", 0);
			name = json.optString("name", "");
			nickname = json.optString("nickname", "");
			bio = json.optString("bio", "");
			provider = json.optString("provider", "");
			connected_user_id = json.optLong("connected_user_id", 0);
			external_id = json.optString("external_id", "");
			external_username = json.optString("external_username", "");
			avatar_filename = json.optString("avatar_filename", "");
			avatar_updated_at = json.optString("avatar_updated_at", "");

			try {
				created_at = Const.STD_DATE_FORMAT.parse(json.optString(
						"created_at", ""));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!json.isNull("updated_at")) {
				try {
					String update = json.optString("updated_at", "");
					if (!TextUtils.isEmpty(update)) {
						updated_at = Const.STD_DATE_FORMAT.parse(update);
					} else {
						updated_at = null;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (updated_at == null) {
				updated_at = created_at;
			}

		}
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);
			json.put("name", name);
			json.put("nickname", nickname);
			json.put("bio", bio);
			json.put("provider", provider);
			json.put("connected_user_id", connected_user_id);
			json.put("external_id", external_id);
			json.put("external_username", external_username);
			json.put("avatar_filename", avatar_filename);
			json.put("avatar_updated_at", avatar_updated_at);
			if (created_at == null) {
				json.put("created_at", null);
			} else {
				json.put("created_at", Const.STD_DATE_FORMAT.format(created_at));
			}
			if (updated_at == null) {
				json.put("updated_at", null);
			} else {
				json.put("updated_at", Const.STD_DATE_FORMAT.format(updated_at));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Identity, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<Identity, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return this.nickname;
	}

	/**
	 * @param nickname
	 *            the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the bio
	 */
	public String getBio() {
		return this.bio;
	}

	/**
	 * @param bio
	 *            the bio to set
	 */
	public void setBio(String bio) {
		this.bio = bio;
	}

	/**
	 * @return the provider
	 */
	public String getProvider() {
		return this.provider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * @return the connectedUserId
	 */
	public long getConnectedUserId() {
		return this.connected_user_id;
	}

	/**
	 * @param connectedUserId
	 *            the connectedUserId to set
	 */
	public void setConnectedUserId(long connectedUserId) {
		this.connected_user_id = connectedUserId;
	}

	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return this.external_id;
	}

	/**
	 * @param externalId
	 *            the externalId to set
	 */
	public void setExternalId(String externalId) {
		this.external_id = externalId;
	}

	/**
	 * @return the externalUsername
	 */
	public String getExternalUsername() {
		return this.external_username;
	}

	/**
	 * @param externalUsername
	 *            the externalUsername to set
	 */
	public void setExternalUsername(String externalUsername) {
		this.external_username = externalUsername;
	}

	/**
	 * @return the avatarFilename
	 */
	public String getAvatarFilename() {
		return this.avatar_filename;
	}

	/**
	 * @param avatarFilename
	 *            the avatarFilename to set
	 */
	public void setAvatarFilename(String avatarFilename) {
		this.avatar_filename = avatarFilename;
	}

	/**
	 * @return the avatarUpdatedAt
	 */
	public String getAvatarUpdatedAt() {
		return this.avatar_updated_at;
	}

	/**
	 * @param avatarUpdatedAt
	 *            the avatarUpdatedAt to set
	 */
	public void setAvatarUpdatedAt(String avatarUpdatedAt) {
		this.avatar_updated_at = avatarUpdatedAt;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return this.created_at;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.created_at = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return this.updated_at;
	}

	/**
	 * @param updatedAt
	 *            the updatedAt to set
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updated_at = updatedAt;
	}
	
	public boolean isDeviceToken(){
		return (Const.PROVIDER_IOS.equalsIgnoreCase(getProvider())
		|| Const.PROVIDER_ANDROID.equalsIgnoreCase(getProvider()));
	}
}
