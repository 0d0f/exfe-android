package com.exfe.android.db;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

public class EntityPersister extends StringType {

	private static final EntityPersister singleTon = new EntityPersister();

	private EntityPersister() {
		super(SqlType.STRING, new Class<?>[] { Entity.class });
		// TODO Auto-generated constructor stub
	}

	public static EntityPersister getSingleton() {
		return singleTon;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		Entity entity = (Entity) javaObject;
		if (entity == null) {
			return null;
		} else {
			return entity.toString();
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		Object result = null;
		try {
			result = EntityFactory.create(new JSONObject((String) sqlArg));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
