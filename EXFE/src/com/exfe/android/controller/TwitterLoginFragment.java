package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.exfe.android.Const;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Provider;

public class TwitterLoginFragment extends Fragment implements Observer {

	private static final String TAG = TwitterLoginFragment.class
			.getSimpleName();

	public static final String CALL_BACK = "oauth://handleTwitterLogin";
	public static final String EXFE_TWITTER_LOGIN = Const
			.getOAuthURL("TwitterAuthenticate?device=Android&device_callback="
					+ CALL_BACK);

	private Fragment.ActivityCallBack mCallBack;
	private WebView mWebView;
	private ProgressBar mProgress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_twitter_login, container,
				false);
		return v;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		View v = null;

		v = view.findViewById(R.id.login_close);
		if (v != null) {
			v.setOnClickListener(clickListener);
		}

		v = view.findViewById(R.id.login_progress);
		if (v != null) {
			mProgress = (ProgressBar) v;
		}

		v = view.findViewById(R.id.login_webview);
		if (v != null) {
			mWebView = (WebView) v;

			WebSettings webSettings = mWebView.getSettings();
			webSettings.setSavePassword(false);
			webSettings.setSaveFormData(false);
			webSettings.setJavaScriptEnabled(true);
			webSettings.setSupportZoom(false);

			mWebView.setWebChromeClient(new MyWebChromeClient());
			mWebView.setWebViewClient(new MyWebViewClient());
			mWebView.setVerticalScrollbarOverlay(true);

			mWebView.loadUrl(EXFE_TWITTER_LOGIN);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(android.app.Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (activity instanceof Fragment.ActivityCallBack) {
			mCallBack = (Fragment.ActivityCallBack) activity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mCallBack = null;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.login_close:
				if (mCallBack != null) {
					Bundle param = new Bundle();
					param.putInt(LandingActivity.FIELD_ACTION,
							LandingActivity.ACTIVITY_RESULT_SIGNIN);
					param.putString(LandingActivity.FIELD_PROVIDER,
							Provider.STR_EMAIL);
					mCallBack.onSwitch(TwitterLoginFragment.this, param);
				}
				break;
			default:
				break;
			}
		}
	};

	final class MyWebChromeClient extends WebChromeClient {

		public void onProgressChanged(WebView view, int progress) {
			if (mProgress != null) {
				if (progress < 100) {
					mProgress.setVisibility(View.VISIBLE);
				} else {
					mProgress.setVisibility(View.INVISIBLE);
				}
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
					mModel.Me().setProvider(Provider.STR_TWITTER);
					mModel.Me().setToken(token);
					mModel.Me().setUserId(Long.valueOf(userid));
					mModel.Me().setExternalId(external_id);
					
					mModel.Me().fetchProfile();

					if (mCallBack != null) {
						Bundle param = new Bundle();
						param.putInt(LandingActivity.FIELD_ACTION,
								LandingActivity.ACTIVITY_RESULT_CROSS);
						mCallBack.onSwitch(TwitterLoginFragment.this, param);
					}
				}

				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);

		}

	}

}
