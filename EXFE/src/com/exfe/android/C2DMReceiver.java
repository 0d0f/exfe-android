package com.exfe.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.exfe.android.controller.CrossDetailActivity;
import com.exfe.android.controller.PortalActivity;
import com.exfe.android.model.Model;
import com.exfe.android.util.Tool;

public class C2DMReceiver extends BroadcastReceiver {

	public static final String TAG = C2DMReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		String action = intent.getAction();
		// The action for registration reply is
		// com.google.android.c2dm.intent.REGISTRATION
		if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
			Log.d(TAG, "Received C2DM Registration Packet");
			// Call the handleRegistration function to handle registration
			handleRegistration(context, intent);
		} else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
			Log.d(TAG, "Received C2DM Data Packet");
			// Call the handleData function to handle c2dm packet
			handleData(context, intent);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		// These strings are sent back by google
		String regId = intent.getStringExtra("registration_id");
		String error = intent.getStringExtra("error");
		String unregistered = intent.getStringExtra("unregistered");

		Model mModel = ((Application) context.getApplicationContext())
				.getModel();
		if (error != null) {
			// If there is an error, then we log the error
			Log.e(TAG, String.format("Received error: %s\n", error));
			if (error.equals("ACCOUNT MISSING")) {
				// ACCOUNT MISSING is sent back when the device doesn't have a
				// google account registered
				Toast.makeText(context,
						"Please add a google account to your device.",
						Toast.LENGTH_LONG).show();
			} else {
				// Other errors
				Toast.makeText(context, "Registration Error: " + error,
						Toast.LENGTH_LONG).show();
			}
		} else if (unregistered != null) {
			// This is returned when you are unregistering your device from c2dm
			Log.d(TAG, String.format("Unregistered: %s\n", unregistered));
			Toast.makeText(context, "Unregistered: " + unregistered,
					Toast.LENGTH_LONG).show();
			// TODO: send POST to web server to unregister device from sending
			// list
			String deviceToken = mModel.Me().getDeviceToken();
			// mModel.getServer().signOut(deviceToken);
			// mModel.getServerv1().disconnectDeviceToken(deviceToken);
			// Clear the shared prefs
			mModel.Me().setDeviceToken("");
			mModel.Me().setDeviceIsReg(false);
			// Update our Home Activity
			// updateHome(context);

		} else if (regId != null) {
			// You will get a regId if nothing goes wrong and you tried to
			// register a device
			Log.d(TAG, String.format("Got regId: %s", regId));
			// TODO send regID to server in ANOTHER THREAD
			new RegDeviceTokenTask(mModel).execute(regId);

			// Update our Home Activity
			// updateHome(context);
		}
	}

	private void handleData(Context context, Intent intent) {
		String app_name = (String) context.getText(R.string.app_name);
		String message = intent.getStringExtra("message");
		long cid = intent.getLongExtra("cid", 0L);

		// Use the Notification manager to send notification
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Create a notification using android stat_notify_chat icon.
		Notification notification = new Notification(
				android.R.drawable.stat_notify_chat, app_name + ": " + message,
				0);

		// Create a pending intent to call the HomeActivity when the
		// notification is clicked
		Intent notifyIntent = null;
		if (cid > 0) {
			String type = intent.getStringExtra("t"); // ciur
			notifyIntent = new Intent(context, CrossDetailActivity.class);
			notifyIntent.putExtra(CrossDetailActivity.FIELD_CROSS_ID, cid);
			if (type.equals("c")){
				// show conversation
				notifyIntent.putExtra(CrossDetailActivity.FIELD_SHOW_CONVERSATION, true);
			}
		}
		
		if (notifyIntent == null){
			notifyIntent = new Intent(context, PortalActivity.class);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(context, -1,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT); //
		notification.when = System.currentTimeMillis();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// Set the notification and register the pending intent to it
		notification.setLatestEventInfo(context, app_name, message,
				pendingIntent); //

		// Trigger the notification
		notificationManager.notify(0, notification);
	}

	class RegDeviceTokenTask extends AsyncTask<String, Integer, String> {

		private Model mModel;
		private String mDeviceToken;

		RegDeviceTokenTask(Model m) {
			mModel = m;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mModel.Me().setDeviceIsReg(false);
		}

		@Override
		protected String doInBackground(String... params) {
			mDeviceToken = params[0];
			// return mModel.getServerv1().regDeviceToken(mDeviceToken,
			// mModel.getDeviceString());
			// TODO need a new api to accept android push token.
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			if (Tool.isJson(result)) {
				try {
					JSONObject response = new JSONObject(result);
					JSONObject meta = response.getJSONObject("meta");
					int code = meta.optInt("code");
					if (code == 200) {
						mModel.Me().setDeviceToken(mDeviceToken);
						mModel.Me().setDeviceIsReg(true);
						return;
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// fail to add token try again?
			return;
		}

	}
}
