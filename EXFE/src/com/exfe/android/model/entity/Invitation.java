package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Invitation extends Entity {

	public static final int NORESPONSE = 0;
	public static final int ACCEPTED = 1;
	public static final int INTERESTED = 2;
	public static final int DECLINED = 3;
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
		} else {//STR_NORESPONSE
			return NORESPONSE;
		}
	}

	public static String getRsvpStatusString(int rsvp) {
		switch (rsvp) {
		case ACCEPTED:
			return STR_ACCEPTED;
			//break;
		case INTERESTED:
			return STR_INTERESTED;
			//break;
		case DECLINED:
			return STR_DECLINED;
			//break;
		case REMOVED:
			return STR_REMOVED;
			//break;
		case NOTIFICATION:
			return STR_NOTIFICATION;
			//break;
		case NORESPONSE:
		default:
			return STR_NORESPONSE;
		}
	}

	private long mId;
	private Identity mIdentity;
	private Identity mByIdentity;
	private int mRsvpStatus;
	private String mVia;
	private String mCreateAt;
	private String mUpdateAt;

	public Invitation() {
	}

	public Invitation(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_INVITATION;

		mId = json.optLong("id", 0);
		mIdentity = new Identity(json.optJSONObject("identity"));
		mByIdentity = new Identity(json.optJSONObject("by_identity"));
		mRsvpStatus = getRsvpStatusValue(json.optString("rsvp_status", ""));
		mVia = json.optString("via", "");
		mCreateAt = json.optString("created_at", "");
		mUpdateAt = json.optString("updated_at", "");
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("id", mId);
			json.put("identity", mIdentity.toJSON());
			json.put("by_identity", mByIdentity.toJSON());
			json.put("rsvp_status", getRsvpStatusString(mRsvpStatus));
			json.put("via", mVia);
			json.put("created_at", mCreateAt);
			json.put("updated_at", mUpdateAt);
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
	 * @return the identity
	 */
	public Identity getIdentity() {
		return this.mIdentity;
	}

	/**
	 * @param identity
	 *            the identity to set
	 */
	public void setIdentity(Identity identity) {
		this.mIdentity = identity;
	}

	/**
	 * @return the byIdentity
	 */
	public Identity getByIdentity() {
		return this.mByIdentity;
	}

	/**
	 * @param byIdentity
	 *            the byIdentity to set
	 */
	public void setByIdentity(Identity byIdentity) {
		this.mByIdentity = byIdentity;
	}

	/**
	 * @return the rsvpStatus
	 */
	public int getRsvpStatus() {
		return this.mRsvpStatus;
	}

	/**
	 * @param rsvpStatus
	 *            the rsvpStatus to set
	 */
	public void setRsvpStatus(int rsvpStatus) {
		this.mRsvpStatus = rsvpStatus;
	}

	/**
	 * @return the via
	 */
	public String getVia() {
		return this.mVia;
	}

	/**
	 * @param via
	 *            the via to set
	 */
	public void setVia(String via) {
		this.mVia = via;
	}

	/**
	 * @return the createAt
	 */
	public String getCreateAt() {
		return this.mCreateAt;
	}

	/**
	 * @param createAt
	 *            the createAt to set
	 */
	public void setCreateAt(String createAt) {
		this.mCreateAt = createAt;
	}

	/**
	 * @return the updateAt
	 */
	public String getUpdateAt() {
		return this.mUpdateAt;
	}

	/**
	 * @param updateAt
	 *            the updateAt to set
	 */
	public void setUpdateAt(String updateAt) {
		this.mUpdateAt = updateAt;
	}

}
