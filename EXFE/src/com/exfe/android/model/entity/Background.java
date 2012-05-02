package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Background extends Widget {

	private String mImage;
	
	public Background() {
		// TODO Auto-generated constructor stub
	}

	public Background(JSONObject json) {
		super(json);
		// TODO Auto-generated constructor stub
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mImage = json.optString("image", "");
	}
	
	
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("image", mImage);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the image
	 */
	public String getImage() {
		return this.mImage;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.mImage = image;
	}

}
