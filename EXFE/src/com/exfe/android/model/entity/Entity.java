package com.exfe.android.model.entity;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.db.DatabaseHelper;

public abstract class Entity {
	public static final long NO_ID = 0;
	
	public final String TAG = getClass().getSimpleName();
	

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
		return toJSON(true);
	}

	public JSONObject toJSON(boolean deep) {
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
	
	public <T extends Entity> T cloneSelf(){
		T entity = null;
		try {
			@SuppressWarnings("unchecked")
			Class<T> cls = (Class<T>) getClass();
			entity = cls.newInstance();
			entity.parseJSON(toJSON());
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return entity;
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
	
	/**
	 * @return the id
	 */
	public abstract long getId();
	
	/**
	 * @param id
	 *            the id to set
	 */
	public abstract void setId(long id);
	
	public abstract void saveToDao(DatabaseHelper dbhelper);
	
	public abstract void loadFromDao(DatabaseHelper dbhelper);
	
	public static final Comparator<Entity> sIdComparator = new Comparator<Entity>() {

		@Override
		public int compare(Entity lhs, Entity rhs) {
			if (lhs.getId() == rhs.getId()) {
				return 0;
			} else if (lhs.getId() < rhs.getId()) {
				return -1;
			} else {
				return 1;
			}

		}
	};
}
