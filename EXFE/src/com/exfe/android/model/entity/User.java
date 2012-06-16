package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.db.IdentityArrayPersister;
import com.exfe.android.util.Tool;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	@DatabaseField
	private String name;
	@DatabaseField
	private String bio;
	@DatabaseField(foreign = true)
	private Identity default_identity;
	@DatabaseField
	private String avatar_filename;
	@DatabaseField
	private String avatar_updated_at;
	@DatabaseField
	private String timezone;
	@DatabaseField(persisterClass = IdentityArrayPersister.class)
	private List<Identity> identities;
	@DatabaseField
	private int cross_quantity;

	public User() {
		mType = EntityFactory.TYPE_USER;
	}

	public User(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_USER;

		mId = json.optLong("id", NO_ID);
		name = Tool.parseString(json, "name");
		bio = Tool.parseString(json, "bio");
		default_identity = new Identity(json.optJSONObject("default_identity"));
		avatar_filename = Tool.parseString(json, "avatar_filename");
		avatar_updated_at = Tool.parseString(json, "avatar_updated_at");
		timezone = Tool.parseString(json, "timezone");
		cross_quantity = json.optInt("cross_quantity", 0);

		identities = new ArrayList<Identity>();
		JSONArray idArray = json.optJSONArray("identities");
		if (idArray != null) {
			for (int i = 0; i < idArray.length(); i++) {
				try {
					JSONObject obj = idArray.getJSONObject(i);
					Identity ident = (Identity) EntityFactory.create(obj);
					identities.add(ident);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
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
	 * @return the defaultIdentity
	 */
	public Identity getDefaultIdentity() {
		return this.default_identity;
	}

	/**
	 * @param defaultIdentity
	 *            the defaultIdentity to set
	 */
	public void setDefaultIdentity(Identity defaultIdentity) {
		this.default_identity = defaultIdentity;
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
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return this.timezone;
	}

	/**
	 * @param timeZone
	 *            the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timezone = timeZone;
	}

	/**
	 * @return the identities
	 */
	public List<Identity> getIdentities() {
		return this.identities;
	}

	/**
	 * @param identities
	 *            the identities to set
	 */
	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}

	/**
	 * @return the crossQuantity
	 */
	public int getCrossQuantity() {
		return cross_quantity;
	}

	/**
	 * @param crossQuantity
	 *            the crossQuantity to set
	 */
	public void setCrossQuantity(int crossQuantity) {
		this.cross_quantity = crossQuantity;
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<User, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			if (this.default_identity != null) {
				this.default_identity.saveToDao(dbhelper);
			}
			for (Identity id : this.identities) {
				id.saveToDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<User, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
