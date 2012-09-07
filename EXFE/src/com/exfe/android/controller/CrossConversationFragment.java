package com.exfe.android.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exfe.android.Application;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.ConversationModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.Response;
import com.exfe.android.net.ServerAPI2;
import com.exfe.android.util.ImageCache;
import com.exfe.android.util.Tool;

public class CrossConversationFragment extends ListFragment implements Observer {
	protected final String TAG = getClass().getSimpleName();

	public static final String FIELD_CROSS_ID = "cross_id";

	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
	static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

	static final int LOADER_QUERY_LOCAL = 1;
	static final int LOADER_QUERY_NETWORK = 2;

	protected Model mModel = null;

	private long mCrossId = 0;
	private Cross mCross = null;

	private EditText mInput;
	private Button mSend;
	private View mScrollTopTime;
	
	final Animation mAnimOut = new AlphaAnimation(1.0f, 0.0f);
	{
		mAnimOut.setDuration(500);
	}

	private ConversationAdpater mAdapter = null;

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mModel = ((Application) getActivity().getApplicationContext())
				.getModel();
		mModel.addObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater
	 * , android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cross_conversation,
				container, false);
		View pc = v.findViewById(R.id.container_progress);
		pc.setId(INTERNAL_PROGRESS_CONTAINER_ID);

		View lc = v.findViewById(R.id.container_list);
		lc.setId(INTERNAL_LIST_CONTAINER_ID);

		View ev = v.findViewById(R.id.empty);
		ev.setId(INTERNAL_EMPTY_ID);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mCrossId = getArguments().getLong(FIELD_CROSS_ID, 0);
		if (mCrossId > 0) {
			mCross = mModel.Crosses().getCrossById(mCrossId);
		} else {
			mCross = null;
		}

		setEmptyText("");
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new ConversationAdpater(getActivity(),
				R.layout.listitem_conversation, new ArrayList<Post>());
		setListAdapter(mAdapter);
		// mConversationListView.setOnItemClickListener(itemClickListener);
		// mConversationListView.setOnScrollListener(listScrollerListener);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_QUERY_LOCAL, null,
				queryLoaderHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		View v = null;

		v = view.findViewById(R.id.x_add_post);
		if (v != null) {
			mSend = (Button) v;
			mSend.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.x_input_content);
		if (v != null) {
			mInput = (EditText) v;
			mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEND) {
						mSend.performClick();
						return true;
					}
					return false;
				}
			});
		}

		v = view.findViewById(R.id.hover_scroll_time_layer);
		if (v != null) {
			mScrollTopTime = v;
		}

		mAdapter = new ConversationAdpater(this.getActivity(),
				R.layout.listitem_conversation, new ArrayList<Post>());
		setListAdapter(mAdapter);
		getListView().setOnScrollListener(listScrollerListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mModel.deleteObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView
	 * , android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {

		if (v == null) {
			return;
		}
		View last_time_layer = (View) lv.getTag();
		if (last_time_layer != null) {
			last_time_layer.setTag(null);
			last_time_layer.setVisibility(View.INVISIBLE);
			lv.setTag(null);
		}

		final View time_layer = v.findViewById(R.id.post_time_layer);
		if (time_layer != null) {
			time_layer.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					View vv = v.findViewById(R.id.post_relative_time);
					vv.setVisibility(vv.getVisibility() == View.VISIBLE ? View.GONE
							: View.VISIBLE);
					vv = v.findViewById(R.id.post_abs_date);
					vv.setVisibility(vv.getVisibility() == View.VISIBLE ? View.GONE
							: View.VISIBLE);
					vv = v.findViewById(R.id.post_abs_time);
					vv.setVisibility(vv.getVisibility() == View.VISIBLE ? View.GONE
							: View.VISIBLE);
				}
			});
			lv.setTag(time_layer);
			showForSeconds(time_layer, 3);
		}
	}

	AbsListView.OnScrollListener listScrollerListener = new AbsListView.OnScrollListener() {

		int lastIndex = -1;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			if (mScrollTopTime != null) {
				if (visibleItemCount > 0 && firstVisibleItem != lastIndex) {
					lastIndex = firstVisibleItem;
					Post post = (Post) view.getAdapter().getItem(
							firstVisibleItem);

					if (post != null && post.getCreatedAt() != null) {
						// fill mTimeLayer;
						TextView rel = (TextView) mScrollTopTime
								.findViewById(R.id.hover_scroll_relative_time);
						TextView time = (TextView) mScrollTopTime
								.findViewById(R.id.hover_scroll_abs_time);

						if (Tool.isInSame(Calendar.YEAR, post.getCreatedAt())) {
							rel.setText(Tool.getXRelativeString(
									post.getCreatedAt(), getResources()));
							time.setText(Const.LOCAL_TIME_DATE_FORMAT
									.format(post.getCreatedAt()));
						} else {
							rel.setText(Const.LOCAL_TIME_FORMAT.format(post
									.getCreatedAt()));
							time.setText(Const.LOCAL_DATE_FORMAT.format(post
									.getCreatedAt()));
						}
						mScrollTopTime.requestLayout();
					}
				}

			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			Log.d(TAG, "scroll: %d", scrollState);
			if (scrollState != SCROLL_STATE_IDLE) {
				showForSeconds(mScrollTopTime, 1);
			}
		}
	};

	LoaderManager.LoaderCallbacks<List<Post>> queryLoaderHandler = new LoaderManager.LoaderCallbacks<List<Post>>() {

		@Override
		public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case LOADER_QUERY_LOCAL:
				return new ConversationLoader(getActivity(), mModel, mCross,
						args);
			case LOADER_QUERY_NETWORK:
				return new ConversationLoader(getActivity(), mModel, mCross,
						args);
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {

			mModel.Conversations().addConversation(data);

			// Set the new data in the adapter.
			showConversation(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<List<Post>> loader) {
			// TODO Auto-generated method stub
			mAdapter.clear();
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
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

	protected void showConversation(List<Post> posts) {
		try {
			mAdapter.setNotifyOnChange(false);
			mAdapter.clear();
			if (posts != null) {
				Collections.sort(posts, Post.sCreateTimeComparator);
				for (Post p : posts) {
					mAdapter.add(p);
				}
			}
		} finally {
			mAdapter.setNotifyOnChange(true);
			mAdapter.notifyDataSetChanged();
		}
	}

	protected void showForSeconds(final View v, final int seconds) {
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			Object tag = v.getTag(R.id.field_time_out);
			Date until = new Date(System.currentTimeMillis() + seconds * 1000);
			v.setTag(R.id.field_time_out, until);
			if (tag == null) {
				v.postDelayed(new Runnable() {

					@Override
					public void run() {
						Object obj = v.getTag(R.id.field_time_out);
						if (obj != null) {
							Date until = (Date) obj;
							if (until.getTime() >= System.currentTimeMillis()) {
								// wait again
								v.postDelayed(this,
										(long) (seconds * 1000 * 0.2));
								return;
							}
						}
						
						if (v.getVisibility() == View.VISIBLE) {
							v.startAnimation(mAnimOut);
							v.setVisibility(View.INVISIBLE);
							v.setTag(R.id.field_time_out, null);
						}
					}
				}, (long) (seconds * 1000 * 0.8));
			}
		}
	}

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
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
						new PostPost(mModel, mInput).execute(p); // mModel.Conversations().addPostToPendingList(p);
																	// //
						mInput.getText().clear();
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
			super.onPreExecute();
			mInput.setEnabled(false);
		}

		@Override
		protected Post doInBackground(Post... params) {
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

	public static class ConversationAdpater extends ArrayAdapter<Post> {
		public static class ViewHolder {
			ImageView avatar;
			TextView content;
			TextView time;
			TextView date;
			TextView rel_time;
			ViewGroup time_layer;
			ProgressBar wait;
			WeakReference<View> root;
		}

		private int mResource;
		@SuppressWarnings("unused")
		private int mDropDownResource;
		private LayoutInflater mInflater;

		public ConversationAdpater(Context context, int resource,
				List<Post> objects) {
			super(context, resource, objects);
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
			return createViewFromResource(position, convertView, parent,
					mResource);
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
			TextView date = holder.date;
			ProgressBar wait = holder.wait;
			ViewGroup layer = holder.time_layer;
			TextView rel_time = holder.rel_time;
			@SuppressWarnings("unused")
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
				String poster = p.getByIdentitiy().getName();
				String message = p.getContent();
				Spannable sp = new SpannableString(String.format("%s %s",
						poster, message));
				sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
						poster.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				content.setText(sp);
			}

			if (time != null) {
				if (p.getId() == Post.NO_ID) {
					layer.setVisibility(View.INVISIBLE);
				} else {
					// layer.setVisibility(View.VISIBLE);
					if (p.getCreatedAt() != null) {
						time.setText(Const.LOCAL_TIME_FORMAT.format(p
								.getCreatedAt()));
						date.setText(Const.LOCAL_DATE_FORMAT.format(p
								.getCreatedAt()));
						rel_time.setText(Tool.getXRelativeString(
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
			holder.time = (TextView) view.findViewById(R.id.post_abs_time);
			holder.date = (TextView) view.findViewById(R.id.post_abs_date);
			holder.rel_time = (TextView) view
					.findViewById(R.id.post_relative_time);
			holder.time_layer = (ViewGroup) view
					.findViewById(R.id.post_time_layer);
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

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res
					.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE
							| ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class ConversationLoader extends AsyncTaskLoader<List<Post>> {
		final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		final Bundle mParam;
		final private Model mModel;
		final private Cross mCross;

		List<Post> mApps;

		public ConversationLoader(Context context, Model model, Cross cross,
				Bundle bundle) {
			super(context);
			mModel = model;
			mCross = cross;
			mParam = bundle;
			setUpdateThrottle(300);
		}

		/**
		 * This is where the bulk of our work is done. This function is called
		 * in a background thread and should generate a new set of data to be
		 * published by the loader.
		 */
		@Override
		public List<Post> loadInBackground() {
			Exfee exfee = mCross.getExfee();
			ServerAPI2 server = mModel.getServer();

			Response result = server.getConversation(exfee);
			// Response result =
			// mModel.getServer().getConversation(exfee.getId());

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
				// Sort the list.
				// Collections.sort(entries, ALPHA_COMPARATOR);

				return posts;
				// break;
			case HttpStatus.SC_UNAUTHORIZED:
				// relogin
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				// retry
				break;
			default:
				break;
			}

			return null;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(List<Post> posts) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (posts != null) {
					onReleaseResources(posts);
				}
			}

			List<Post> oldApps = posts;
			mApps = posts;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(posts);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			boolean configChange = mLastConfig.applyNewConfig(getContext()
					.getResources());

			if (takeContentChanged() || mApps == null || configChange) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

		/**
		 * Handles a request to stop the Loader.
		 */
		@Override
		protected void onStopLoading() {
			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		/**
		 * Handles a request to cancel a load.
		 */
		@Override
		public void onCanceled(List<Post> apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			onReleaseResources(apps);
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mApps != null) {
				onReleaseResources(mApps);
				mApps = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<Post> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}
	}
}
