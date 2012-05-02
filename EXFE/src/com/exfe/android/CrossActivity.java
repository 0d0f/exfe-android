package com.exfe.android;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;

public class CrossActivity extends Activity implements Observer {

	private static final String TAG = CrossActivity.class.getSimpleName();

	private static final int ACTIVITY_RESULT_LOGIN = 12345;

	private ArrayAdapter<Cross> mAdapter = null;

	private FetchCrossesTask mTask = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.scr_cross);

		View btnNoti = findViewById(R.id.btn_notification);
		if (btnNoti != null) {
			btnNoti.setOnClickListener(mClickListener);
		}

		View btnSettings = findViewById(R.id.btn_settings);
		if (btnSettings != null) {
			btnSettings.setOnClickListener(mClickListener);
		}

		View btnRefresh = findViewById(R.id.btn_refresh);
		if (btnRefresh != null) {
			btnRefresh.setOnClickListener(mClickListener);
		}

		View btnClear = findViewById(R.id.btn_clear);
		if (btnClear != null) {
			btnClear.setOnClickListener(mClickListener);
		}

		View list = findViewById(R.id.list_crosses);
		if (list != null) {
			ListView listView = (ListView) list;
			mAdapter = new CrossAdpater(this, R.layout.listitem_cross,
					new ArrayList<Cross>());
			listView.setAdapter(mAdapter);
			listView.setOnItemClickListener(mItemClickListener);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (TextUtils.isEmpty(mModel.getUsername())) {
			Intent it = new Intent();
			it.setClass(this, LoginActivity.class);
			startActivityForResult(it, ACTIVITY_RESULT_LOGIN);
			overridePendingTransition(R.anim.slide_up_in, 0);
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

		showCrosses(mModel.getCrosses());
		if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
			mTask = new FetchCrossesTask();
		}
		mTask.execute();

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
		if (requestCode == ACTIVITY_RESULT_LOGIN) {
			if (resultCode == LoginActivity.RESULT_EXIT_LOGIN) {
				finish();
				Log.d(TAG, "CANCEL: Exit app");
			} else if (resultCode == RESULT_OK) {
				// login successfully
				Log.d(TAG, "OK: Login successfully");
			} else {
				Log.d(TAG, "OTHERS");
			}
		}

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
		case Model.ACTION_TYPE_UPDATE_CROSSES:
			showCrosses(mModel.getCrosses());
			break;
		}
	}
	
	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Intent it = null;
			switch (id) {
			case R.id.btn_clear:
				Log.v(TAG, "%s is clicked", "clear");
				mModel.clearCrosses();
				break;
			case R.id.btn_notification:
				it = new Intent();
				it.setClass(CrossActivity.this, NotificationActivity.class);
				startActivity(it);
				break;
			case R.id.btn_settings:
				it = new Intent();
				it.setClass(CrossActivity.this, UserSettingsActivity.class);
				startActivity(it);
				break;
			case R.id.btn_refresh:
				Log.v(TAG, "%s is clicked", "Refreash");
				// new checkLoginTask().execute(Const.username, Const.pwd);
				if (mTask == null
						|| mTask.getStatus() == AsyncTask.Status.FINISHED) {
					mTask = new FetchCrossesTask();
					mTask.execute();
				}
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
			it.setClass(CrossActivity.this, CrossDetailActivity.class);
			CrossActivity.this.startActivity(it);
		}

	};

	private void showCrosses(Collection<Cross> collection) {
		mAdapter.clear();
		for (Cross x : collection) {
			mAdapter.add(x);
		}
	}

	class FetchCrossesTask extends AsyncTask<Integer, Integer, Response> {

		@Override
		protected Response doInBackground(Integer... params) {
			return mModel.getServer().getCrossesByUser(mModel.getUserId());
		}

		@Override
		protected void onPostExecute(Response result) {
			int code = result.getCode();
			@SuppressWarnings("unused")
			int http_category = code % 100;
			
			switch(code){
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
				mModel.addCrosses(xs);
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
			TextView title;
			TextView time;
			TextView place;
			WeakReference<View> root;
		}

		private int mResource;
		private int mDropDownResource;
		private LayoutInflater mInflater;

		public CrossAdpater(Context context, int resource, List<Cross> objects) {
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
			TextView title = holder.title;
			TextView time = holder.time;
			TextView place = holder.place;
			View root = holder.root.get();

			Cross x = getItem(position);
			title.setText(x.getTitle());
			place.setText(x.getPlace().getTitle());
			// time.setText(x.getTime().display());
			time.setText(x.getCreatedAt());
			return view;
		}

		private ViewHolder creatViewHolder(View view) {
			ViewHolder holder = new ViewHolder();
			holder.avatar = (ImageView) view
					.findViewById(R.id.cross_host_avatar);
			holder.title = (TextView) view.findViewById(R.id.cross_title);
			holder.time = (TextView) view.findViewById(R.id.cross_time);
			holder.place = (TextView) view.findViewById(R.id.cross_place);
			holder.root = new WeakReference<View>(
					view.findViewById(R.id.list_cross_root));
			return holder;
		}

		/* (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		/* (non-Javadoc)
		 * @see android.widget.BaseAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

	}

}