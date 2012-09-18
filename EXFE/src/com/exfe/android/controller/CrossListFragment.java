package com.exfe.android.controller;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Application;
import com.exfe.android.Const;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.CrossesModel;
import com.exfe.android.model.MeModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.util.ImageCache;
import com.exfe.android.view.DoubleTextView;
import com.exfe.android.view.SeperateTextView;
import com.exfe.android.view.TwoLineTextView;

public class CrossListFragment extends Fragment implements Observer {

	private ArrayAdapter<Cross> mAdapter = null;
	private ImageView mUserAvatar = null;
	private TextView mExfeeTitle = null;
	private ImageWorker mImageWorker = null;

	private FetchCrossesTask mTask = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		v = view.findViewById(R.id.list_crosses);
		if (v != null) {
			ListView listView = (ListView) v;
			mAdapter = new CrossAdpater(getActivity(), R.layout.listitem_cross,
					new ArrayList<Cross>());
			listView.setAdapter(mAdapter);
			listView.setOnItemClickListener(mItemClickListener);
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					// TODO Auto-generated method stub
					switch (scrollState) {
					case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
						break;
					case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
						if (view.getFirstVisiblePosition() == 0) {
							Log.d("ListView", "refresh new");
							freshCrosses();
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
			});
		}

		setCrosses(mModel.Crosses().getCrosses());
		loadHead(mModel.Me().getProfile());
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
			freshCrosses();
		}
	}

	public void freshCrosses() {
		if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
			mTask = new FetchCrossesTask();
			mTask.execute();
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
			ImageCache.getInst().clearCachedFiles();
			mAdapter.clear();
			return true;
			// break;
		case R.id.btn_refresh:
			Log.v(TAG, "%s is clicked", "Refresh");
			freshCrosses();
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

		if (observable instanceof ImageCache) {

		} else if (observable instanceof Model) {

			Bundle bundle = (Bundle) data;
			int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
			switch (type) {
			case CrossesModel.ACTION_TYPE_UPDATE_CROSSES:
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						setCrosses(mModel.Crosses().getCrosses());
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

	AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adpaterView, View v, int pos,
				long id) {
			Intent it = new Intent();
			it.putExtra(CrossDetailActivity.FIELD_CROSS_ID, id);
			it.setClass(getActivity(), CrossDetailActivity.class);
			CrossListFragment.this.startActivity(it);
		}

	};

	private void setCrosses(Collection<Cross> collection) {
		if (collection == null || collection.size() == 0) {
			return;
		}
		Log.d(TAG, "%d crosses is replaced.", collection.size());
		mAdapter.setNotifyOnChange(false);
		mAdapter.clear();
		for (Cross x : collection) {
			mAdapter.add(x);
		}
		mAdapter.sort(Entity.sIdComparator);
		mAdapter.setNotifyOnChange(true);
		mAdapter.notifyDataSetChanged();
	}

	private void loadHead(User user) {
		if (user != null) {
			if (mUserAvatar != null) {
				String avatar_file_name = user.getAvatarFilename();
				boolean flag = false;
				/*
				 * if (!TextUtils.isEmpty(avatar_file_name)) { Bitmap bm =
				 * ImageCache.getInst().getImageFrom( avatar_file_name); if (bm
				 * != null) { mUserAvatar.setImageBitmap(bm);
				 * mUserAvatar.setVisibility(View.VISIBLE); flag = true; } } if
				 * (flag == false) {
				 * //mUserAvatar.setImageResource(R.drawable.default_avatar);
				 * mUserAvatar.setVisibility(View.GONE); }
				 */
				mUserAvatar.setVisibility(View.VISIBLE);
				mImageWorker.loadImage(avatar_file_name, mUserAvatar);
			}

			if (mExfeeTitle != null) {
				mExfeeTitle.setText(user.getName());
			}
		}
	}

	class FetchCrossesTask extends AsyncTask<Integer, Integer, Response> {

		private Date mLastQueryTime = null;
		private Date mThisQueryTime = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLastQueryTime = mModel.Crosses().getLastUpdateQuery();
		}

		@Override
		protected Response doInBackground(Integer... params) {
			mThisQueryTime = new Date();
			return mModel.getServer().getNewCrossesAfter(mLastQueryTime);
		}

		@Override
		protected void onPostExecute(Response result) {
			int code = result.getCode();
			@SuppressWarnings("unused")
			int http_category = code % 100;

			switch (code) {
			case HttpStatus.SC_OK:
				List<Cross> xs = new ArrayList<Cross>();
				JSONObject res = result.getResponse();
				JSONArray array = res.optJSONArray("crosses");
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject json = array.optJSONObject(i);
						if (json != null) {
							Cross cross = (Cross) EntityFactory.create(json);
							xs.add(cross);
						}
					}
				}
				mModel.Crosses().setLastUpdateQuery(mThisQueryTime);
				mModel.Crosses().addCrosses(xs);
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

	}

	public static class CrossAdpater extends ArrayAdapter<Cross> {
		public static class ViewHolder {
			CheckedTextView title;
			SeperateTextView exfee;
			CheckedTextView time;
			TwoLineTextView time_mmmdd;
			CheckedTextView place;
			TextView msgCount;
			WeakReference<View> root;
		}

		private int mResource;
		@SuppressWarnings("unused")
		private int mDropDownResource;
		private LayoutInflater mInflater;
		private Model mModel;

		public CrossAdpater(Context context, int resource, List<Cross> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			init(context, resource);
		}

		private void init(Context context, int resource) {
			mModel = ((Application) context.getApplicationContext()).getModel();
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

			CheckedTextView title = holder.title;
			SeperateTextView exfee = holder.exfee;
			CheckedTextView time = holder.time;
			DoubleTextView time_mmmdd = holder.time_mmmdd;
			CheckedTextView place = holder.place;
			TextView count = holder.msgCount;
			@SuppressWarnings("unused")
			View root = holder.root.get();

			Cross x = getItem(position);
			x.loadFromDao(mModel.getHelper());

			String diff = x.diffByUpdate();

			/*
			 * if (avatar != null) { boolean flag = false; if
			 * (x.getByIdentitiy() != null) { if
			 * (!TextUtils.isEmpty(x.getByIdentitiy() .getAvatarFilename())) {
			 * Bitmap bm = ImageCache.getInst().getImageFrom(
			 * x.getByIdentitiy().getAvatarFilename()); if (bm != null) {
			 * avatar.setImageBitmap(bm); flag = true; } } } if (flag == false)
			 * { avatar.setImageResource(R.drawable.default_avatar); } }
			 */

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
				time.setText(x.getTime().getBeginAt()
						.getRelativeStringFromNow(time.getResources()));
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
				if (c < 30) {
					count.setText(String.valueOf(c));
				} else {
					count.setText("");
				}
				count.getBackground().setLevel(c);
			}
			return view;
		}

		private ViewHolder creatViewHolder(View view) {
			ViewHolder holder = new ViewHolder();
			holder.title = (CheckedTextView) view
					.findViewById(R.id.cross_title);
			holder.exfee = (SeperateTextView) view
					.findViewById(R.id.cross_exfee);
			holder.time = (CheckedTextView) view.findViewById(R.id.cross_time);
			holder.time_mmmdd = (TwoLineTextView) view
					.findViewById(R.id.cross_time_mmmdd);
			holder.place = (CheckedTextView) view
					.findViewById(R.id.cross_place);
			holder.msgCount = (TextView) view
					.findViewById(R.id.cross_message_count);
			holder.root = new WeakReference<View>(
					view.findViewById(R.id.list_cross_root));
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