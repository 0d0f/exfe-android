package com.exfe.android.db;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

public class JSONObjectPersister extends StringType {

	private static final JSONObjectPersister singleTon = new JSONObjectPersister();

	private JSONObjectPersister() {
		super(SqlType.STRING, new Class<?>[] { JSONObject.class });
		// TODO Auto-generated constructor stub
	}

	public static JSONObjectPersister getSingleton() {
		return singleTon;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		JSONObject json = (JSONObject) javaObject;
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
			result = new JSONObject((String) sqlArg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
