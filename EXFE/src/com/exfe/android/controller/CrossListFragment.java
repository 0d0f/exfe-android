package com.exfe.android.controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Activity;
import com.exfe.android.Application;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.CrossesModel;
import com.exfe.android.model.MeModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.User;
import com.exfe.android.util.InterestingConfigChanges;
import com.exfe.android.view.DoubleTextView;
import com.exfe.android.view.IteratorAdapter;
import com.exfe.android.view.SeperateTextView;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;

public class CrossListFragment extends ListFragment implements Observer {
	protected final String TAG = getClass().getSimpleName();

	static final int LOADER_QUERY_LOCAL = 1;

	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
	static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

	protected Model mModel = null;

	private IteratorAdapter<Cross> mAdapter = null;
	private ImageView mUserAvatar = null;
	private TextView mExfeeTitle = null;
	private View mProgressBarHint = null;
	private ImageWorker mImageWorker = null;

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getActivity().getApplicationContext())
				.getModel();
		mModel.addObserver(this);

		setHasOptionsMenu(true);
		mImageWorker = new ImageFetcher(mModel.getAppContext(), getResources()
				.getDimensionPixelSize(R.dimen.small_avatar_width),
				getResources().getDimensionPixelSize(
						R.dimen.small_avatar_height));
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
		// mImageWorker.setLoadingImage(resId);

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
		View v = inflater.inflate(R.layout.fragment_cross_list, container,
				false);

		View pc = v.findViewById(R.id.container_progress);
		pc.setId(INTERNAL_PROGRESS_CONTAINER_ID);

		View lc = v.findViewById(R.id.container_list);
		lc.setId(INTERNAL_LIST_CONTAINER_ID);

		View ev = v.findViewById(R.id.empty);
		ev.setId(INTERNAL_EMPTY_ID);

		return v;

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

		v = view.findViewById(R.id.user_avatar);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}
		v = view.findViewById(R.id.user_name_and_title);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.pb_network_hint);
		if (v != null) {
			mProgressBarHint = v;
		}

		v = view.findViewById(R.id.btn_refresh);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.btn_clear);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.user_avatar);
		if (v != null) {
			mUserAvatar = (ImageView) v;
		}

		v = view.findViewById(R.id.user_name_and_title);
		if (v != null) {
			mExfeeTitle = (TextView) v;
		}

		loadHead(mModel.Me().getProfile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		setEmptyText("");
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		View emptyView = getActivity().getLayoutInflater().inflate(
				R.layout.listitem_cross_empty, getListView(), false);
		getListView().addFooterView(emptyView, null, false);
		mAdapter = new CrossIteratorAdapter(getActivity(),
				R.layout.listitem_cross, null);
		setListAdapter(mAdapter);
		getListView().setOnScrollListener(listScrollerListener);
		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_QUERY_LOCAL, null,
				mQueryLoaderHandler);
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

		Date last = mModel.Crosses().getLastUpdateQuery();
		Date now = new Date();
		if (last == null || now.getTime() - last.getTime() > 15 * 3600 * 1000) {
			mModel.Crosses().freshCrosses();
		}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.cross, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.btn_clear:
			Log.v(TAG, "%s is clicked", "clear");
			mModel.Crosses().clearCrosses();
			mModel.Crosses().setLastUpdateQuery(null);// new Date(112, 4,1,
														// 0,0,0)
			getLoaderManager().restartLoader(LOADER_QUERY_LOCAL, null,
					mQueryLoaderHandler);
			return true;
			// break;
		case R.id.btn_refresh:
			Log.v(TAG, "%s is clicked", "Refresh");
			mModel.Crosses().freshCrosses();
			return true;
			// break;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void update(Observable observable, Object data) {

		if (observable instanceof Model) {

			Bundle bundle = (Bundle) data;
			int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
			switch (type) {
			case CrossesModel.ACTION_TYPE_UPDATE_CROSSES:
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						getLoaderManager().restartLoader(LOADER_QUERY_LOCAL,
								null, mQueryLoaderHandler);
					}
				});
				break;
			case MeModel.ACTION_TYPE_UPDATE_MY_PROFILE:
				final User u = mModel.Me().getProfile();
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						loadHead(u);
					}
				});
				break;
			case Model.ACTION_TYPE_SIGN_OUT:
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(
								"The token is invalid. Do you want to sign out?")
								.setPositiveButton(R.string.sign_out,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
												((Activity) getActivity())
														.signOut();
											}
										})
								.setNegativeButton(R.string.cancel,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();
											}
										});
						builder.create().show();
					}
				});

				break;

			case Model.ACTION_TYPE_START_NETWROK_QUERY:
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (mProgressBarHint != null) {
							mProgressBarHint.setVisibility(View.VISIBLE);
						}
					}
				});
				break;
			case Model.ACTION_TYPE_STOP_NETWROK_QUERY:
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (mProgressBarHint != null) {
							mProgressBarHint.setVisibility(View.GONE);
						}
					}
				});
				break;
			}
		}
	}

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Intent it = null;
			switch (id) {
			case R.id.user_avatar:
			case R.id.user_name_and_title:
				it = new Intent();
				it.setClass(getActivity(), ProfileActivity.class);
				startActivity(it);
				break;
			}
		}
	};

	AbsListView.OnScrollListener listScrollerListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			switch (scrollState) {
			case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
				if (view.getFirstVisiblePosition() == 0) {
					Log.d("ListView", "refresh new");
					mModel.Crosses().freshCrosses();
				}
				if (view.getLastVisiblePosition() == view.getAdapter()
						.getCount() - 1) {
					Log.d("ListView", "refresh more");
				}
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				break;
			}
		}
	};

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Intent it = new Intent();
		it.putExtra(CrossDetailActivity.FIELD_CROSS_ID, id);
		it.setClass(getActivity(), CrossDetailActivity.class);
		CrossListFragment.this.startActivity(it);
	}

	private void loadHead(User user) {
		if (user != null) {
			if (mUserAvatar != null) {
				String avatar_file_name = user.getAvatarFilename();
				mUserAvatar.setVisibility(View.VISIBLE);
				mImageWorker.loadImage(avatar_file_name, mUserAvatar);
			}

			if (mExfeeTitle != null) {
				mExfeeTitle.setText(user.getName());
			}
		}
	}

	LoaderManager.LoaderCallbacks<CloseableIterator<Cross>> mQueryLoaderHandler = new LoaderManager.LoaderCallbacks<CloseableIterator<Cross>>() {

		@Override
		public Loader<CloseableIterator<Cross>> onCreateLoader(int id,
				Bundle args) {
			switch (id) {
			case LOADER_QUERY_LOCAL:
				QueryBuilder<Cross, Long> qb = getModel().getHelper()
						.getCrossDao().queryBuilder();
				qb.orderBy("updated_at", false);
				return new OrmLiteCursorLoader(getActivity(), getModel(), qb,
						null);
			default:
				return null;
			}
		}

		@Override
		public void onLoadFinished(Loader<CloseableIterator<Cross>> loader,
				CloseableIterator<Cross> data) {
			mAdapter.changeIterator(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<CloseableIterator<Cross>> loader) {
			mAdapter.changeIterator(null);
		}
	};

	public static class OrmLiteCursorLoader extends
			AsyncTaskLoader<CloseableIterator<Cross>> {

		final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		final Bundle mParam;
		final private Model mModel;
		QueryBuilder<Cross, Long> mQb;
		CloseableIterator<Cross> mIterator;

		public OrmLiteCursorLoader(Context context, Model model,
				QueryBuilder<Cross, Long> qb, Bundle bundle) {
			super(context);
			mModel = model;
			mParam = bundle;
			mQb = qb;
			setUpdateThrottle(500);
		}

		@Override
		public CloseableIterator<Cross> loadInBackground() {
			CloseableIterator<Cross> iterator = null;
			try {
				// when you are done, prepare your query and build an iterator
				iterator = mModel.getHelper().getCrossDao()
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
		public void deliverResult(CloseableIterator<Cross> iterator) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (iterator != null) {
					onReleaseResources(iterator);
				}
			}

			CloseableIterator<Cross> oldApps = mIterator;
			mIterator = iterator;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(iterator);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null && oldApps != iterator) {
				onReleaseResources(oldApps);
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
		public void onCanceled(CloseableIterator<Cross> cursor) {
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

	public class CrossIteratorAdapter extends IteratorAdapter<Cross> {

		private int mResource;
		@SuppressWarnings("unused")
		private int mDropDownResource;
		private LayoutInflater mInflater;
		private Model mModel;

		public CrossIteratorAdapter(Context context, int resource,
				CloseableIterator<Cross> iterator) {
			super(context, iterator, true);
			init(context, resource);
		}

		public CrossIteratorAdapter(Context context, int resource,
				CloseableIterator<Cross> iterator, boolean autoRequery) {
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
				Cross cross = mIterator.current();
				return cross.getId();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		public View newView(Context context, CloseableIterator<Cross> cursor,
				ViewGroup parent) {
			View view = mInflater.inflate(mResource, parent, false);
			SparseArray<View> holder = creatViewHolder(view);
			view.setTag(holder);
			return view;
		}

		@Override
		public void bindView(View view, Context context,
				CloseableIterator<Cross> iterator) {
			SparseArray<View> holder;
			Object obj = view.getTag();
			if (obj == null) {
				holder = creatViewHolder(view);
				view.setTag(holder);
			} else {
				holder = (SparseArray<View>) obj;
			}

			CheckedTextView title = (CheckedTextView) holder
					.get(R.id.cross_title);
			SeperateTextView exfee = (SeperateTextView) holder
					.get(R.id.cross_exfee);
			CheckedTextView time = (CheckedTextView) holder
					.get(R.id.cross_time);
			DoubleTextView time_mmmdd = (DoubleTextView) holder
					.get(R.id.cross_time_mmmdd);
			CheckedTextView place = (CheckedTextView) holder
					.get(R.id.cross_place);
			TextView count = (TextView) holder.get(R.id.cross_message_count);
			@SuppressWarnings("unused")
			View root = holder.get(R.id.list_cross_root);

			Cross x;
			try {
				x = iterator.current();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			x.loadFromDao(mModel.getHelper());

			String diff = x.diffByUpdate();

			if (title != null) {
				title.setText(x.getTitle());
				title.setChecked(diff.contains("t"));
			}
			if (exfee != null) {
				exfee.setText(String.valueOf(x.getExfee().getAccepted()));
				exfee.setAltText(String.valueOf(x.getExfee().getTotal()));
			}
			if (place != null) {
				if (x.getPlace() != null) {
					place.setText(x.getPlace().getTitle());
				}
				place.setChecked(diff.contains("p"));
			}
			if (time != null) {
				if (x.getTime() != null) {
					time.setText(x.getTime().getLongLocalTimeSring(
							time.getResources()));
				} else {
					time.setText(null);
				}
				time.setChecked(diff.contains("m"));
			}

			boolean no_time = true;
			if (x.getTime() != null && x.getTime().getBeginAt() != null) {
				String date = x.getTime().getBeginAt().getDate();
				if (!TextUtils.isEmpty(date)) {
					try {
						Date d = Const.UTC_DATE_FORMAT.parse(date);
						if (time_mmmdd != null) {
							time_mmmdd.setText(Const.UTC_DAY_FORMAT.format(d));
							time_mmmdd.setAltText(Const.UTC_MONTH_FORMAT
									.format(d));
							time_mmmdd.getBackground().setLevel(0);
						}
						no_time = false;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (no_time) {
				if (time_mmmdd != null) {
					time_mmmdd.setText("");
					time_mmmdd.setAltText("");
					time_mmmdd.getBackground().setLevel(1);
				}
			}

			if (count != null) {
				int c = x.getConversationCount();
				if (c == 0) {
					count.setVisibility(View.INVISIBLE);
					count.setText(String.valueOf(c));
				} else {
					count.setVisibility(View.VISIBLE);
					if (c < 30) {
						count.setText(String.valueOf(c));
					} else {
						count.setText("");
					}
					count.getBackground().setLevel(c);
				}
			}
		}

		private SparseArray<View> creatViewHolder(View view) {
			SparseArray<View> holder = new SparseArray<View>();
			holder.put(R.id.cross_title, view.findViewById(R.id.cross_title));
			holder.put(R.id.cross_exfee, view.findViewById(R.id.cross_exfee));
			holder.put(R.id.cross_time, view.findViewById(R.id.cross_time));
			holder.put(R.id.cross_time_mmmdd,
					view.findViewById(R.id.cross_time_mmmdd));
			holder.put(R.id.cross_place, view.findViewById(R.id.cross_place));
			holder.put(R.id.cross_message_count,
					view.findViewById(R.id.cross_message_count));
			holder.put(R.id.list_cross_root,
					view.findViewById(R.id.list_cross_root));
			return holder;
		}

	}

}