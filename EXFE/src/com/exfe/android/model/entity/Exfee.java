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
import com.exfe.android.debug.Log;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "exfee")
public class Exfee extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	@DatabaseField
	private int mTotal;
	@DatabaseField
	private int mAccepted;
	@ForeignCollectionField(eager = true, columnName = "invitations")
	private Collection<Invitation> invitations;
	@DatabaseField
	private Date updated_at;

	public Exfee() {
		mType = EntityFactory.TYPE_EXFEE;
	}

	public Exfee(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_EXFEE;

		mTotal = json.optInt("total", 0);
		mAccepted = json.optInt("accepted", 0);

		setId(json.optLong("id", 0));
		invitations = new ArrayList<Invitation>();
		JSONArray idArray = json.optJSONArray("invitations");
		for (int i = 0; i < idArray.length(); i++) {
			try {
				JSONObject obj = idArray.getJSONObject(i);
				Invitation inv = (Invitation) EntityFactory.create(obj);
				inv.setExfee(this);
				invitations.add(inv);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			updated_at = Const.UTC_DATE_TIME_FORMAT.parse(json.optString(
					"updated_at", ""));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);
			json.put("total", mTotal);
			json.put("accepted", mAccepted);

			JSONArray array = new JSONArray();
			for (Invitation inv : invitations) {
				array.put(inv.toJSON());
			}
			json.put("invitations", array);

			json.put("updated_at", Const.UTC_DATE_TIME_FORMAT.format(updated_at));

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return mId;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.mId = id;
	}

	/**
	 * @return the total
	 */
	public int getTotal() {
		return this.mTotal;
	}

	/**
	 * @param total
	 *            the total to set
	 */
	public void setTotal(int total) {
		this.mTotal = total;
	}

	/**
	 * @return the accepted
	 */
	public int getAccepted() {
		return this.mAccepted;
	}

	/**
	 * @param accepted
	 *            the accepted to set
	 */
	public void setAccepted(int accepted) {
		this.mAccepted = accepted;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return this.updated_at;
	}

	/**
	 * @param updated_at
	 *            the updated_at to set
	 */
	public void setUpdatedAt(Date updated_at) {
		this.updated_at = updated_at;
	}

	/**
	 * @return the invitations
	 */
	public Collection<Invitation> getInvitations() {
		return this.invitations;
	}

	/**
	 * @param invitations
	 *            the invitations to set
	 */
	public void setInvitations(Collection<Invitation> invitations) {
		this.invitations = invitations;
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<Exfee, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			for (Invitation inv : this.invitations) {
				inv.saveToDao(dbhelper);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadFromDao(DatabaseHelper dbhelper) {
		try {
			Dao<Exfee, Long> dao = dbhelper.getCachedDao(getClass());
			dao.refresh(this);
			for (Invitation inv : this.invitations) {
				inv.loadFromDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
