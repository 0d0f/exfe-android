package com.exfe.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.net.ServerAPI1;
import com.exfe.android.net.ServerAPI2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class UserSettingsActivity extends Activity implements Observer {
	private static final String TAG = UserSettingsActivity.class
			.getSimpleName();

	private ImageView mAvatar = null;
	private TextView mName = null;
	private ListView mList = null;
	private List<Identity> mIdentityData = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_user_settings);

		View btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(mBackClick);

		View btnSignOut = findViewById(R.id.btn_sign_out);
		btnSignOut.setOnClickListener(mLogout);

		mAvatar = (ImageView) findViewById(R.id.user_avatar);
		mName = (TextView) findViewById(R.id.user_name);
		mList = (ListView) findViewById(R.id.user_list);

		String my_identites = mModel.getMyIdentites();
		String my_users = mModel.getMyUsers();
		if (TextUtils.isEmpty(my_identites) || TextUtils.isEmpty(my_users)) {
			mIdentityData = new ArrayList<Identity>();

			// identity
			// device
		} else {
			// convert my_identites to mIdentityData
			mIdentityData = new ArrayList<Identity>();
			// convert my_users to user
			User user = null;
			loadUser(user);
		}

		new getProfileTask().execute();
	}

	View.OnClickListener mLogout = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final String deviceToken = mModel.getDeviceToken();
			mModel.setToken("");
			mModel.setUserId(0);
			mModel.setProvider("");
			mModel.setUsername("");
			mModel.setDeviceToken("");
			mModel.setDeviceTokenReg("");
			mModel.setLastUpdateTime("");
			mModel.setMyUsers("");
			mModel.setMyIdentites("");

			/*
			 * 3 clean db DBUtil *dbu=[DBUtil sharedManager]; [dbu emptyDBData];
			 */
			
			Runnable run = new Runnable() {
				public void run() {
					mModel.getServer().signOut(deviceToken);
				}
			};
			new Thread(run).start();
			
			finish();
		}
	};

	View.OnClickListener mBackClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	protected void loadUser(User user) {
		if (user != null) {
			// set avatar
			String avatar_file_name = user.getAvatarFilename();
			if (TextUtils.isEmpty(avatar_file_name)) {
				mAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
			} else {
				new fillAvatarTask().execute(avatar_file_name);
			}
			// set name
			String name = user.getName();
			mName.setText(name);
		}
	}

	protected void loadList(List<Identity> identityData) {
		if (identityData != null) {
			ListAdapter adapter = new ArrayAdapter<Identity>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					identityData);
			mList.setAdapter(adapter);
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	private class fillAvatarTask extends AsyncTask<String, Integer, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			// TODO Auto-generated method stub
			// 1 get img url from params[0]
			// 2 get bitmap from img url
			// 3 convert bitmap to drawable
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Drawable result) {
			super.onPostExecute(result);
			if (result != null) {
				mAvatar.setImageDrawable(result);
			}
		}

	}

	private class getProfileTask extends AsyncTask<String, Integer, String> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		@Override
		protected void onCancelled(String result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... params) {
			return mModel.getServerv1().getProfile();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			//super.onPostExecute(result);
			if (result != null) {
				try {
					Response res = new Response(result);
					int code = res.getCode();
					switch (code) {
					case HttpStatus.SC_OK:

						JSONObject resp = res.getResponse();
						JSONArray identities = resp.getJSONArray("identities");
						List<Identity> ids = new ArrayList<Identity>();
						List<Identity> devs = new ArrayList<Identity>();
						for (int i = 0; i < identities.length(); i++) {
							Identity identity = new Identity(
									identities.getJSONObject(i));
							if ("iOSAPN".equals(identity.getProvider())) {
								devs.add(identity);
							} else {
								ids.add(identity);
							}
						}
						mIdentityData.clear();
						mIdentityData.addAll(ids);
						mIdentityData.addAll(devs);
						// conver mIdentityData to string
						String idsString = identities.toString();
						mModel.setMyIdentites(idsString);
						loadList(mIdentityData);

						JSONObject myself = resp.getJSONObject("user");
						User user = new User(myself);
						// convert user to userString;
						String userString = myself.toString();
						mModel.setMyUsers(userString);
						loadUser(user);

						break;
					case HttpStatus.SC_NOT_FOUND:
						break;
					default:
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
