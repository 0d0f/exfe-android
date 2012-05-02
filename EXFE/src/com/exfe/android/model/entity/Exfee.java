package com.exfe.android.model.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Exfee extends Entity {
	
	private long mId;
	private List<Invitation> mInvitations;
	
	public Exfee(){
	}
	
	public Exfee(JSONObject json){
		parseJSON(json);	
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mType = EntityFactory.TYPE_EXFEE;
		
		setId(json.optLong("id", 0));
		mInvitations = new ArrayList<Invitation>();
		JSONArray idArray = json.optJSONArray("invitations");
		for(int i = 0; i < idArray.length(); i++){
			try {
				JSONObject obj = idArray.getJSONObject(i);
				Invitation ident = new Invitation(obj);
				mInvitations.add(ident);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("id", mId);
			
			JSONArray array = new JSONArray();
			for(Invitation inv: mInvitations){
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
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.mId = id;
	}

	/**
	 * @return the invitations
	 */
	public List<Invitation> getInvitations() {
		return this.mInvitations;
	}

	/**
	 * @param invitations the invitations to set
	 */
	public void setInvitations(List<Invitation> invitations) {
		this.mInvitations = invitations;
	}
	
	
}
