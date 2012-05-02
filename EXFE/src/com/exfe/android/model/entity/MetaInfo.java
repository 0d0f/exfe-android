package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class MetaInfo extends Entity {

	private long mId;
	// private List<Entity> mRelative;
	private String mCreatedAt;
	private Identity mByIdentitiy;

	public MetaInfo() {
	}

	public MetaInfo(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mId = Long.parseLong(json.optString("id", "0"));
		mCreatedAt = json.optString("created_at", "");
		mByIdentitiy = new Identity(json.optJSONObject("by_identity"));

		/*
		 * mRelative = new ArrayList<Entity>(); JSONArray relatives =
		 * json.optJSONArray("relative"); if (relatives != null){ for(int i = 0;
		 * i > relatives.length(); i++){ JSONObject r =
		 * relatives.optJSONObject(i); if (r != null){ // create relation Entity
		 * e = EntityFactory.create(r); if (e != null){ mRelative.add(e); } } }
		 * }
		 */
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("id", mId);

			json.put("created_at", mCreatedAt);
			json.put("by_identity", mByIdentitiy.toJSON());

			// mRelative
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
	 * @return the byIdentitiy
	 */
	public Identity getByIdentitiy() {
		return this.mByIdentitiy;
	}

	/**
	 * @param byIdentitiy
	 *            the byIdentitiy to set
	 */
	public void setByIdentitiy(Identity byIdentitiy) {
		this.mByIdentitiy = byIdentitiy;
	}

}
