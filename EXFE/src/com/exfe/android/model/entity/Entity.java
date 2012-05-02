package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Entity {

	private static final String TAG = Entity.class.getSimpleName();

	protected int mType = EntityFactory.TYPE_UNKNOWN;

	public Entity() {
		mType = EntityFactory.TYPE_UNKNOWN;
	}

	public Entity(JSONObject json) {
		parseJSON(json);
	}
	
	public void parseJSON(JSONObject json){
		//mType = EntityFactory.getType(json.optString("type"));
	}

	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("type", EntityFactory.getTypeString(mType));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public String toString(){
		try {
			return toJSON().toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getFieldName(String jsonName) {
		StringBuilder sb = new StringBuilder("m");
		boolean flag = true;
		for (int i = 0; i < jsonName.length(); i++) {
			char ch = jsonName.charAt(i);
			if (ch == '_') {
				flag = true;
				continue;
			}

			if (flag == true) {
				sb.append(Character.toUpperCase(ch));
			} else {
				sb.append(Character.toLowerCase(ch));
			}
		}
		if (sb.length() > 1) {
			return sb.toString();
		}
		return "";
	}

	public static String getJSONName(String fieldName) {
		if (fieldName.startsWith("m")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < fieldName.length(); i++) {
				char ch = fieldName.charAt(i);
				if (Character.isUpperCase(ch)) {
					if (sb.length() > 0) {
						sb.append("_");
					}
				}
				sb.append(Character.toLowerCase(ch));
			}
			return sb.toString();
		}
		return "";
	}
}
