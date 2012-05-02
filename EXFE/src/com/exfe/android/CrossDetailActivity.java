package com.exfe.android;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Background;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Place;
import com.exfe.android.util.ImageCache;

public class CrossDetailActivity extends Activity implements Observer {

	private static final String TAG = CrossDetailActivity.class.getSimpleName();

	public static final String FIELD_CROSS_ID = "cross_id";

	private long mCrossId = 0;
	private Cross mCross = null;

	private WebView mWebView;

	private Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_cross_detail);

		Intent it = getIntent();
		mCrossId = it.getLongExtra(FIELD_CROSS_ID, 0);
		if (mCrossId > 0) {
			mCross = mModel.getCrossById(mCrossId);
		} else {
			mCross = null;
		}

		View v = findViewById(R.id.nav_btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_title);
		if (v != null) {
			TextView title = (TextView) v;
			title.setText(mCross.getTitle());
		}

		mWebView = (WebView) findViewById(R.id.x_web_view);

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);

		mWebView.setWebChromeClient(new MyWebChromeClient());
		mWebView.setWebViewClient(new MyWebViewClient());

		mWebView.addJavascriptInterface(new ExfeJavaScriptInterface(mWebView, this), "exfe");

		// mWebView.loadData(generatePage("x.html"), "text/html; charset=utf-8",
		// "");
		mWebView.loadDataWithBaseURL("http://exfe.com/",
				generatePage("x.html"), "text/html", "utf-8", null);

	}

	private void replaceOccurrences(StringBuilder sb, String oldStr,
			String newStr) {
		int offset = sb.indexOf(oldStr);
		while (offset != -1) {
			sb.replace(offset, offset + oldStr.length(), newStr);
			offset = sb.indexOf(oldStr, offset + 1);
		}
	}

	private String generatePage(String filename) {
		String result = null;
		try {
			InputStream in = getAssets().open(filename);
			int lenght = in.available();
			byte[] buffer = new byte[lenght];
			in.read(buffer);
			StringBuilder sb = new StringBuilder(EncodingUtils.getString(
					buffer, "UTF-8"));

			// time
			// "{#begin_at_human#}"
			replaceOccurrences(sb, "{#begin_at_human#}", mCross.getTime().getBeginAt().getRelativeStringToNow(getResources()));
			// "{#begin_at#}"
			// "{#hidden_calendar#}"
			// "{#show_detail_time#}"
			// "{#show_detail_time#}

			// Address
			Place p = mCross.getPlace();
			String mapimg = String
					.format("https://maps.googleapis.com/maps/api/staticmap?center=%s,%s&markers=size:mid|color:blue|%s,%s&zoom=13&size=130x75&sensor=false",
							p.getLat(), p.getLng(), p.getLat(), p.getLng());
			if (TextUtils.isEmpty(p.getTitle())) {
				if (!TextUtils.isEmpty(p.getLat())
						&& !TextUtils.isEmpty(p.getLng())) {

					replaceOccurrences(sb, "{#map_img_url#}", mapimg);
					replaceOccurrences(sb, "{#show_map_img#}", "display:block");
					replaceOccurrences(sb, "{#place_line1#}", "Somewhere");
				} else {
					replaceOccurrences(sb, "{#place_line1#}", "Any Place");
					replaceOccurrences(sb, "{#show_map_img#}", "display:none");
					replaceOccurrences(sb, "{#nomap#}", "nomap");
				}
			} else {
				replaceOccurrences(sb, "{#place_line1#}", p.getTitle());
				replaceOccurrences(sb, "{#map_display#}", "inline");
				if (!TextUtils.isEmpty(p.getLat())
						&& !TextUtils.isEmpty(p.getLng())) {
					replaceOccurrences(sb, "{#map_img_url#}", mapimg);
					replaceOccurrences(sb, "{#show_map_img#}", "display:block");
				} else {
					replaceOccurrences(sb, "{#show_map_img#}", "display:none");
					replaceOccurrences(sb, "{#nomap#}", "nomap");
				}
				replaceOccurrences(sb, "{#place_line2#}", p.getDescription()
						.replace("\n", "<br/>"));
			}

			// background
			Entity bk = mCross
					.getWidgetByCategory(EntityFactory.STRING_BACKGROUND);
			if (bk != null) {
				Background b = (Background) bk;
				replaceOccurrences(sb, "{#background_img#}",
						String.format(
								"http://img.exfe.com/xbgimage/%s_ios.jpg",
								b.getImage()));
			} else {
				replaceOccurrences(sb, "{#background_img#}", "x_background.png");
			}

			// exfee
			StringBuilder exfee = new StringBuilder();
			int confirmed_count = 0;
			for (Invitation inv : mCross.getExfee().getInvitations()) {
				if (inv.getRsvpStatus() == Invitation.ACCEPTED) {
					confirmed_count++;
				}
				String imgName = inv.getIdentity().getAvatarFilename();
				if (!TextUtils.isEmpty(imgName)) {
					String imgurl = ImageCache.getImageUrl(imgName);
					String host = "";
					String withnum = "";

					if (inv.getIdentity().getId() == mCross.getHostIdentity()
							.getId()) {
						host = "<span class='rt'>H</span>";
					}
					// with Number
					// if(inv.withnum > 0){
					// withnum=String.format("<span class='lt'>%d</span>",inv.withnum);
					// }

					String exfee_fmt = null;
					if (inv.getRsvpStatus() == Invitation.ACCEPTED) {
						exfee_fmt = "<li id='avatar_%d'><img alt='' width='40px' height='40px' src='%s' />%s%s</li>";
					} else {
						exfee_fmt = "<li id='avatar_%d' class='opacity'><img alt='' width='40px' height='40px' src='%s' />%s%s</li>";
					}
					exfee.append(String.format(exfee_fmt, inv.getId(), imgurl,
							host, withnum));
				}

				if (inv.getIdentity().getId() == mModel.getUserId()) {
					int my_rsvp = inv.getRsvpStatus();
					if (my_rsvp == Invitation.ACCEPTED
							|| my_rsvp == Invitation.DECLINED
							|| my_rsvp == Invitation.INTERESTED) {
						replaceOccurrences(sb, "{#rsvp_btn_show#}",
								"style='display:none'");
						switch (my_rsvp) {
						case Invitation.ACCEPTED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Accept");
							break;
						case Invitation.DECLINED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Declined");
							break;
						case Invitation.INTERESTED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Interested");
							break;
						}
						replaceOccurrences(sb, "{#rsvp_opt_show#}",
								"style='display:block");
					} else {
						replaceOccurrences(sb, "{#rsvp_opt_show#}",
								"style='display:none");
					}
				}
			}
			replaceOccurrences(sb, "{#confirmed_num#}",
					String.valueOf(confirmed_count));
			replaceOccurrences(sb, "{#all_num#}",
					String.valueOf(mCross.getExfee().getInvitations().size()));
			replaceOccurrences(sb, "{#exfee_list#}", exfee.toString());

			// title & description
			replaceOccurrences(sb, "{#title#}", mCross.getTitle());
			replaceOccurrences(sb, "{#description#}", mCross.getDescription()
					.replace("\n", "<br/>"));

			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.nav_btn_back:
				finish();
				break;
			case R.id.nav_btn_action:
				break;
			default:
				break;
			}
		}
	};

	final class ExfeJavaScriptInterface {
		Context mContext;
		WebView mWebView;

	    /** Instantiate the interface and set the context */
		ExfeJavaScriptInterface(WebView w, Context c) {
	        mWebView = w;
			mContext = c;
	    }
		
		public void rsvp(final int rvsp, final String call_back){
			mHandler.post(new Runnable() {
				public void run() {
					// handle response in UI thread
					//mWebView.loadUrl(String.format("javascript:%s(%s)", call_back, ""));
				}
			});
		}

		/**
		 * This is not called on the UI thread. Post a runnable to invoke
		 * loadUrl on the UI thread.
		 */
		public void clickOnAndroid() {
			mHandler.post(new Runnable() {
				public void run() {
					mWebView.loadUrl("javascript:wave()");
				}
			});

		}
	}

	/**
	 * Provides a hook for calling "alert" from javascript. Useful for debugging
	 * your javascript.
	 */
	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			Log.d(TAG, message);
			result.confirm();
			return true;
		}
	}

	final class MyWebViewClient extends WebViewClient {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#shouldInterceptRequest(android.webkit
		 * .WebView, java.lang.String)
		 */
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view,
				String url) {
			// TODO Auto-generated method stub
			return super.shouldInterceptRequest(view, url);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit
		 * .WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			return super.shouldOverrideUrlLoading(view, url);
		}

	}
}
