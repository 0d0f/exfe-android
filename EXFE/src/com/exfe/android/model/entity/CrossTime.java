package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class CrossTime extends Entity {

	public static final int MARK_ORIGINAL = 0;
	public static final int MARK_FORMAT = 1;
	
	private EFTime mBeginAt;
	private String mOrigin;
	private int mOriginMarkType;
	
	public CrossTime(EFTime beginAt, String origin, int markType){
		mType = EntityFactory.TYPE_CROSSTIME;
		mBeginAt = beginAt;
		mOrigin = origin;
		mOriginMarkType = markType ;
	}
	
	public CrossTime(JSONObject json){
		parseJSON(json);	
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mType = EntityFactory.TYPE_CROSSTIME;
		
		mBeginAt = new EFTime(json.optJSONObject("begin_at"));
		mOrigin = json.optString("origin", "");
		mOriginMarkType = json.optInt("outputformat", 0);
	}
	
	public JSONObject toJSON(){
		JSONObject json = super.toJSON();
		try {
			json.put("begin_at", mBeginAt.toJSON());
			json.put("outputformat", mOriginMarkType);
			json.put("origin", mOrigin);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}

	/**
	 * @return the beginAt
	 */
	public EFTime getBeginAt() {
		return this.mBeginAt;
	}

	/**
	 * @param beginAt the beginAt to set
	 */
	public void setBeginAt(EFTime beginAt) {
		this.mBeginAt = beginAt;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return this.mOrigin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.mOrigin = origin;
	}

	/**
	 * @return the originMark
	 */
	public int getOriginMarkType() {
		return this.mOriginMarkType;
	}

	/**
	 * @param originMark the originMark to set
	 */
	public void setOriginMarkType(int originMarkType) {
		this.mOriginMarkType = originMarkType;
	}

	public CharSequence display() {
		// TODO Auto-generated method stub
		return String.format("%s %s", mBeginAt.getTime(), mBeginAt.getDate());
	}
	
	
	public String getLongLocalTimeSring(String localTimezone){
		if (mOriginMarkType == MARK_ORIGINAL){
			return mOrigin;
		}else{
			
		}
		
		return null;
	}
}
