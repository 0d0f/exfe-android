package com.exfe.android.model.entity;

import java.sql.SQLException;

import org.json.JSONArray;

import com.exfe.android.db.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

public class Relative extends Entity{
	
	private long mId = NO_ID;
	
	public Relative(){
		mType = EntityFactory.TYPE_WIDGET;
	}
	
	public Relative(JSONArray jsonArray){
		
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
	
	@Override
	public void saveToDao(DatabaseHelper dbhelper){
		try {
			Dao<Relative, Long> dao = dbhelper.getCachedDao(getClass());
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
