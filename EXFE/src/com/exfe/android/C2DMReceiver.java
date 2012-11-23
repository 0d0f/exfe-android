package com.exfe.android;

import org.apache.http.HttpStatus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.exfe.android.controller.CrossDetailActivity;
import com.exfe.android.controller.LandingActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Response;

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
		final String regId = intent.getStringExtra("registration_id");
		String error = intent.getStringExtra("error");
		String unregistered = intent.getStringExtra("unregistered");

		final Model mModel = ((Application) context.getApplicationContext())
				.getModel();
		if (error != null) {
			// http://developer.android.com/intl/zh-CN/guide/google/gcm/adv.html#retry
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

			//String deviceToken = mModel.Device().getPushToken();
			// Clear the shared prefs
			mModel.Device().setPushToken("");
			mModel.Device().setReg(false);
		} else if (regId != null) {
			// You will get a regId if nothing goes wrong and you tried to
			// register a device
			Log.d(TAG, String.format("Got regId: %s", regId));
			// TODO send regID to server in ANOTHER THREAD
			Runnable run = new Runnable() {

				@Override
				public void run() {
					int retry = 0;
					int code = HttpStatus.SC_CONTINUE;
					mModel.Device().setPushToken(regId);
					mModel.Device().setReg(false);
					while (code != HttpStatus.SC_OK && retry < 3) {
						Response result = mModel.getServer().regDevice(
								mModel.getDeviceId(), regId,
								mModel.getDeviceName());
						code = result.getCode();
						retry++;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (code == HttpStatus.SC_OK) {
						mModel.Device().setReg(true);
						return;
					}
				}
			};
			new Thread(run).start();
		}
	}

	private void handleData(Context context, Intent data) {
		String app_name = (String) context.getText(R.string.app_name);
		/**intent field:
		 * text: message to display
		 * sound
		 * badge
		 * cid: cross id
		 * t: type, ciur
		 * 	c: conversation
		 *  i: invite to a new cross
		 *  u: update
		 *  r: remove a user from exfee
		 */  
		String message = data.getStringExtra("text");
		String cross_id = data.getStringExtra("cid");
		
		long cid = 0L;
		if (cross_id != null) {
			cid = Long.parseLong(cross_id);
		}

		// Create a pending intent to call the HomeActivity when the
		// notification is clicked
		Intent notifyIntent = null;
		if (cid > 0) {
			String type = data.getStringExtra("t"); // ciur
			if (type.equals("c")) {
				notifyIntent = new Intent(context, CrossDetailActivity.class);
				notifyIntent.putExtra(CrossDetailActivity.FIELD_CROSS_ID, cid);
				notifyIntent
						.putExtra(CrossDetailActivity.FIELD_CROSS_SIDE, false);
				// need refresh conversation
			}else if (type.equals("r")) {
				// no access to the cross
				// need refresh cross list
			}
			else {
				notifyIntent = new Intent(context, CrossDetailActivity.class);
				notifyIntent.putExtra(CrossDetailActivity.FIELD_CROSS_ID, cid);
				notifyIntent.putExtra(CrossDetailActivity.FIELD_CROSS_SIDE, true);
				notifyIntent.putExtra(CrossDetailActivity.FIELD_REFRESH_CROSS, true);
			}
		}

		if (notifyIntent == null) {
			notifyIntent = new Intent(context, LandingActivity.class);
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, -1,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		
		builder.setContentTitle(app_name).setContentText(message)
				.setAutoCancel(true).setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.notification_icon).setLights(Color.rgb(0xDD, 0xEA, 0xF9), 500, 500);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify((int) cid, builder.build());
	}
}
