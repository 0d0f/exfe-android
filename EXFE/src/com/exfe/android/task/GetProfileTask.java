package com.exfe.android.task;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.exfe.android.model.Model;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;

public class GetProfileTask extends AsyncTask<String, Integer, Response> {

	private Model mModel;
	
	public GetProfileTask(Model m){
		mModel = m;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
	 */
	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
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
	protected Response doInBackground(String... params) {
		return mModel.getServer().getProfile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Response result) {
		// super.onPostExecute(result);
		if (result != null) {
			try {
				int code = result.getCode();
				switch (code) {
				case HttpStatus.SC_OK:

					JSONObject resp = result.getResponse();
					JSONObject myself = resp.getJSONObject("user");
					User user = (User) EntityFactory.create(myself);

					mModel.Me().setProfile(user);

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