package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Widget extends Entity {

	private String mCategory;
	private long mWidgetId;
	
	public Widget() {
		// TODO Auto-generated constructor stub
	}

	public Widget(JSONObject json) {
		super(json);
		// TODO Auto-generated constructor stub
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
		
		mType = EntityFactory.TYPE_WIDGET;
		
		mCategory = json.optString("type");
		mWidgetId = json.optLong("widget_id", 0);
	}
	
	
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("widget_id", mWidgetId);
			json.put("type", mCategory);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the widgetId
	 */
	public long getWidgetId() {
		return this.mWidgetId;
	}

	/**
	 * @param widgetId the widgetId to set
	 */
	public void setWidgetId(long widgetId) {
		this.mWidgetId = widgetId;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return mCategory;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.mCategory = category;
	}

}
