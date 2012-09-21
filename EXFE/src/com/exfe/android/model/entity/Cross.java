package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.Const;
import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.db.EntityPersister;
import com.exfe.android.db.JSONArrayPersister;
import com.exfe.android.db.JSONObjectPersister;
import com.exfe.android.db.WidgetArrayPersister;
import com.exfe.android.debug.Log;
import com.exfe.android.util.Tool;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "crosses")
public class Cross extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId = NO_ID;
	@DatabaseField(persisterClass = JSONArrayPersister.class)
	private JSONArray relative;
	@DatabaseField
	private Date created_at;
	@DatabaseField
	private Date updated_at;
	@DatabaseField(foreign = true)
	private Identity by_identity;
	@DatabaseField
	private String title;
	@DatabaseField
	private String description;
	@DatabaseField
	private int mConversationCount;
	@DatabaseField(persisterClass = EntityPersister.class)
	private CrossTime time;
	@DatabaseField(persisterClass = EntityPersister.class)
	private Place place;
	@DatabaseField(persisterClass = JSONObjectPersister.class)
	private JSONObject attribute;
	@DatabaseField(foreign = true)
	private Exfee exfee;
	@DatabaseField(persisterClass = WidgetArrayPersister.class)
	private Collection<Widget> widgets;
	@DatabaseField(persisterClass = JSONObjectPersister.class)
	private JSONObject updated;
	@DatabaseField
	private Date last_view_at = null;

	public Cross() {
		mType = EntityFactory.TYPE_CROSS;
	}

	public Cross(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);
		mType = EntityFactory.TYPE_CROSS;
		mId = json.optLong("id", NO_ID);

		created_at = Tool.parseDate(json, "created_at");
		updated_at = Tool.parseDate(json, "updated_at", created_at);

		by_identity = (Identity) EntityFactory.create(json
				.optJSONObject("by_identity"));

		relative = json.optJSONArray("relative");
		updated = json.optJSONObject("updated");
		title = json.optString("title");
		description = json.optString("description");
		time = (CrossTime) EntityFactory.create(json.optJSONObject("time"));
		place = (Place) EntityFactory.create(json.optJSONObject("place"));
		mConversationCount = json.optInt("conversation_count");

		JSONObject attrMap = json.optJSONObject("attribute");
		if (attrMap != null) {
			attribute = attrMap;
		}

		exfee = (Exfee) EntityFactory.create(json.optJSONObject("exfee"));

		widgets = new ArrayList<Widget>();
		JSONArray ws = json.optJSONArray("widget");
		if (ws != null) {
			for (int i = 0; i < ws.length(); i++) {
				JSONObject w = ws.optJSONObject(i);
				if (w != null) {
					Widget e = (Widget) EntityFactory.create(w);
					if (e != null) {
						widgets.add(e);
					}
				}
			}
		}
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);

			if (deep) {
				if (created_at == null) {
					json.put("created_at", "");
				} else {
					json.put("created_at",
							Const.UTC_DATE_TIME_FORMAT.format(created_at));
				}
				if (updated_at == null) {
					json.put("updated_at", "");
				} else {
					json.put("updated_at",
							Const.UTC_DATE_TIME_FORMAT.format(updated_at));
				}
			}

			if (by_identity != null) {
				if (deep) {
					json.put("by_identity", by_identity.toJSON());
				} else {
					json.put("by_identity_id", by_identity.getId());
				}
			} else {
				json.put("by_identity", JSONObject.NULL);
			}

			// mRelative
			JSONArray rels = new JSONArray();
			json.put("relative", rels);
			json.put("title", title);
			json.put("description", description);
			json.put("time", time.toJSON());
			json.put("place", place.toJSON());
			json.put("exfee", exfee.toJSON());
			json.put("attribute", attribute);
			json.put("conversation_count", mConversationCount);

			JSONArray array = new JSONArray();
			for (Entity e : widgets) {
				if (e != null) {
					array.put(e.toJSON());
				}
			}
			json.put("widget", array);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Cross, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			if (this.getByIdentitiy() != null) {
				this.getByIdentitiy().saveToDao(dbhelper);
			}
			if (this.getExfee() != null) {
				this.getExfee().saveToDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<Cross, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
			if (this.getByIdentitiy() != null) {
				this.getByIdentitiy().loadFromDao(dbhelper);
			}
			if (this.getExfee() != null) {
				this.getExfee().loadFromDao(dbhelper);
			}
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
	 * @return the update_at
	 */
	public Date getUpdateAt() {
		return updated_at;
	}

	/**
	 * @param update_at
	 *            the update_at to set
	 */
	public void setUpdateAt(Date updateAt) {
		this.updated_at = updateAt;
	}

	/**
	 * @return the byIdentitiy
	 */
	public Identity getByIdentitiy() {
		return this.by_identity;
	}

	/**
	 * @param byIdentitiy
	 *            the byIdentitiy to set
	 */
	public void setByIdentitiy(Identity byIdentitiy) {
		this.by_identity = byIdentitiy;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the time
	 */
	public CrossTime getTime() {
		return this.time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(CrossTime time) {
		this.time = time;
	}

	/**
	 * @return the place
	 */
	public Place getPlace() {
		return this.place;
	}

	/**
	 * @param place
	 *            the place to set
	 */
	public void setPlace(Place place) {
		this.place = place;
	}

	/**
	 * @return the attribute
	 */
	public JSONObject getAttributes() {
		return this.attribute;
	}

	public String getAttribute(String name) {
		if (this.attribute == null) {
			return null;
		}
		return this.attribute.optString(name);
	}

	public void setAttribute(String name, String value) {
		try {
			if (this.attribute == null) {
				this.attribute = new JSONObject();
			}
			this.attribute.putOpt(name, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the conversationCount
	 */
	public int getConversationCount() {
		return this.mConversationCount;
	}

	/**
	 * @param conversationCount
	 *            the conversationCount to set
	 */
	public void setConversationCount(int conversationCount) {
		this.mConversationCount = conversationCount;
	}

	/**
	 * @return the exfeeId
	 */
	public Exfee getExfee() {
		return this.exfee;
	}

	/**
	 * @param exfeeId
	 *            the exfeeId to set
	 */
	public void setExfee(Exfee exfee) {
		this.exfee = exfee;
	}

	public Widget getWidgetByCategory(String category) {
		if (widgets != null) {
			for (Widget e : widgets) {
				if (e.getCategory().equalsIgnoreCase(category)) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * @return the widget
	 */
	public Collection<Widget> getWidget() {
		return this.widgets;
	}

	/**
	 * @param widget
	 *            the widget to set
	 */
	public void setWidget(Collection<Widget> widgets) {
		this.widgets = widgets;
	}

	/**
	 * @return the relative
	 */
	public JSONArray getRelative() {
		return relative;
	}

	/**
	 * @param relative
	 *            the relative to set
	 */
	public void setRelative(JSONArray relative) {
		this.relative = relative;
	}

	/**
	 * @return the updated
	 */
	public JSONObject getUpdated() {
		return updated;
	}

	/**
	 * @param updated
	 *            the updated to set
	 */
	public void setUpdated(JSONObject updated) {
		this.updated = updated;
	}

	/**
	 * @return the update_at
	 */
	public Date getLastViewAt() {
		return last_view_at;
	}

	/**
	 * @param update_at
	 *            the update_at to set
	 */
	public void setLastViewAt(Date lastViewAt) {
		this.last_view_at = lastViewAt;
	}

	public boolean isUpdated(String name) {
		JSONObject update = getUpdated();
		if (update != null) {
			Date updated_at;
			// long identity_id;
			Date lastCall;
			if (update.has(name)) {
				JSONObject json = update.optJSONObject(name);
				updated_at = Tool.parseDate(json, "updated_at");
				if (updated_at != null) {
					// identity_id = json.optLong("identity_id");
					lastCall = this.last_view_at;

					if (lastCall == null
							|| updated_at.getTime() > lastCall.getTime()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public long updatedBy(String name) {
		JSONObject update = getUpdated();
		if (update != null) {
			Date updated_at;
			long identity_id;
			Date lastCall;
			if (update.has(name)) {
				JSONObject json = update.optJSONObject(name);
				updated_at = Tool.parseDate(json, "updated_at");
				if (updated_at != null) {
					identity_id = json.optLong("identity_id");
					lastCall = this.last_view_at;

					if (lastCall == null
							|| updated_at.getTime() > lastCall.getTime()) {
						return identity_id;
					}
				}
			}
		}
		return NO_ID;
	}

	public String diffByUpdate() {
		String diff = "";

		boolean hilightTitle = false;
		boolean hilightPlace = false;
		boolean hilightTime = false;
		boolean hilightExfee = false;
		boolean hilightConversation = false;

		if (isUpdated("exfee")) {
			diff += "e";
			hilightExfee = true;
		}

		if (isUpdated("conversation")) {
			diff += "c";
			hilightConversation = true;
		}

		if (isUpdated("title")) {
			diff += "t";
			hilightTitle = true;
		}

		if (isUpdated("time")) {
			diff += "m";
			hilightTime = true;
		}

		if (isUpdated("place")) {
			diff += "p";
			hilightPlace = true;
		}

		Log.d(TAG, "Cross (%d), %s: %s", getId(), getTitle(), diff);
		return diff;
	}

	// @Override
	// public <T extends Entity> T applyValues(T target) {
	// // TODO Auto-generated method stub
	// Cross t = (Cross)target;
	// t.setRelative(getRelative());
	// t.setCreatedAt(getCreatedAt());
	// t.setUpdateAt(getUpdateAt());
	// //t.set
	//
	// return target;
	// }

}
