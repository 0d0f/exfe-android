package com.exfe.android;

import android.os.Bundle;

import com.exfe.android.model.Model;

public class Fragment extends android.support.v4.app.Fragment {

	public interface ActivityCallBack{
		public boolean onSwitch(Fragment fragment, Bundle bundle);
	}
	
	protected final String TAG = getClass().getSimpleName();
	protected Model mModel = null;
	
	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getActivity().getApplicationContext()).getModel();

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
}
