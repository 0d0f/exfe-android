package com.exfe.android.model.entity;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

public class Widget extends Entity {

	private long mId = NO_ID;
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
		mWidgetId = json.optLong("widget_id", Entity.NO_ID);
	}
	
	
	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("widget_id", mWidgetId);
			json.put("type", mCategory);
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
	
	@Override
	public void saveToDao(DatabaseHelper dbhelper){
		try {
			Dao<Widget, Long> dao = dbhelper.getCachedDao(getClass());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadFromDao(DatabaseHelper dbhelper) {
		// TODO Auto-generated method stub
		
	}
}
