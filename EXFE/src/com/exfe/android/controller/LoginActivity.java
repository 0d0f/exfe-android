package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.R.anim;
import com.exfe.android.R.id;
import com.exfe.android.R.layout;
import com.exfe.android.R.string;
import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Response;

public class LoginActivity extends Activity implements Observer {

	private static final String TAG = LoginActivity.class.getSimpleName();

	public static final String CALL_BACK = "oauth://handleTwitterLogin";
	public static final String EXFE_TWITTER_LOGIN = Const
			.getOAuthURL("twitterRedirect?device=iOS&device_callback="
					+ CALL_BACK);

	private WebView mWebView;
	private EditText etIdentity = null;
	private EditText etPassword = null;
	private View btnSignIn = null;
	private View btnTwitter = null;
	private ProgressBar pbIndicator = null;
	private TextView tvHint = null;
	private ViewSwitcher mSwitcher = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_login);

		View v = null;

		v = findViewById(R.id.input_indentity);
		if (v != null) {
			etIdentity = (EditText) v;
		}
		v = findViewById(R.id.input_password);
		if (v != null) {
			etPassword = (EditText) findViewById(R.id.input_password);
			etPassword.setOnEditorActionListener(editorAction);
		}

		v = findViewById(R.id.btn_sign_in);
		if (v != null) {
			btnSignIn = v;
			btnSignIn.setOnClickListener(clickListener);
		}

		v = findViewById(R.id.indicator);
		if (v != null) {
			pbIndicator = (ProgressBar) v;
		}

		v = findViewById(R.id.btn_twitter);
		if (v != null) {
			btnTwitter = v;
			btnTwitter.setOnClickListener(clickListener);
		}

		v = findViewById(R.id.label_hint);
		if (v != null) {
			tvHint = (TextView) v;
		}

		v = findViewById(R.id.login_switcher);
		if (v != null) {
			mSwitcher = (ViewSwitcher) v;
		}

		v = findViewById(R.id.login_close);
		if (v != null) {
			v.setOnClickListener(clickListener);
		}

		v = findViewById(R.id.login_webview);
		if (v != null) {
			mWebView = (WebView) v;

			WebSettings webSettings = mWebView.getSettings();
			webSettings.setSavePassword(false);
			webSettings.setSaveFormData(false);
			webSettings.setJavaScriptEnabled(true);
			webSettings.setSupportZoom(false);

			// mWebView.setWebChromeClient(new MyWebChromeClient());
			mWebView.setWebViewClient(new MyWebViewClient());
			mWebView.setVerticalScrollbarOverlay(true);
		}

		if (Const.debug_login_auto_fill) {
			etIdentity.setText(Const.username);
			etPassword.setText(Const.pwd);
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
				new doLoginTask().execute(etIdentity.getText().toString()
						.trim(), "email", etPassword.getText().toString());
				break;
			case R.id.btn_twitter:
				if (mSwitcher != null && mSwitcher.getDisplayedChild() == 0) {
					mSwitcher.showNext();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					if (mWebView != null
							&& (mWebView.getUrl() == null || !mWebView.getUrl()
									.startsWith(Const.TWITTER_OAUTH_PAGE))) {
						mWebView.loadUrl(EXFE_TWITTER_LOGIN);
					}
				}
				break;
			case R.id.login_close:
				if (mSwitcher != null && mSwitcher.getDisplayedChild() != 0) {
					mSwitcher.setDisplayedChild(0);
				}
				break;
			default:
				break;
			}
		}
	};

	final class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "%s", url);
			if (url.contains("token=") && url.startsWith(CALL_BACK)) {

				Uri uri = Uri.parse(url);
				String err = uri.getQueryParameter("err");
				if (err == null) {
					String userid = uri.getQueryParameter("userid");
					String name = uri.getQueryParameter("name");
					String token = uri.getQueryParameter("token");
					String external_id = uri.getQueryParameter("external_id");

					mModel.Me().setUsername(name);
					mModel.Me().setProvider("twitter");
					mModel.Me().setToken(token);
					mModel.Me().setUserId(Long.valueOf(userid));
					mModel.Me().setExternalId(external_id);
					mModel.Me().fetchProfile();

					setResult(RESULT_OK);
					finish();
					overridePendingTransition(0, R.anim.slide_down_out);
				}

				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);

		}

	}

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
			btnSignIn.setEnabled(false);
			pbIndicator.setVisibility(View.VISIBLE);
			btnTwitter.setVisibility(View.GONE);
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
			username = params[0];
			provider = params[1];
			password = params[2];
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
				code = result.getCode();
				switch (code) {
				case HttpStatus.SC_OK:
					JSONObject resp = result.getResponse();
					String token = resp.optString("token");
					long user_id = resp.optLong("user_id");

					mModel.Me().setUsername(username);
					mModel.Me().setProvider(provider);
					mModel.Me().setToken(token);
					mModel.Me().setUserId(user_id);
					mModel.Me().fetchProfile();
					mModel.Me().setExternalId("");

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

			}
			pbIndicator.setVisibility(View.GONE);
			btnSignIn.setEnabled(true);
			btnTwitter.setVisibility(View.VISIBLE);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			pbIndicator.setVisibility(View.GONE);
			btnSignIn.setEnabled(true);
			btnTwitter.setVisibility(View.VISIBLE);
		}

	}

}
