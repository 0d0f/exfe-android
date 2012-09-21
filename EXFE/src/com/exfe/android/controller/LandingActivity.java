package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.text.TextUtils;

import com.exfe.android.Activity;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Provider;
import com.flurry.android.FlurryAgent;

public class LandingActivity extends Activity implements Observer,
		Fragment.ActivityCallBack {

	public static final String FIELD_ACTION = "action";
	public static final String FIELD_PROVIDER = "provider";

	public static final int ACTIVITY_RESULT_CROSS = 12345;
	public static final int ACTIVITY_RESULT_SIGNIN = 12346;

	public static final String FIELD_CROSS_ID = "cross_id";

	private String mCurrentProvider = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_landing);

		// Intent it = getIntent();
		showFragment(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (TextUtils.isEmpty(mCurrentProvider) && mModel.Me().getUserId() == 0) {
			showFragment(null);
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();		
	}
	
	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	protected void showFragment(String provider) {
		android.support.v4.app.Fragment fragment = null;
		mCurrentProvider = provider;
		
		
		if (Provider.STR_EMAIL.equalsIgnoreCase(provider)) {
			fragment = new LoginFragment();
			FlurryAgent.logEvent("Fragment_normal_login");
			Bundle args = new Bundle();
			fragment.setArguments(args);
		} else if (Provider.STR_TWITTER.equalsIgnoreCase(provider)) {
			fragment = new TwitterLoginFragment();
			FlurryAgent.logEvent("Fragment_twitter_login");
			Bundle args = new Bundle();
			fragment.setArguments(args);
		} else {

			if (mModel.Me().getUserId() != 0) {
				Log.d(TAG, "Jump to Cross");
				fragment = new CrossListFragment();
				FlurryAgent.logEvent("Fragment_cross_list");
				Bundle args = new Bundle();
				fragment.setArguments(args);
			} else {
				fragment = new PortalFragment();
				FlurryAgent.logEvent("Fragment_portal");
				Bundle args = new Bundle();
				fragment.setArguments(args);
			}
		}
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.x_container, fragment).commit();

	}

	@Override
	public boolean onSwitch(Fragment fragment, Bundle bundle) {
		int action = bundle.getInt(FIELD_ACTION);
		if (action == LandingActivity.ACTIVITY_RESULT_SIGNIN) {
			String provider = bundle.getString(FIELD_PROVIDER);
			showFragment(provider);
		} else if (action == LandingActivity.ACTIVITY_RESULT_CROSS) {
			showFragment(null);
		}

		return false;
	}

}
