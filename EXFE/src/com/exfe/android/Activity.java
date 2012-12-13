package com.exfe.android;

import org.apache.http.HttpStatus;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.exfe.android.controller.LandingActivity;
import com.exfe.android.controller.SearchPlaceActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Response;
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
			// String appName =
			// pkg.applicationInfo.loadLabel(getPackageManager())
			// .toString();
			FlurryAgent.setVersionName(pkg.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Const.FLURRY_APP_ID);
	}

	@Override
	protected void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
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

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}

	protected Handler mUIHandler = new Handler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MessageID.MSG_ID_SHOW_PROGRESS_BAR:
				Log.i(TAG, "process showProgressBar msg");
				if (mProgressDialog == null) {
					mProgressDialog = new ProgressDialog(Activity.this);
				}
				String[] data = (String[]) msg.obj;
				mProgressDialog.setTitle(data[0]);
				mProgressDialog.setMessage(data[1]);

				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				break;
			case MessageID.MSG_ID_DIMISS_PROGRESS_BAR:
				Log.i(TAG, "process hideProgressBar msg");
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				break;
			case MessageID.MSG_ID_SHOW_TOAST:
				int duration = msg.arg1;
				boolean showNow = msg.arg2 == 1;
				String message = msg.obj.toString();
				if (showNow && mToast != null) {
					mToast.cancel();
				}
				if (mToast == null) {
					mToast = Toast.makeText(Activity.this, message, duration);
				} else {
					mToast.setText(message);
					mToast.setDuration(duration);
				}
				mToast.show();
				break;
			default:
				super.handleMessage(msg);

			}

		}

	};

	protected ProgressDialog mProgressDialog = null;

	public void showProgressBar(final String title, final String message) {
		Message.obtain(mUIHandler, MessageID.MSG_ID_SHOW_PROGRESS_BAR,
				new String[] { title, message }).sendToTarget();
	}

	public void showProgressBar(final String title, final String message,
			long delayMS) {
		Log.i(TAG, "send showProgressBar msg");
		Message msg = Message.obtain(mUIHandler,
				MessageID.MSG_ID_SHOW_PROGRESS_BAR, new String[] { title,
						message });
		mUIHandler.sendMessageDelayed(msg, delayMS);
	}

	public void hideProgressBar() {
		Log.i(TAG, "remove showProgressBar msg");
		mUIHandler.removeMessages(MessageID.MSG_ID_SHOW_PROGRESS_BAR);
		Log.i(TAG, "send hideProgressBar msg");
		Message.obtain(mUIHandler, MessageID.MSG_ID_DIMISS_PROGRESS_BAR)
				.sendToTarget();
	}

	protected Toast mToast = null;

	public void showToast(int msg) {
		showToast(getResources().getString(msg), Toast.LENGTH_SHORT);
	}

	public void showToast(String msg) {
		showToast(msg, Toast.LENGTH_SHORT);
	}

	public void showToast(int msg, int duration) {
		showToast(getResources().getString(msg), duration, false);
	}

	public void showToast(String msg, int duration) {
		showToast(msg, duration, false);
	}

	public void showToast(int msg, int duration, boolean showNow) {
		showToast(getResources().getString(msg), duration, showNow);
	}

	public void showToast(String msg, int duration, boolean showNow) {
		Message.obtain(mUIHandler, MessageID.MSG_ID_SHOW_TOAST, duration,
				showNow ? 1 : 0, msg).sendToTarget();
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

	public void signOut() {
		final String deviceToken = mModel.Device().getPushToken();
		final String appKey = mModel.Me().getToken();
		mModel.Me().setToken("");
		mModel.Me().setUserId(0L);
		mModel.Me().setProvider("");
		mModel.Me().setExternalId("");
		mModel.Me().setUsername("");
		mModel.Device().setPushToken("");
		mModel.Crosses().clearCrosses();
		mModel.Crosses().setLastUpdateQuery(null);
		mModel.Me().setProfile(null);

		Runnable run = new Runnable() {

			public void run() {
				FlurryAgent.logEvent("sign_out");
				Response resp = mModel.getServer().signOut(appKey, deviceToken);
				if (resp.getCode() == HttpStatus.SC_OK) {
					mModel.mHandler.post(new Runnable() {

						@Override
						public void run() {
							unregistGCM();
						}
					});
				}
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						Intent it = new Intent();
						it.setClass(Activity.this, LandingActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(it);
						finish();
					}
				});

			}
		};

		new Thread(run).start();
	}
}
