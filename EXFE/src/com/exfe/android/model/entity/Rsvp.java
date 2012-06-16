package com.exfe.android.model.entity;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.DatabaseTable;

public class Rsvp extends Entity {

	public static final int NORESPONSE = 0;
	public static final int ACCEPTED = 1;
	public static final int INTERESTED = 3;
	public static final int DECLINED = 2;
	public static final int REMOVED = 4;
	public static final int NOTIFICATION = 5;

	public static final String STR_NORESPONSE = "NORESPONSE";
	public static final String STR_ACCEPTED = "ACCEPTED";
	public static final String STR_INTERESTED = "INTERESTED";
	public static final String STR_DECLINED = "DECLINED";
	public static final String STR_REMOVED = "REMOVED";
	public static final String STR_NOTIFICATION = "NOTIFICATION";

	public static int getRsvpStatusValue(String rsvp) {
		if (STR_ACCEPTED.equalsIgnoreCase(rsvp)) {
			return ACCEPTED;
		} else if (STR_INTERESTED.equalsIgnoreCase(rsvp)) {
			return INTERESTED;
		} else if (STR_DECLINED.equalsIgnoreCase(rsvp)) {
			return DECLINED;
		} else if (STR_REMOVED.equalsIgnoreCase(rsvp)) {
			return REMOVED;
		} else if (STR_NOTIFICATION.equalsIgnoreCase(rsvp)) {
			return NOTIFICATION;
		} else {// STR_NORESPONSE
			return NORESPONSE;
		}
	}

	public static String getRsvpStatusString(int rsvp) {
		switch (rsvp) {
		case ACCEPTED:
			return STR_ACCEPTED;
			// break;
		case INTERESTED:
			return STR_INTERESTED;
			// break;
		case DECLINED:
			return STR_DECLINED;
			// break;
		case REMOVED:
			return STR_REMOVED;
			// break;
		case NOTIFICATION:
			return STR_NOTIFICATION;
			// break;
		case NORESPONSE:
		default:
			return STR_NORESPONSE;
		}
	}
	
	
	private Identity identity;
	private int rsvp_status;
	private Identity by_identity;

	public Rsvp(){
		mType = EntityFactory.TYPE_RSVP;
	}
	
	public Rsvp(JSONObject json) {
		// TODO Auto-generated constructor stub
		parseJSON(json);
	}
	
	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_RSVP;
		if (json != null) {
			identity = (Identity) EntityFactory.create(json
					.optJSONObject("identity"));
			if (identity == null){
				long id = json.optLong("identity_id");
				if (id != NO_ID){
					identity = new Identity();
					identity.setId(id);
				}
			}
			
			by_identity = (Identity) EntityFactory.create(json
					.optJSONObject("by_identity"));
			if (by_identity == null){
				long id = json.optLong("by_identity_id");
				if (id != NO_ID){
					by_identity = new Identity();
					by_identity.setId(id);
				}
			}
			
			rsvp_status = Rsvp.getRsvpStatusValue(json.optString("rsvp_status", ""));
		}
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			if (deep){
				json.put("identity", identity.toJSON());
			}else{
				json.put("identity_id", identity.getId());
			}
			if (deep){
				json.put("by_identity", by_identity.toJSON());
			}else{
				json.put("by_identity_id", by_identity.getId());
			}
			json.put("rsvp_status", Rsvp.getRsvpStatusString(rsvp_status));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	@Override
	public void saveToDao(DatabaseHelper dbhelper) {
		/*
		try {
			Dao<Rsvp, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public void loadFromDao(DatabaseHelper dbhelper) {
		this.identity.loadFromDao(dbhelper);
		this.by_identity.loadFromDao(dbhelper);
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return NO_ID;
	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
	}

	/**
	 * @return the identity
	 */
	public Identity getIdentity() {
		return this.identity;
	}

	/**
	 * @param identity the identity to set
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * @return the rsvpStatus
	 */
	public int getRsvpStatus() {
		return this.rsvp_status;
	}

	/**
	 * @param rsvpStatus the rsvpStatus to set
	 */
	public void setRsvpStatus(int rsvpStatus) {
		this.rsvp_status = rsvpStatus;
	}

	/**
	 * @return the byIdentity
	 */
	public Identity getByIdentity() {
		return this.by_identity;
	}

	/**
	 * @param byIdentity the byIdentity to set
	 */
	public void setByIdentity(Identity byIdentity) {
		this.by_identity = byIdentity;
	}

	
}
