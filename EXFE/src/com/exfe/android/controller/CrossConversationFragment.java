package com.exfe.android.controller;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Application;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.ConversationModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.Response;
import com.exfe.android.util.InterestingConfigChanges;
import com.exfe.android.util.Tool;
import com.exfe.android.view.IteratorAdapter;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;

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

	private ConversationIteratorAdpater mAdapter = null;

	private ImageWorker mImageWorker = null;

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
		super.onCreate(savedInstanceState);
		mModel = ((Application) getActivity().getApplicationContext())
				.getModel();
		mModel.addObserver(this);

		mImageWorker = new ImageFetcher(mModel.getAppContext(), getResources()
				.getDimensionPixelSize(R.dimen.avatar_width), getResources()
				.getDimensionPixelSize(R.dimen.avatar_height));
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
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
		mAdapter = new ConversationIteratorAdpater(getActivity(),
				R.layout.listitem_conversation, null);

		setListAdapter(mAdapter);
		getListView().setOnScrollListener(listScrollerListener);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_QUERY_LOCAL, null,
				mQueryLoaderHandler);
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
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (mCross != null) {
			mModel.Conversations().refreshPosts(mCross);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
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

		int line = 0;
		View tv = v.findViewById(R.id.post_content);
		if (tv != null) {
			TextView content = (TextView) tv;
			line = content.getLineCount();
		}

		final View time_layer = v.findViewById(R.id.post_time_layer);
		if (time_layer != null) {
			FrameLayout.LayoutParams params = (LayoutParams) time_layer
					.getLayoutParams();
			if (line == 1) {
				params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
				params.topMargin = 0;
			} else {
				params.gravity = Gravity.TOP | Gravity.RIGHT;
				params.topMargin = (int) (12 * time_layer.getResources()
						.getDisplayMetrics().density);
			}
			v.findViewById(R.id.post_relative_time).setVisibility(View.VISIBLE);
			v.findViewById(R.id.post_abs_date).setVisibility(View.GONE);
			v.findViewById(R.id.post_abs_time).setVisibility(View.GONE);

			if (!time_layer.isClickable()) {

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
			}
			lv.setTag(time_layer);
			showOneForSeconds(lv, 3);
		}
	}

	AbsListView.OnScrollListener listScrollerListener = new AbsListView.OnScrollListener() {

		int lastIndex = -1;
		boolean allShown = true;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			if (mScrollTopTime != null) {
				if (visibleItemCount > 0 && firstVisibleItem != lastIndex) {
					allShown = visibleItemCount == totalItemCount;
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
							time.setText(Const.LOCAL_TIME_DATE_MMM_DD_FORMAT
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
			if (scrollState != SCROLL_STATE_IDLE && !allShown) {
				showForSeconds(mScrollTopTime, 1);
			}
		}
	};

	LoaderManager.LoaderCallbacks<CloseableIterator<Post>> mQueryLoaderHandler = new LoaderManager.LoaderCallbacks<CloseableIterator<Post>>() {

		@Override
		public Loader<CloseableIterator<Post>> onCreateLoader(int id,
				Bundle args) {
			switch (id) {
			case LOADER_QUERY_LOCAL:
				QueryBuilder<Post, Long> qb = getModel().getHelper()
						.getPostDao().queryBuilder();
				try {
					qb.orderBy("created_at", true);
					qb.where()
							.eq(Post.POSTABLE_TYPE_FIELD_NAME,
									Post.POSTABLE_TYPE_EXFEE)
							.and()
							.eq(Post.POSTABLE_ID_FIELD_NAME,
									mCross.getExfee().getId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return new OrmLiteCursorLoader(getActivity(), getModel(), qb,
						null);
			default:
				return null;
			}
		}

		@Override
		public void onLoadFinished(Loader<CloseableIterator<Post>> loader,
				CloseableIterator<Post> data) {
			mAdapter.changeIterator(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<CloseableIterator<Post>> loader) {
			mAdapter.changeIterator(null);
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void update(Observable observable, Object data) {
		final Bundle bundle = (Bundle) data;
		int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
		switch (type) {
		case ConversationModel.ACTION_TYPE_CLEAR_CONVERSATION:
			mModel.mHandler.post(new Runnable() {

				@Override
				public void run() {
					getLoaderManager().restartLoader(LOADER_QUERY_LOCAL, null,
							mQueryLoaderHandler);
				}
			});

			break;
		case ConversationModel.ACTION_TYPE_NEW_CONVERSATION:
			if (bundle.getLong("exfee_id") == mCross.getExfee().getId()) {
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						getLoaderManager().restartLoader(LOADER_QUERY_LOCAL,
								null, mQueryLoaderHandler);

					}
				});
			}
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

	protected void showOneForSeconds(final View storage, final int seconds) {
		if (storage == null) {
			return;
		}
		View v = (View) storage.getTag();
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			Object tag = storage.getTag(R.id.field_time_out);
			Date until = new Date(System.currentTimeMillis() + seconds * 1000);
			storage.setTag(R.id.field_time_out, until);
			if (tag == null) {
				v.postDelayed(new Runnable() {

					@Override
					public void run() {
						Object obj = storage.getTag(R.id.field_time_out);
						if (obj != null) {
							Date until = (Date) obj;
							if (until.getTime() >= System.currentTimeMillis()) {
								// wait again
								View v = (View) storage.getTag();
								if (v != null) {
									v.postDelayed(this,
											(long) (seconds * 1000 * 0.2));
								}
								return;
							}
						}

						View v = (View) storage.getTag();
						if (v != null && v.getVisibility() == View.VISIBLE) {
							v.startAnimation(mAnimOut);
							v.setVisibility(View.INVISIBLE);
							storage.setTag(R.id.field_time_out, null);
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
						postPost(p); // mModel.Conversations().addPostToPendingList(p);
						mInput.getText().clear();
					}
				}
				break;
			default:
				break;
			}
		}
	};

	void postPost(final Post post) {

		Runnable run = new Runnable() {

			@Override
			public void run() {
				Response result = mModel.getServer().addConversation(post);
				int code = result.getCode();
				@SuppressWarnings("unused")
				int http_category = code % 100;
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						mInput.setEnabled(true);
					}
				});

				switch (code) {
				case HttpStatus.SC_OK:
					mModel.mHandler.post(new Runnable() {

						@Override
						public void run() {
							mInput.getEditableText().clear();
						}
					});

					JSONObject res = result.getResponse();
					JSONObject json = res.optJSONObject("post");
					if (json != null) {
						final Post p = (Post) EntityFactory.create(json);
						if (p != null) {
							mModel.Conversations().addPost(p);
						}
					}
					break;
				}
			}

		};

		Thread th = new Thread(run);
		mInput.setEnabled(false);
		th.start();
	}

	public class ConversationIteratorAdpater extends IteratorAdapter<Post> {
		private int mResource;
		@SuppressWarnings("unused")
		private int mDropDownResource;
		private LayoutInflater mInflater;
		private Model mModel;

		public ConversationIteratorAdpater(Context context, int resource,
				CloseableIterator<Post> iterator) {
			super(context, iterator, true);
			init(context, resource);
		}

		public ConversationIteratorAdpater(Context context, int resource,
				CloseableIterator<Post> iterator, boolean autoRequery) {
			super(context, iterator, autoRequery);
			init(context, resource);
		}

		private void init(Context context, int resource) {
			mModel = ((Application) context.getApplicationContext()).getModel();
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResource = resource;
			mDropDownResource = resource;
		}

		@Override
		protected long getCursorId() {
			try {
				Post p = mIterator.current();
				return p.getId();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		public View newView(Context context, CloseableIterator<Post> cursor,
				ViewGroup parent) {
			View view = mInflater.inflate(mResource, parent, false);
			SparseArray<View> holder = creatViewHolder(view);
			view.setTag(holder);
			return view;
		}

		@Override
		public void bindView(View view, Context context,
				CloseableIterator<Post> iterator) {
			SparseArray<View> holder;
			Object obj = view.getTag();
			if (obj == null) {
				holder = creatViewHolder(view);
				view.setTag(holder);
			} else {
				holder = (SparseArray<View>) obj;
			}

			ImageView avatar = (ImageView) holder.get(R.id.post_avatar);
			TextView content = (TextView) holder.get(R.id.post_content);
			TextView time = (TextView) holder.get(R.id.post_abs_time);
			TextView date = (TextView) holder.get(R.id.post_abs_date);
			ProgressBar wait = (ProgressBar) holder.get(R.id.post_wait);
			ViewGroup layer = (ViewGroup) holder.get(R.id.post_time_layer);
			TextView rel_time = (TextView) holder.get(R.id.post_relative_time);
			@SuppressWarnings("unused")
			View root = holder.get(R.id.list_post_root);

			Post p;
			try {
				p = iterator.current();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			p.loadFromDao(mModel.getHelper());

			boolean flag = false;
			if (!TextUtils.isEmpty(p.getByIdentitiy().getAvatarFilename())) {
				mImageWorker.loadImage(p.getByIdentitiy().getAvatarFilename(),
						avatar);
				flag = true;
			}
			if (flag == false) {
				avatar.setImageResource(R.drawable.default_avatar);
			}
			Log.d("ConversationAdpater", "Post %1$d: %2$s at %3$s", p.getId(),
					p.getContent(),
					Const.LOCAL_FULL_FORMAT.format(p.getCreatedAt()));

			if (content != null) {
				String poster = p.getByIdentitiy().getName();
				String message = p.getContent();
				Spannable sp = new SpannableString(String.format("%1$s %2$s",
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
		}

		private SparseArray<View> creatViewHolder(View view) {
			SparseArray<View> holder = new SparseArray<View>();
			holder.put(R.id.post_avatar, view.findViewById(R.id.post_avatar));
			holder.put(R.id.post_content, view.findViewById(R.id.post_content));
			holder.put(R.id.post_abs_time,
					view.findViewById(R.id.post_abs_time));
			holder.put(R.id.post_abs_date,
					view.findViewById(R.id.post_abs_date));
			holder.put(R.id.post_relative_time,
					view.findViewById(R.id.post_relative_time));
			holder.put(R.id.post_time_layer,
					view.findViewById(R.id.post_time_layer));
			holder.put(R.id.post_wait, view.findViewById(R.id.post_wait));
			holder.put(R.id.list_post_root,
					view.findViewById(R.id.list_post_root));
			return holder;
		}

	}

	public static class OrmLiteCursorLoader extends
			AsyncTaskLoader<CloseableIterator<Post>> {

		final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		final Bundle mParam;
		final private Model mModel;
		QueryBuilder<Post, Long> mQb;
		CloseableIterator<Post> mIterator;

		public OrmLiteCursorLoader(Context context, Model model,
				QueryBuilder<Post, Long> qb, Bundle bundle) {
			super(context);
			mModel = model;
			mParam = bundle;
			mQb = qb;
			setUpdateThrottle(500);
		}

		@Override
		public CloseableIterator<Post> loadInBackground() {
			CloseableIterator<Post> iterator = null;
			try {
				// when you are done, prepare your query and build an iterator
				iterator = mModel.getHelper().getPostDao()
						.iterator(mQb.prepare());

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

			}
			return iterator;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(CloseableIterator<Post> iterator) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (iterator != null) {
					onReleaseResources(iterator);
				}
			}

			CloseableIterator<Post> oldIterator = mIterator;
			mIterator = iterator;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(iterator);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldIterator != null && oldIterator != iterator) {
				onReleaseResources(oldIterator);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mIterator != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mIterator);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			boolean configChange = mLastConfig.applyNewConfig(getContext()
					.getResources());

			if (takeContentChanged() || mIterator == null || configChange) {
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
		public void onCanceled(CloseableIterator<Post> cursor) {
			super.onCanceled(cursor);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			onReleaseResources(cursor);
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
			if (mIterator != null) {
				onReleaseResources(mIterator);
				mIterator = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(CloseableIterator<?> iterator) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
			try {
				if (iterator != null) {
					iterator.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
