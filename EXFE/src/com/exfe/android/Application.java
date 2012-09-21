package com.exfe.android;

import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.util.ImageCache;

import android.content.res.Configuration;

public class Application extends android.app.Application {

	private static final String TAG = Application.class.getSimpleName();
	private Model mModel = null;
	
	public Application() {
		Log.v(TAG, ".ctor()");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.v(TAG, "onConfigurationChanged");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mModel = new Model(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		mModel.releaseHelper();
		mModel = null;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.v(TAG, "onLowMemory");
	}

//	@Override
//	public void onTrimMemory(int level) {
//		super.onTrimMemory(level);
//		Log.v(TAG, "onTrimMemory level: %d", level);
//	}

	public Model getModel() {
		return mModel;
	}

	public void setModel(Model model) {
		this.mModel = model;
	}

}
