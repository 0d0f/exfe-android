package com.exfe.android.model.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class ExfeSQLiteOpenHelper extends SQLiteOpenHelper {

	private final static String DB_NAME = "exfe.db";
	private final static int DB_VERSION = 1;
	private final static String TABLE_NAME_CROSSES = "crosses";

	public ExfeSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/*
	 * public ExfeSQLiteOpenHelper(Context context, String name, CursorFactory
	 * factory, int version) { super(context, name, factory, version); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * public ExfeSQLiteOpenHelper(Context context, String name, CursorFactory
	 * factory, int version, DatabaseErrorHandler errorHandler) { super(context,
	 * name, factory, version, errorHandler); // TODO Auto-generated constructor
	 * stub }
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = String
				.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, $s TEXT);",
						TABLE_NAME_CROSSES, "_id", "relative_id", "create_at", "by_identity_id", "title", "description"
						,"cross_time", "place", "attribute", "exfee_id", "widget");

		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CROSSES);
		onCreate(db);
	}

}
