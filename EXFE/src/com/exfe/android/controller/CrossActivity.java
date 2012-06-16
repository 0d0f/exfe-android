package com.exfe.android.controller;

import java.lang.ref.WeakReference;
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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.exfe.android.Activity;
import com.exfe.android.Application;
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

public class CrossActivity extends Activity implements Observer {

	public static final int RESULT_EXIT_APP = 1;

	private ArrayAdapter<Cross> mAdapter = null;
	private ImageView mUserAvatar = null;
	private TextView mExfeeTitle = null;

	private FetchCrossesTask mTask = null;

	private int mBackPressCount = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_cross);
		View v = null;

		v = findViewById(R.id.btn_edit_profile);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.btn_refresh);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.btn_clear);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.user_avatar);
		if (v != null) {
			mUserAvatar = (ImageView) v;
		}

		v = findViewById(R.id.user_name_and_title);
		if (v != null) {
			mExfeeTitle = (TextView) v;
		}

		v = findViewById(R.id.list_crosses);
		if (v != null) {
			ListView listView = (ListView) v;
			mAdapter = new CrossAdpater(this, R.layout.listitem_cross,
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
		Log.d(TAG, "RESULT: %d, %d", requestCode, resultCode);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		if (mBackPressCount == 0) {
			Toast.makeText(this, "Press back again to exit the app",
					Toast.LENGTH_SHORT).show();
			mBackPressCount++;
		} else {
			setResult(RESULT_EXIT_APP);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cross, menu);
		return true;
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

		if (observable instanceof ImageCache) {
			
		} else if (observable instanceof Model) {

			Bundle bundle = (Bundle) data;
			int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
			switch (type) {
			case CrossesModel.ACTION_TYPE_UPDATE_CROSSES:
				setCrosses(mModel.Crosses().getCrosses());
				break;
			case MeModel.ACTION_TYPE_UPDATE_MY_PROFILE:
				User u = mModel.Me().getProfile();
				loadHead(u);
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
			case R.id.btn_edit_profile:
				it = new Intent();
				it.setClass(CrossActivity.this, UserSettingsActivity.class);
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
			it.putExtra(CrossDetailActivity.FIELD_SHOW_CONVERSATION, false);
			it.setClass(CrossActivity.this, CrossDetailActivity.class);
			CrossActivity.this.startActivity(it);
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

				if (!TextUtils.isEmpty(avatar_file_name)) {
					Bitmap bm = ImageCache.getInst().getImageFrom(
							avatar_file_name);
					if (bm != null) {
						mUserAvatar.setImageBitmap(bm);
						flag = true;
					}
				}
				if (flag == false) {
					mUserAvatar.setImageResource(R.drawable.default_avatar);
				}
			}

			if (mExfeeTitle != null) {
				mExfeeTitle.setText(getResources().getString(
						R.string.someone_exfee, user.getName()));
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
			return mModel.getServer().getCrossesUpdatedAfter(mLastQueryTime);
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
			ImageView avatar;
			CheckedTextView title;
			CheckedTextView time;
			CheckedTextView place;
			WeakReference<View> root;
		}

		private int mResource;
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

			ImageView avatar = holder.avatar;
			CheckedTextView title = holder.title;
			CheckedTextView time = holder.time;
			CheckedTextView place = holder.place;
			View root = holder.root.get();

			Cross x = getItem(position);
			x.loadFromDao(mModel.getHelper());

			String diff = x.diffByUpdate();

			if (avatar != null) {
				boolean flag = false;
				if (x.getByIdentitiy() != null) {
					if (!TextUtils.isEmpty(x.getByIdentitiy()
							.getAvatarFilename())) {
						Bitmap bm = ImageCache.getInst().getImageFrom(
								x.getByIdentitiy().getAvatarFilename());
						if (bm != null) {
							avatar.setImageBitmap(bm);
							flag = true;
						}
					}
				}
				if (flag == false) {
					avatar.setImageResource(R.drawable.default_avatar);
				}
			}
			if (title != null) {
				title.setText(x.getTitle());
				title.setChecked(diff.contains("t"));
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
			return view;
		}

		private ViewHolder creatViewHolder(View view) {
			ViewHolder holder = new ViewHolder();
			holder.avatar = (ImageView) view
					.findViewById(R.id.cross_host_avatar);
			holder.title = (CheckedTextView) view
					.findViewById(R.id.cross_title);
			holder.time = (CheckedTextView) view.findViewById(R.id.cross_time);
			holder.place = (CheckedTextView) view
					.findViewById(R.id.cross_place);
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