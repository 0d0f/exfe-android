package com.exfe.android;

import android.os.Bundle;

import com.exfe.android.model.Model;

public class Activity extends android.app.Activity{

	private static final String TAG = Activity.class.getSimpleName();
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
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}
}
