package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.Const;
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
	@DatabaseField
	private Date created_at;
	@DatabaseField
	private Date updated_at;

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
		name = json.optString("name", "");
		bio = json.optString("bio", "");
		avatar_filename = json.optString("avatar_filename", "");
		avatar_updated_at = json.optString("avatar_updated_at", "");
		timezone = json.optString("timezone", "");
		cross_quantity = json.optInt("cross_quantity", 0);
		identities = new ArrayList<Identity>();
		created_at = Tool.parseDate(json, "created_at");
		updated_at = Tool.parseDate(json, "updated_at", created_at);
		if (updated_at == null) {
			updated_at = created_at;
		}
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
		try {
			json.put("id", mId);
			json.put("name", name);
			json.put("bio", bio);
			json.put("avatar_filename", avatar_filename);
			json.put("avatar_updated_at", avatar_updated_at);
			json.put("timezone", timezone);
			json.put("cross_quantity", cross_quantity);
			if (created_at == null) {
				json.put("created_at", "");
			} else {
				json.put("created_at",
						Const.UTC_DATE_TIME_TIMEZONE_FORMAT.format(created_at));
			}
			if (updated_at == null) {
				json.put("updated_at", "");
			} else {
				json.put("updated_at",
						Const.UTC_DATE_TIME_TIMEZONE_FORMAT.format(updated_at));
			}

			JSONArray array = new JSONArray();
			for (Identity ident : identities) {
				if (ident != null) {
					array.put(ident.toJSON(deep));
				}
			}
			json.put("identities", array);

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
	
	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<User, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
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
