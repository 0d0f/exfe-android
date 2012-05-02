package com.exfe.android;

import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Response;

public class LoginActivity extends Activity implements Observer {

	private static final String TAG = LoginActivity.class.getSimpleName();
	
	public static final int RESULT_EXIT_LOGIN = 1;

	private int mBackPressCount = 0;
	private EditText etIdentity = null;
	private EditText etPassword = null;
	private Button btnSignIn = null;
	private ProgressBar pbIndicator = null;
	private TextView tvHint = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_login);

		etIdentity = (EditText) findViewById(R.id.input_indentity);
		etPassword = (EditText) findViewById(R.id.input_password);
		etPassword.setOnEditorActionListener(editorAction);
		btnSignIn = (Button) findViewById(R.id.btn_sign_in);
		btnSignIn.setOnClickListener(clickListener);
		pbIndicator = (ProgressBar) findViewById(R.id.indicator);
		tvHint = (TextView) findViewById(R.id.label_hint);
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
		//super.onBackPressed();
		if (mBackPressCount == 0){
			Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show();
			mBackPressCount ++;
		}else{
			setResult(RESULT_EXIT_LOGIN);
			finish();
		}
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

	private EditText.OnEditorActionListener editorAction = new EditText.OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_GO
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				if (btnSignIn != null) {
					btnSignIn.performClick();
				}
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.btn_sign_in:
				new doLoginTask().execute();
				break;
			default:
				break;
			}
		}
	};

	private class doLoginTask extends AsyncTask<String, Integer, Response> {

		String username = null;
		String password = null;
		String provider = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		@Override
		protected void onCancelled(Response result) {
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
			username = etIdentity.getText().toString();
			password = etPassword.getText().toString();
			provider = "email";

			btnSignIn.setEnabled(false);
			pbIndicator.setVisibility(View.VISIBLE);
			tvHint.setText("");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Response doInBackground(String... params) {
			// TODO Auto-generated method stub
			Response response = mModel.getServer().signIn(username, provider,
					password);
			return response;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			if (result != null) {
				int code;
				try {
					code = result.getCode();
					switch (code) {
					case HttpStatus.SC_OK:
						JSONObject resp = result.getResponse();
						String token = resp.getString("token");
						long user_id = resp.getLong("user_id");

						mModel.setUsername(username);
						mModel.setProvider(provider);
						mModel.setToken(token);
						mModel.setUserId(user_id);

						setResult(RESULT_OK);
						finish();
						overridePendingTransition(0, R.anim.slide_down_out);
						break;
					case HttpStatus.SC_NOT_FOUND:
						tvHint.setText(R.string.login_sign_in_fail_hint);
						break;
					default:
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			pbIndicator.setVisibility(View.INVISIBLE);
			btnSignIn.setEnabled(true);

		}
	}

}
