package com.exfe.android.db;

import java.sql.SQLException;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.User;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private final String TAG = getClass().getSimpleName();

	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "exfe.sqlite";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;

	private HashMap<Class<?>, Object> daoCache = new HashMap<Class<?>, Object>();

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		try {
			Class<?>[] clzs = { Cross.class, Identity.class, Invitation.class,
					Exfee.class, User.class, Post.class};
			for (Class<?> c : clzs) {
				daoCache.put(c, getDao(c));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		Log.i(TAG, "onCreate");
		createTables();
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		Log.i(TAG, "onUpgrade");
		dropTables();
		// after we drop the old databases, we create the new ones
		createTables();
	}

	private void createTables() {
		try {
			for (Class<?> clz : daoCache.keySet()) {
				TableUtils.createTableIfNotExists(getConnectionSource(), clz);
			}
		} catch (SQLException e) {
			Log.e(TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	private void dropTables() {
		try {
			for (Class<?> clz : daoCache.keySet()) {
				TableUtils.dropTable(getConnectionSource(), clz, true);
			}
		} catch (SQLException e) {
			Log.e(TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our class. It will create it
	 * or just give the cached value.
	 */
	public <D extends Dao<T, ?>, T> D getCachedDao(Class<T> clz)
			throws SQLException {
		@SuppressWarnings("unchecked")
		D dao = (D) daoCache.get(clz);
		if (dao == null) {
			TableUtils.createTableIfNotExists(getConnectionSource(), clz);
			dao = getDao(clz);
			daoCache.put(clz, dao);
		}
		return dao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		daoCache.clear();
	}
	
	public Dao<Cross, Long> getCrossDao(){
		try {
			return getCachedDao(Cross.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Dao<Exfee, Long> getExfeeDao(){
		try {
			return getCachedDao(Exfee.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Dao<Invitation, Long> getInvitationDao(){
		try {
			return getCachedDao(Invitation.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Dao<Identity, Long> getIdentityDao(){
		try {
			return getCachedDao(Identity.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Dao<User, Long> getUserDao() {
		try {
			return getCachedDao(User.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Dao<Post, Long> getPostDao() {
		try {
			return getCachedDao(Post.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
