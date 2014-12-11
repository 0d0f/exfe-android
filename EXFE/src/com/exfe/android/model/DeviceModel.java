package com.exfe.android.model;

import android.content.SharedPreferences;

import com.exfe.android.PrefKeys;

public class DeviceModel {
	private Model mRoot = null;

	private String mPushToken = null;
	private boolean isReg = false;

	public DeviceModel(Model m) {
		mRoot = m;
		SharedPreferences sp = mRoot.getDefaultSharedPreference();

		mPushToken = sp.getString(PrefKeys.ME_DEVICE_TOKEN, "");
		isReg = sp.getBoolean(PrefKeys.ME_DEVICE_REGISTED, false);
	}

	/**
	 * @return the devicePushToken
	 */
	public String getPushToken() {
		return this.mPushToken;
	}

	/**
	 * @param devicePushToken
	 *            the deviceToken to set
	 */
	public void setPushToken(String pushToken) {
		if (!this.mPushToken.equalsIgnoreCase(pushToken)) {
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(PrefKeys.ME_DEVICE_TOKEN, pushToken);
			editor.commit();
		}
		this.mPushToken = pushToken;
	}

	/**
	 * @return the deviceIsReg
	 */
	public boolean isReg() {
		return this.isReg;
	}

	/**
	 * @param deviceIsReg
	 *            the deviceIsReg to set
	 */
	public void setReg(boolean reg) {
		if (this.isReg != reg) {
			SharedPreferences sp = mRoot.getDefaultSharedPreference();
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean(PrefKeys.ME_DEVICE_REGISTED, reg);
			editor.commit();
		}
		this.isReg = reg;
	}
}
