package com.exfe.android;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.exfe.android.model.Model;
import com.flurry.android.FlurryAgent;

public class Activity extends FragmentActivity {

	protected final String TAG = getClass().getSimpleName();

	protected Model mModel = null;

	public Activity() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getApplicationContext()).getModel();

		try {
			PackageInfo pkg = getPackageManager().getPackageInfo(
					getApplication().getPackageName(), 0);
//			String appName = pkg.applicationInfo.loadLabel(getPackageManager())
//					.toString();
			FlurryAgent.setVersionName(pkg.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Const.FLURRY_APP_ID);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}

	public void registGCM() {
		Intent regIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		regIntent.putExtra(Const.GCM_FIELD_APP,
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		regIntent.putExtra(Const.GCM_FIELD_SENDER, Const.PUSH_PROJECT_ID);
		startService(regIntent);
	}

	public void unregistGCM() {
		Intent unregIntent = new Intent(
				"com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra(Const.GCM_FIELD_APP,
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		startService(unregIntent);
	}
}
