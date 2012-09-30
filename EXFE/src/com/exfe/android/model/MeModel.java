package com.exfe.android.model;

import java.sql.SQLException;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.exfe.android.PrefKeys;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.j256.ormlite.dao.Dao;

public class MeModel {

	public static final int ACTION_TYPE_UPDATE_MY_PROFILE = Model.ACTION_TYPE_ME_BASE + 1;

	private Model mRoot = null;

	private String mToken = null;
	private long mUserId = 0;
	private String mUsername = null;
	private String mProvider = null;
	private String mExternalId = null;

	public MeModel(Model m) {
		mRoot = m;
		SharedPreferences sp = mRoot.getDefaultSharedPreference();
		mToken = sp.getString(PrefKeys.ME_TOKEN, "");
		mUserId = sp.getLong(PrefKeys.ME_USER_ID, 0L);
		mUsername = sp.getString(PrefKeys.ME_USERNAME, "");
		mProvider = sp.getString(PrefKeys.ME_PROVIDER, Provider.STR_UNKNOWN);
		mExternalId = sp.getString(PrefKeys.ME_EXTERNAL_ID, "");
	}

	private Dao<User, Long> getDao() {
		return mRoot.getHelper().getUserDao();
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
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PrefKeys.ME_TOKEN, token);
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
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putLong(PrefKeys.ME_USER_ID, userId);
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
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PrefKeys.ME_USERNAME, username);
			editor.commit();
		}
		this.mUsername = username;
	}

	/**
	 * @return the external id
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
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PrefKeys.ME_PROVIDER, provider);
			editor.commit();
		}
		this.mProvider = provider;
	}

	/**
	 * @return the external id
	 */
	public String getExternalId() {
		return mExternalId;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setExternalId(String externalId) {
		if (!this.mExternalId.equalsIgnoreCase(externalId)) {
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PrefKeys.ME_EXTERNAL_ID, externalId);
			editor.commit();
		}
		this.mExternalId = externalId;
	}

	/**
	 * @return the profile
	 */
	public User getProfile() {
		try {
			long uid = getUserId();
			if (uid == 0) {
				return null;
			}
			User u = getDao().queryForId(mUserId);
			return u;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param profile
	 *            the profile to set
	 */
	public void setProfile(User profile) {
		try {
			if (profile != null) {

//				User current = mRoot.Me().getProfile();
//				if (current == null || 
//						profile.getLatestModify().getTime() > 
//				current.getLatestModify().getTime()) {
					profile.saveToDao(mRoot.getHelper());
					getDao().createOrUpdate(profile);
					setUserId(profile.getId());
					
					mRoot.setChanged();
					Bundle data = new Bundle();
					data.putInt(Model.OBSERVER_FIELD_TYPE,
							ACTION_TYPE_UPDATE_MY_PROFILE);
					mRoot.notifyObservers(data);
//				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Date lastProfileQuery;

	public void fetchProfile() {
		// Date now = new Date();
		// if (lastProfileQuery == null || (now.getTime() -
		// lastProfileQuery.getTime()) > Const.HALF_HOUR){
		// lastProfileQuery = now;
		//
		// }

		Runnable run = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Response result = mRoot.getServer().getMyProfile();
				if (result != null) {
					try {
						int code = result.getCode();
						switch (code) {
						case HttpStatus.SC_OK:

							JSONObject resp = result.getResponse();
							JSONObject myself = resp.getJSONObject("user");
							User user = (User) EntityFactory.create(myself);

							mRoot.Me().setProfile(user);

							break;
						case HttpStatus.SC_NOT_FOUND:
							break;
						default:
							break;
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		Thread th = new Thread(run);
		th.start();

	}
}
