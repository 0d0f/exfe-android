package com.exfe.android;

import android.os.Bundle;

import com.exfe.android.model.Model;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class Activity extends android.app.Activity{

	protected final String TAG = getClass().getSimpleName();
	
	protected Model mModel = null;
	
	public Activity() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application)getApplicationContext()).getModel();
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}
}
