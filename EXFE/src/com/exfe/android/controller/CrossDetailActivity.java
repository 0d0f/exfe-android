package com.exfe.android.controller;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.android.apis.animation.Rotate3dAnimation;
import com.exfe.android.Activity;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.ConversationModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Background;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Place;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.Rsvp;
import com.exfe.android.util.ImageCache;
import com.exfe.android.util.Tool;

public class CrossDetailActivity extends Activity implements Observer {

	public static final String FIELD_CROSS_ID = "cross_id";
	public static final String FIELD_SHOW_CONVERSATION = "show_conversation";

	private long mCrossId = 0;
	private Cross mCross = null;
	private long mIdentityId = 0;

	private ViewSwitcher mSwitcher;
	private EditText mInput;
	private WebView mWebView;
	private ListView mConversationListView;

	private ConversationAdpater mAdapter = null;

	private Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_cross_detail);

		Intent it = getIntent();
		mCrossId = it.getLongExtra(FIELD_CROSS_ID,
				it.getIntExtra(FIELD_CROSS_ID, 0));

		if (mCrossId > 0) {
			mCross = mModel.Crosses().getCrossById(mCrossId);
		} else {
			mCross = null;
		}

		boolean show_conversation = it.getBooleanExtra(FIELD_SHOW_CONVERSATION,
				false);

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
			if (mCross != null) {
				title.setText(mCross.getTitle());
			}
		}

		v = findViewById(R.id.switcher);
		if (v != null) {
			mSwitcher = (ViewSwitcher) v;
			if (show_conversation) {
				if (mSwitcher.getCurrentView().getId() == R.id.x_web_view) {
					mSwitcher.showNext();
				}
			}
		}

		v = findViewById(R.id.x_web_view);
		if (v != null) {
			mWebView = (WebView) v;
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setSavePassword(false);
			webSettings.setSaveFormData(false);
			webSettings.setJavaScriptEnabled(true);
			webSettings.setSupportZoom(false);

			mWebView.setWebChromeClient(new MyWebChromeClient());
			mWebView.setWebViewClient(new MyWebViewClient());

			mWebView.addJavascriptInterface(new ExfeJavaScriptInterface(
					mWebView, this), "exfe");
		}

		v = findViewById(R.id.x_input_content);
		if (v != null) {
			mInput = (EditText) v;
		}

		v = findViewById(R.id.x_add_post);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.x_conversation);
		if (v != null) {
			mConversationListView = (ListView) v;
			mAdapter = new ConversationAdpater(this,
					R.layout.listitem_conversation, new ArrayList<Post>());
			mConversationListView.setAdapter(mAdapter);
		}
		if (mCross != null
				&& mModel.Conversations().hasConversation(
						mCross.getExfee().getId())) {
			showConversation(mModel.Conversations().getFullConversationByExfee(
					mCross.getExfee()));
		} else {
			new FetchCoversationTask(mModel, mCross).execute();
		}
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
			// TODO unfinished logic
			replaceOccurrences(sb, "{#begin_at_human#}", mCross.getTime()
					.getBeginAt().getRelativeStringFromNow(getResources()));
			replaceOccurrences(sb, "{#begin_at#}", mCross.getTime()
					.getLongLocalTimeSring(null, null));
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
				if (inv.getRsvpStatus() == Rsvp.ACCEPTED) {
					confirmed_count++;
				}
				String imgName = inv.getIdentity().getAvatarFilename();
				if (!TextUtils.isEmpty(imgName)) {
					String imgurl = ImageCache.getInst().getImageUrl(imgName);
					String host = "";
					String withnum = "";

					if (inv.isHost()) {
						host = "<span class='rt'>H</span>";
					}
					// with Number
					// if(inv.withnum > 0){
					// withnum=String.format("<span class='lt'>%d</span>",inv.withnum);
					// }

					String exfee_fmt = null;
					if (inv.getRsvpStatus() == Rsvp.ACCEPTED) {
						exfee_fmt = "<li id='avatar_%d'><img alt='' width='40px' height='40px' src='%s' />%s%s</li>";
					} else {
						exfee_fmt = "<li id='avatar_%d' class='opacity'><img alt='' width='40px' height='40px' src='%s' />%s%s</li>";
					}
					exfee.append(String.format(exfee_fmt, inv.getIdentity()
							.getId(), imgurl, host, withnum));
				}

				if (inv.getIdentity().getConnectedUserId() == mModel.Me()
						.getUserId()) {
					mIdentityId = inv.getIdentity().getId();
					int my_rsvp = inv.getRsvpStatus();
					if (my_rsvp == Rsvp.ACCEPTED || my_rsvp == Rsvp.DECLINED
							|| my_rsvp == Rsvp.INTERESTED) {
						replaceOccurrences(sb, "{#rsvp_btn_show#}",
								"style='display:none'");
						switch (my_rsvp) {
						case Rsvp.ACCEPTED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Accept");
							break;
						case Rsvp.DECLINED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Declined");
							break;
						case Rsvp.INTERESTED:
							replaceOccurrences(sb, "{#rsvp_opt_text#}",
									"Interested");
							break;
						}
						replaceOccurrences(sb, "{#rsvp_opt_show#}",
								"style='display:block'");
					} else {
						replaceOccurrences(sb, "{#rsvp_opt_show#}",
								"style='display:none'");
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

		if (mWebView != null) {
			// mWebView.loadData(generatePage("x.html"),
			// "text/html; charset=utf-8",
			// "");
			mWebView.loadDataWithBaseURL("http://exfe.com/",
					generatePage("x.html"), "text/html", "utf-8", null);
		}
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
		mModel.Crosses().updateLastView(mCross);
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
		mWebView.stopLoading();
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
		Bundle bundle = (Bundle) data;
		int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
		switch (type) {
		case ConversationModel.ACTION_TYPE_CLEAR_CONVERSATION:
			showConversation(mModel.Conversations().getFullConversationByExfee(
					mCross.getExfee()));
			break;
		case ConversationModel.ACTION_TYPE_NEW_CONVERSATION:
			if (bundle.getLong("exfee_id") == mCross.getExfee().getId()) {
				showConversation(mModel.Conversations()
						.getFullConversationByExfee(mCross.getExfee()));
			}
		}
	}

	private void showConversation(List<Post> posts) {
		try {
			mAdapter.setNotifyOnChange(false);
			mAdapter.clear();
			if (posts != null) {
				for (Post p : posts) {
					mAdapter.add(p);
				}
			}
		} finally {
			mAdapter.setNotifyOnChange(true);
			mAdapter.notifyDataSetChanged();
		}
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
				if (mSwitcher != null) {
					if (getWindow() != null
							&& getWindow().getCurrentFocus() != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow()
								.getCurrentFocus().getWindowToken(), 0);
					}

					float centerX = mSwitcher.getLeft() + mSwitcher.getWidth()
							/ 2;
					float centerY = mSwitcher.getTop() + mSwitcher.getHeight()
							/ 2;
					float depthZ = mSwitcher.getWidth() / 4.0f;
					Rotate3dAnimation rotationIn = new Rotate3dAnimation(-90,
							0, centerX, centerY, depthZ, false);
					rotationIn.setStartOffset(500);
					rotationIn.setDuration(500);
					// rotationIn.setFillAfter(true);
					rotationIn
							.setInterpolator(new AccelerateDecelerateInterpolator());

					Rotate3dAnimation rotationOut = new Rotate3dAnimation(0,
							90, centerX, centerY, depthZ, true);
					rotationOut.setDuration(500);
					// rotationOut.setFillAfter(true);
					rotationOut
							.setInterpolator(new AccelerateDecelerateInterpolator());

					mSwitcher.setInAnimation(rotationIn);
					mSwitcher.setOutAnimation(rotationOut);

					if (mSwitcher.getCurrentView().getId() == R.id.x_web_view) {
						mSwitcher.showNext();
					} else {
						mSwitcher.showPrevious();
					}
				}
				break;
			case R.id.x_add_post:
				if (mInput != null) {
					String content = mInput.getText().toString();
					Invitation i = null;
					for (Invitation inv : mCross.getExfee().getInvitations()) {
						if (inv.getIdentity().getConnectedUserId() == mModel
								.Me().getUserId()) {
							i = inv;
							break;
						}
					}
					if (i != null) {
						Post p = new Post();
						p.setByIdentitiy(i.getIdentity());
						p.setContent(content);
						p.setVia("Android");
						p.setPostableType("exfee");
						p.setPostableId(mCross.getExfee().getId());
						new PostPost(mModel, mInput).execute(p);
						//mModel.Conversations().addPostToPendingList(p);
						//mInput.getText().clear();
					}
				}
				break;
			default:
				break;
			}
		}
	};

	class PostPost extends AsyncTask<Post, Void, Post> {

		private Model mModel;
		private TextView mInput;

		PostPost(Model model, TextView tv) {
			mModel = model;
			mInput = tv;
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
			mInput.setEnabled(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Post result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mInput.setEnabled(true);
			mInput.getEditableText().clear();
			if (result != null) {
				mModel.Conversations().addPost(result);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mInput.setEnabled(false);
		}

		@Override
		protected Post doInBackground(Post... params) {
			// TODO Auto-generated method stub
			Response result = mModel.getServer().addConversation(params[0]);
			int code = result.getCode();
			@SuppressWarnings("unused")
			int http_category = code % 100;
			switch (code) {
			case HttpStatus.SC_OK:
				JSONObject res = result.getResponse();
				JSONObject json = res.optJSONObject("post");
				if (json != null) {
					Post p = (Post) EntityFactory.create(json);
					return p;
				}
				break;
			}
			return null;
		}

	}

	final class ExfeJavaScriptInterface {
		Context mContext;
		WebView mWebView;

		/** Instantiate the interface and set the context */
		ExfeJavaScriptInterface(WebView w, Context c) {
			mWebView = w;
			mContext = c;
		}

		public void rsvp(final int rvsp, final String call_back) {
			if (mCross == null || mIdentityId == 0) {
				return;
			}

			Exfee exfee = mCross.getExfee();

			Invitation inv = null;
			for (Invitation i : exfee.getInvitations()) {
				if (i.getIdentity().getId() == mIdentityId) {
					inv = i;
					break;
				}
			}
			if (inv == null) {
				Log.w(TAG, "Missing user's invitation or error identity id %d",
						mIdentityId);
				return;
			}

			Invitation inv_temp = inv.cloneSelf();
			inv_temp.setRsvpStatus(rvsp);
			List<Rsvp> rsvps = new ArrayList<Rsvp>();
			Rsvp rsvp = inv_temp.getRsvpObject();
			rsvp.setByIdentity(inv_temp.getIdentity());
			rsvps.add(rsvp);
			// submit the change to server
			Response res = mModel.getServer().udpateRSVP(exfee.getId(), rsvps);
			if (res.getCode() != 200) {
				Log.w(TAG, "Submit fail");
				return;
			}

			// update the local cache
			JSONArray array = res.getResponse().optJSONArray("rsvp");
			if (array == null || array.length() == 0) {
				return;
			}

			for (int i = 0; i < array.length(); i++) {
				try {
					JSONObject json = array.getJSONObject(i);
					Rsvp r = (Rsvp) EntityFactory.create(json);
					r.loadFromDao(mModel.getHelper());
					for (Invitation in : exfee.getInvitations()) {
						if (in.getIdentity().getId() == r.getIdentity().getId()) {
							in.setByIdentity(r.getByIdentity());
							in.setRsvpStatus(r.getRsvpStatus());
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mCross.saveToDao(mModel.getHelper());

			int c_count = 0;
			for (Invitation i : mCross.getExfee().getInvitations()) {
				if (i.getRsvpStatus() == Rsvp.ACCEPTED) {
					c_count++;
				}
			}
			final int confirmed_count = c_count;

			// call callback function to update webview UI.
			mHandler.post(new Runnable() {
				public void run() {
					// handle response in UI thread
					String params = String
							.format("{\"confirmed_num\":%d,\"state_str\":\"%s\",\"state\":%d,\"identity_id\":\"%d\"}",
									confirmed_count,
									Rsvp.getRsvpStatusString(rvsp), rvsp,
									mIdentityId);
					mWebView.loadUrl(String.format("javascript:%s(%s)",
							call_back, params));
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

	private class FetchCoversationTask extends AsyncTask<Long, Void, Response> {

		private Model mModel = null;
		private Cross mCross = null;

		public FetchCoversationTask(Model m, Cross x) {
			mModel = m;
			mCross = x;
		}

		@Override
		protected Response doInBackground(Long... params) {
			return mModel.getServer()
					.getConversation(mCross.getExfee().getId());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Response result) {
			int code = result.getCode();
			@SuppressWarnings("unused")
			int http_category = code % 100;

			switch (code) {
			case HttpStatus.SC_OK:
				List<Post> posts = new ArrayList<Post>();
				JSONObject res = result.getResponse();
				JSONArray array = res.optJSONArray("conversation");
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject json = array.optJSONObject(i);
						if (json != null) {
							Post p = (Post) EntityFactory.create(json);
							posts.add(p);
						}
					}
				}

				mModel.Conversations().addConversation(posts);
				break;
			case HttpStatus.SC_UNAUTHORIZED:
				// relogin
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				// retry
				break;
			default:
				break;
			}

		}

	};

	public static class ConversationAdpater extends ArrayAdapter<Post> {
		public static class ViewHolder {
			ImageView avatar;
			TextView content;
			TextView time;
			ProgressBar wait;
			WeakReference<View> root;
		}

		private int mResource;
		private int mDropDownResource;
		private LayoutInflater mInflater;

		public ConversationAdpater(Context context, int resource,
				List<Post> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			init(context, resource);
		}

		private void init(Context context, int resource) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResource = resource;
			mDropDownResource = resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return createViewFromResource(position, convertView, parent,
					mResource);

			// return super.getView(position, convertView, parent);
		}

		private View createViewFromResource(int position, View convertView,
				ViewGroup parent, int resource) {
			View view;
			ViewHolder holder;
			if (convertView == null) {
				view = mInflater.inflate(resource, parent, false);
				holder = creatViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				Object obj = view.getTag();
				if (obj == null) {
					holder = creatViewHolder(view);
					view.setTag(holder);
				} else {
					holder = (ViewHolder) obj;
				}
			}

			ImageView avatar = holder.avatar;
			TextView content = holder.content;
			TextView time = holder.time;
			ProgressBar wait = holder.wait;
			View root = holder.root.get();

			Post p = getItem(position);

			boolean flag = false;
			if (!TextUtils.isEmpty(p.getByIdentitiy().getAvatarFilename())) {
				Bitmap bm = ImageCache.getInst().getImageFrom(
						p.getByIdentitiy().getAvatarFilename());
				if (bm != null) {
					avatar.setImageBitmap(bm);
					flag = true;
				}
			}
			if (flag == false) {
				avatar.setImageResource(R.drawable.default_avatar);
			}
			Log.d("ConversationAdpater", "Post %d: %s at %s", p.getId(),
					p.getContent(), p.getCreatedAt());

			if (content != null) {
				content.setText(p.getContent());
			}

			if (time != null) {
				if (p.getId() == Post.NO_ID) {
					time.setVisibility(View.GONE);
				} else {
					time.setVisibility(View.VISIBLE);
					if (p.getCreatedAt() != null) {
						// TODO: relative time
						time.setText(Tool.getRelativeStringFromNow(
								p.getCreatedAt(), time.getResources()));
					} else {
						time.setText("");
					}
				}
			}

			if (wait != null) {
				if (p.getId() == Post.NO_ID) {
					wait.setVisibility(View.VISIBLE);
				} else {
					wait.setVisibility(View.GONE);
				}
			}
			return view;
		}

		private ViewHolder creatViewHolder(View view) {
			ViewHolder holder = new ViewHolder();
			holder.avatar = (ImageView) view.findViewById(R.id.post_avatar);
			holder.content = (TextView) view.findViewById(R.id.post_content);
			holder.time = (TextView) view.findViewById(R.id.post_time);
			holder.wait = (ProgressBar) view.findViewById(R.id.post_wait);
			holder.root = new WeakReference<View>(
					view.findViewById(R.id.list_post_root));
			return holder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

	}
}
