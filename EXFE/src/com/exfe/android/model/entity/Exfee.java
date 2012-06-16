package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="exfee")
public class Exfee extends Entity {

	@DatabaseField(id = true, columnName = "_ID")
	private long mId;
	@ForeignCollectionField(eager = true, columnName = "invitations")
	private Collection<Invitation> invitations;

	public Exfee() {
		mType = EntityFactory.TYPE_EXFEE;
	}

	public Exfee(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_EXFEE;

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
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("id", mId);

			JSONArray array = new JSONArray();
			for (Invitation inv : invitations) {
				array.put(inv.toJSON());
			}
			json.put("invitations", array);

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

	public void saveToDao(DatabaseHelper dbhelper){
		try {
			Dao<Exfee, Long> dao = dbhelper.getCachedDao(getClass());
			dao.createOrUpdate(this);
			for(Invitation inv: this.invitations){
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
			for(Invitation inv: this.invitations){
				inv.loadFromDao(dbhelper);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
