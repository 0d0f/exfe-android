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

@DatabaseTable(tableName = "invitations")
public class Invitation extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	@DatabaseField(foreign = true)
	private Identity identity;
	@DatabaseField(foreign = true)
	private Identity by_identity;
	@DatabaseField
	private int rsvp_status;
	@DatabaseField
	private String via;
	@DatabaseField
	private Date created_at;
	@DatabaseField(version = true)
	private Date updated_at;
	@DatabaseField
	private boolean host;
	@DatabaseField(canBeNull = false, foreign = true)
	private Exfee exfee;

	public Invitation() {
	}

	public Invitation(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_INVITATION;

		mId = json.optLong("id", 0);
		identity = (Identity) EntityFactory.create(json
				.optJSONObject("identity"));
		by_identity = (Identity) EntityFactory.create(json
				.optJSONObject("by_identity"));
		rsvp_status = Rsvp.getRsvpStatusValue(json.optString("rsvp_status", ""));
		via = json.optString("via", "");
		host = json.optBoolean("host", false);
		
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

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);
			json.put("identity", identity.toJSON());
			json.put("by_identity", by_identity.toJSON());
			json.put("rsvp_status", Rsvp.getRsvpStatusString(rsvp_status));
			json.put("via", via);
			json.put("host", host);
			json.put("created_at", Const.STD_DATE_FORMAT.format(created_at));
			if (updated_at == null) {
				json.put("updated_at", Const.STD_DATE_FORMAT.format(created_at));
			} else {
				json.put("updated_at", Const.STD_DATE_FORMAT.format(updated_at));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	@Override
	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Invitation, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			if (this.identity != null) {
				this.identity.saveToDao(dbhelper);
			}
			if (this.by_identity != null) {
				this.by_identity.saveToDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<Invitation, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
			this.identity.loadFromDao(dbhelper);
			this.by_identity.loadFromDao(dbhelper);
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
	 * @return the identity
	 */
	public Identity getIdentity() {
		return this.identity;
	}

	/**
	 * @param identity
	 *            the identity to set
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * @return the byIdentity
	 */
	public Identity getByIdentity() {
		return this.by_identity;
	}

	/**
	 * @param byIdentity
	 *            the byIdentity to set
	 */
	public void setByIdentity(Identity byIdentity) {
		this.by_identity = byIdentity;
	}

	/**
	 * @return the rsvpStatus
	 */
	public int getRsvpStatus() {
		return this.rsvp_status;
	}

	/**
	 * @param rsvpStatus
	 *            the rsvpStatus to set
	 */
	public void setRsvpStatus(int rsvpStatus) {
		this.rsvp_status = rsvpStatus;
	}
	
	/**
	 * @return the host
	 */
	public boolean isHost(){
		return this.host;
	}
	
	/**
	 * @param isHost
	 *            the host to set
	 */
	public void setHost(boolean isHost){
		this.host = isHost;
	}

	/**
	 * @return the via
	 */
	public String getVia() {
		return this.via;
	}

	/**
	 * @param via
	 *            the via to set
	 */
	public void setVia(String via) {
		this.via = via;
	}

	/**
	 * @return the createAt
	 */
	public Date getCreateAt() {
		return this.created_at;
	}

	/**
	 * @param createAt
	 *            the createAt to set
	 */
	public void setCreateAt(Date createAt) {
		this.created_at = createAt;
	}

	/**
	 * @return the updateAt
	 */
	public Date getUpdateAt() {
		return this.updated_at;
	}

	/**
	 * @param updateAt
	 *            the updateAt to set
	 */
	public void setUpdateAt(Date updateAt) {
		this.updated_at = updateAt;
	}

	/**
	 * @return the exfee
	 */
	public Exfee getExfee() {
		return exfee;
	}

	/**
	 * @param exfee
	 *            the exfee to set
	 */
	public void setExfee(Exfee exfee) {
		this.exfee = exfee;
	}
	
	public Rsvp getRsvpObject(){
		Rsvp result = new Rsvp();
		result.setIdentity(getIdentity());
		result.setRsvpStatus(getRsvpStatus());
		return result;
	}
}
