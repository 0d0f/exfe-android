package com.exfe.android.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

public class IdentityArrayPersister extends StringType {

	private static final IdentityArrayPersister singleTon = new IdentityArrayPersister();

	private IdentityArrayPersister() {
		super(SqlType.STRING, new Class<?>[] { List.class });
	}

	public static IdentityArrayPersister getSingleton() {
		return singleTon;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		@SuppressWarnings("unchecked")
		List<Identity> list = (List<Identity>) javaObject;
		if (list == null) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			boolean first = true;
			for(Entity e: list){
				if (first){
					first = false;
				}else{
					sb.append(",");
				}
				sb.append(e.toString());
			}
			sb.append("]");
			return sb.toString();
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		Object result = null;
		try {
			List<Identity> list = new ArrayList<Identity>();
			JSONArray array = new JSONArray((String) sqlArg);
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject obj = array.getJSONObject(i);
						Identity ident = (Identity) EntityFactory.create(obj);
						list.add(ident);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			result = list;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
