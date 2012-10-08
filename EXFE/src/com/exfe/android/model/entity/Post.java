package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.Const;
import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.util.Tool;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "posts")
public class Post extends Entity {

	public static final String POSTABLE_ID_FIELD_NAME = "postable_id";
	public static final String POSTABLE_TYPE_FIELD_NAME = "postable_type";

	public static final String POSTABLE_TYPE_EXFEE = "exfee";

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	// private List<Entity> mRelative;
	@DatabaseField
	private Date created_at;
	@DatabaseField(foreign = true)
	private Identity by_identity;
	@DatabaseField
	private String content;
	@DatabaseField(index = true)
	private long postable_id;
	@DatabaseField(index = true)
	private String postable_type;
	@DatabaseField
	private String via;

	public Post() {
		mType = EntityFactory.TYPE_POST;
	}

	public Post(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);
		mType = EntityFactory.TYPE_POST;

		mId = Long.parseLong(json.optString("id", "0"));
		created_at = Tool.parseDate(json, "created_at");
		by_identity = (Identity) EntityFactory.create(json
				.optJSONObject("by_identity"));
		/*
		 * mRelative = new ArrayList<Entity>(); JSONArray relatives =
		 * json.optJSONArray("relative"); if (relatives != null){ for(int i = 0;
		 * i > relatives.length(); i++){ JSONObject r =
		 * relatives.optJSONObject(i); if (r != null){ // create relation Entity
		 * e = EntityFactory.create(r); if (e != null){ mRelative.add(e); } } }
		 * }
		 */

		via = json.optString("via");
		content = json.optString("content");

		postable_id = json.optLong("postable_id");
		postable_type = json.optString("postable_type");

	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {

			if (deep) {
				if (created_at == null){
					json.put("created_at", "");
				}else{
					json.put("created_at", Const.UTC_DATE_TIME_TIMEZONE_FORMAT.format(created_at));
				}
			}

			if (deep) {
				json.put("by_identity", by_identity.toJSON());
			} else {
				json.put("by_identity_id", by_identity.getId());
			}

			// mRelative
			JSONArray rels = new JSONArray();
			json.put("relative", rels);

			json.put("content", content);
			json.put("via", via);

			if (deep) {
				json.put("postable_id", postable_id);
				json.put("postable_type", postable_type);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Post, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			if (this.getByIdentitiy() != null) {
				this.getByIdentitiy().saveToDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<Post, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
			if (this.getByIdentitiy() != null) {
				this.getByIdentitiy().loadFromDao(dbhelper);
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
	 * @return the byIdentitiy
	 */
	public Identity getByIdentitiy() {
		return this.by_identity;
	}

	/**
	 * @param byIdentity
	 *            the byIdentity to set
	 */
	public void setByIdentitiy(Identity byIdentity) {
		this.by_identity = byIdentity;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
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
	 * @return the exfeeId
	 */
	public long getExfeeId() {
		if (POSTABLE_TYPE_EXFEE.equalsIgnoreCase(postable_type)) {
			return this.postable_id;
		}
		return NO_ID;
	}

	/**
	 * @param exfeeId
	 *            the exfeeId to set
	 */
	public void setExfeeId(long exfeeId) {
		if ("exfee".equalsIgnoreCase(postable_type)) {
			this.postable_id = exfeeId;
		}
	}

	public long getPostableId() {
		return this.postable_id;
	}

	public void setPostableId(long postableId) {
		this.postable_id = postableId;
	}

	public String getPostableType() {
		return this.postable_type;
	}

	public void setPostableType(String postableType) {
		this.postable_type = postableType;
	}

	public static final Comparator<Post> sCreateTimeComparator = new Comparator<Post>() {

		@Override
		public int compare(Post lhs, Post rhs) {
			if (lhs != null && rhs != null) {
				if (lhs.getCreatedAt().getTime() == rhs.getCreatedAt().getTime()) {
					return 0;
				} else if (lhs.getCreatedAt().getTime() < rhs.getCreatedAt()
						.getTime()) {
					return -1;
				} else {
					return 1;
				}
			} else {
				if (lhs == rhs) {
					return 0;
				} else if (lhs == null) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	};
}
