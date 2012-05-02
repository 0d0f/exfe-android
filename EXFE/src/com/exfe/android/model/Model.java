package com.exfe.android.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import com.exfe.android.Application;
import com.exfe.android.debug.Log;
import com.exfe.android.model.db.ExfeSQLiteOpenHelper;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.net.ServerAPI1;
import com.exfe.android.net.ServerAPI2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class Model extends Observable {

	private static final String TAG = Model.class.getSimpleName();

	public static final String MODEL_PREF_KEY_TOKEN = "token";
	public static final String MODEL_PREF_KEY_USERNAME = "username";
	public static final String MODEL_PREF_KEY_PROVIDER = "provider";
	public static final String MODEL_PREF_KEY_USER_ID = "userid";

	public static final String OBSERVER_FIELD_TYPE = "ACTION_TYPE";

	public static final int ACTION_TYPE_NOTIFICATION_BASE = 100;
	public static final int ACTION_TYPE_UPDATE_CROSSES = ACTION_TYPE_NOTIFICATION_BASE + 1;

	private Application mAppContext = null;
	private SQLiteOpenHelper mSQLHelper = null;
	private String mToken = null;
	private long mUserId = 0;
	private String mUsername = null;
	private String mProvider = null;
	private String mDeviceToken = null;
	private String mDeviceTokenReg = null;
	private String mLastUpdateTime = null;
	private String mMyUsers = null;
	private String mMyIdentites = null;

	private HashMap<Long, Cross> mCross = new HashMap<Long, Cross>();

	public Model(Application appContext) {
		super();
		if (appContext == null) {
			throw new IllegalArgumentException("appConext should not be null.");
		}
		mAppContext = appContext;

		SharedPreferences sp = getDefaultSharedPreference();
		mToken = sp.getString(MODEL_PREF_KEY_TOKEN, "");
		mUserId = sp.getLong(MODEL_PREF_KEY_USER_ID, 0);
		mUsername = sp.getString(MODEL_PREF_KEY_USERNAME, "");
		mProvider = sp.getString(MODEL_PREF_KEY_PROVIDER, "");

		// mAppContext.registerActivityLifecycleCallbacks(mLifecycle);
	}

	public ServerAPI1 getServerv1() {
		return new ServerAPI1(this);
	}

	public ServerAPI2 getServer() {
		return new ServerAPI2(this);
	}

	public SQLiteOpenHelper getSQLHepler() {
		if (mSQLHelper == null) {
			synchronized (mSQLHelper) {
				if (mSQLHelper == null) {
					mSQLHelper = new ExfeSQLiteOpenHelper(getAppContext());
				}
			}
		}
		return mSQLHelper;
	}

	public SharedPreferences getDefaultSharedPreference() {
		return getAppContext().getSharedPreferences("model",
				Context.MODE_PRIVATE);
	}

	public Application getAppContext() {
		return mAppContext;
	}

	public void setAppContext(Application appContext) {
		this.mAppContext = appContext;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return this.mToken;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(String token) {
		if (!this.mToken.equalsIgnoreCase(token)) {
			SharedPreferences sp = getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(MODEL_PREF_KEY_TOKEN, token);
			editor.commit();
		}
		this.mToken = token;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return this.mUserId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(long userId) {
		if (this.mUserId != userId) {
			SharedPreferences sp = getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putLong(MODEL_PREF_KEY_USER_ID, userId);
			editor.commit();
		}
		this.mUserId = userId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return this.mUsername;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		if (!this.mUsername.equalsIgnoreCase(username)) {
			SharedPreferences sp = getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(MODEL_PREF_KEY_USERNAME, username);
			editor.commit();
		}
		this.mUsername = username;
	}

	/**
	 * @return the provider
	 */
	public String getProvider() {
		return mProvider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(String provider) {
		if (!this.mProvider.equalsIgnoreCase(provider)) {
			SharedPreferences sp = getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(MODEL_PREF_KEY_PROVIDER, provider);
			editor.commit();
		}
		this.mProvider = provider;
	}

	/**
	 * @return the deviceToken
	 */
	public String getDeviceToken() {
		return this.mDeviceToken;
	}

	/**
	 * @param deviceToken
	 *            the deviceToken to set
	 */
	public void setDeviceToken(String deviceToken) {
		this.mDeviceToken = deviceToken;
	}

	/**
	 * @return the deviceTokenReg
	 */
	public String getDeviceTokenReg() {
		return this.mDeviceTokenReg;
	}

	/**
	 * @param deviceTokenReg
	 *            the deviceTokenReg to set
	 */
	public void setDeviceTokenReg(String deviceTokenReg) {
		this.mDeviceTokenReg = deviceTokenReg;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public String getLastUpdateTime() {
		return this.mLastUpdateTime;
	}

	/**
	 * @param lastUpdateTime
	 *            the lastUpdateTime to set
	 */
	public void setLastUpdateTime(String lastUpdateTime) {
		this.mLastUpdateTime = lastUpdateTime;
	}

	/**
	 * @return the myUsers
	 */
	public String getMyUsers() {
		return this.mMyUsers;
	}

	/**
	 * @param myUsers
	 *            the myUsers to set
	 */
	public void setMyUsers(String myUsers) {
		this.mMyUsers = myUsers;
	}

	/**
	 * @return the myIdentites
	 */
	public String getMyIdentites() {
		return this.mMyIdentites;
	}

	/**
	 * @param myIdentites
	 *            the myIdentites to set
	 */
	public void setMyIdentites(String myIdentites) {
		this.mMyIdentites = myIdentites;
	}

	public String getDeviceString() {
		String androidId = Settings.System.getString(
				mAppContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		return String.format("%s_%s", Build.MODEL, androidId);
	}

	public void addCrosses(List<Cross> xs) {
		if (xs != null && !xs.isEmpty()) {
			List<Long> update = new ArrayList<Long>();
			for (Cross x : xs) {
				if (x != null) {
					if (mCross.containsKey(x.getId())) {
						// update cross?
					} else {
						mCross.put(x.getId(), x);
						update.add(x.getId());
					}
				}
			}
			if (update.size() > 0) {
				setChanged();
				Bundle b = new Bundle();
				b.putInt(OBSERVER_FIELD_TYPE, ACTION_TYPE_UPDATE_CROSSES);
				long[] value = new long[update.size()];
				for (int i = 0; i < value.length; i++) {
					value[i] = update.get(i);
				}

				b.putLongArray("update", value);
				notifyObservers(b);
			}
		}
	}

	public void clearCrosses() {
		mCross.clear();
		setChanged();
		Bundle b = new Bundle();
		b.putInt(OBSERVER_FIELD_TYPE, ACTION_TYPE_UPDATE_CROSSES);
		notifyObservers(b);
	}

	public Collection<Cross> getCrosses() {
		return mCross.values();
	}
	
	public Cross getCrossById(long id){
		return mCross.get(id);
	}
}
