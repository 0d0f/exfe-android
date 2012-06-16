package com.exfe.android.db;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

public class JSONArrayPersister extends StringType {

	private static final JSONArrayPersister singleTon = new JSONArrayPersister();

	private JSONArrayPersister() {
		super(SqlType.STRING, new Class<?>[] { JSONObject.class });
		// TODO Auto-generated constructor stub
	}

	public static JSONArrayPersister getSingleton() {
		return singleTon;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		JSONArray json = (JSONArray) javaObject;
		if (json == null) {
			return null;
		} else {
			return json.toString();
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		Object result = null;
		try {
			result = new JSONArray((String) sqlArg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
