package com.exfe.android.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Activity;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.MeModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.view.SeperatedBaseAdapter;

public class ProfileActivity extends Activity implements Observer {

	private User mMe = null;
	private ImageWorker mImageWorker = null;
	
	private ImageView mAvatar = null;
	private TextView mName = null;
	private EditText mNameInput = null;
	private ListView mList = null;
	private TextView mAttend = null;
	private IdentityAdpater mIdentityAdapter = null;
	private IdentityAdpater mDeviceAdapter = null;
	private SeperatedBaseAdapter mAdapter = null;
	private SeperatedBaseAdapter.SeperatedSectionFactory mFactory = new SeperatedBaseAdapter.SeperatedSectionFactory() {

		@Override
		public boolean hasHeader(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			return false;
		}

		@Override
		public boolean hasFooter(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			if (index == 0) {
				return true;
			} else if (index == 1) {
				return true;
			}
			return false;
		}

		@Override
		public View getHeader(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			View v = null;
			LayoutInflater mInflater = (LayoutInflater) container.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = mInflater.inflate(R.layout.listitem_add_identity, null);

			return v;
		}

		@Override
		public View getFoot(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			View v = null;
			LayoutInflater mInflater = (LayoutInflater) container.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (index == 0) {
				v = mInflater.inflate(R.layout.listitem_add_identity, null);
			} else {
				v = mInflater.inflate(R.layout.profile_foot, null);

				View btnSignOut = v.findViewById(R.id.btn_sign_out);
				btnSignOut.setOnClickListener(mLogout);
			}

			return v;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_profile);
		
		mImageWorker = new ImageFetcher(mModel.getAppContext(), getResources()
				.getDimensionPixelSize(R.dimen.small_avatar_width),
				getResources().getDimensionPixelSize(
						R.dimen.small_avatar_height));
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
		mImageWorker.setLoadingImage(R.drawable.default_avatar);

		View btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(mBackClick);

		mAvatar = (ImageView) findViewById(R.id.user_avatar);
		if (mAvatar != null) {
			mAvatar.setOnClickListener(mShowDrawable);
		}
		mNameInput = (EditText) findViewById(R.id.user_name_input);
		mNameInput
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE
								|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							new UpdateUsernameTask(v.getText().toString())
									.execute();
							return true;
						}
						return false;
					}
				});
		mName = (TextView) findViewById(R.id.user_name);
		mName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewSwitcher switcher = (ViewSwitcher) v.getParent();
				mNameInput.setText(((TextView) v).getText());
				switcher.showNext();

			}
		});

		mList = (ListView) findViewById(R.id.user_list);
		mAttend = (TextView) findViewById(R.id.user_cross_count);
		int[] reses = { R.layout.listitem_identity,
				R.layout.listitem_identity_device };
		mIdentityAdapter = new IdentityAdpater(this, reses,
				new ArrayList<Identity>());
		mDeviceAdapter = new IdentityAdpater(this, reses,
				new ArrayList<Identity>());
		mAdapter = new SeperatedBaseAdapter(this, mFactory);
		mAdapter.addAdapter(mIdentityAdapter);
		mAdapter.addAdapter(mDeviceAdapter);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(mItemClick);
		mMe = mModel.Me().getProfile();
		loadUser(mMe);
		loadIdentities(mMe);
		mModel.Me().fetchProfile();
	}

	class UpdateUsernameTask extends AsyncTask<Void, Integer, Response> {

		private String mNewName;

		public UpdateUsernameTask(String newName) {
			mNewName = newName;
		}

		@Override
		protected Response doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return getModel().getServer().updateUser(mNewName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Response result) {
			mNameInput.setEnabled(true);
			ViewSwitcher switcher = (ViewSwitcher) mNameInput.getParent();
			switcher.showPrevious();

			if (result.getCode() == HttpStatus.SC_OK) {

				JSONObject resp = result.getResponse();
				JSONObject myself = null;
				try {
					myself = resp.getJSONObject("user");
					User user = (User) EntityFactory.create(myself);

					mModel.Me().setProfile(user);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mNameInput.setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			mNameInput.setEnabled(true);
		}

	};

	View.OnClickListener mLogout = new View.OnClickListener() {

		@Override
		public void onClick(final View v) {
			signOut();
		}
	};

	View.OnClickListener mBackClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	View.OnClickListener mShowDrawable = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof ImageView) {
				Drawable d = ((ImageView) v).getDrawable();
				ImageView iv = new ImageView(v.getContext());
				iv.setImageDrawable(d);
				final PopupWindow popup = new PopupWindow(iv,
						WindowManager.LayoutParams.MATCH_PARENT,
						WindowManager.LayoutParams.MATCH_PARENT, true);
				popup.setContentView(iv);
				popup.setOutsideTouchable(false);
				popup.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
				popup.showAtLocation(v, Gravity.CENTER, 0, 0);
				iv.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						popup.dismiss();
					}
				});
			}
		}
	};

	AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> listView, View item,
				int position, long id) {
			if (id == Identity.NO_ID) {
				Log.d(TAG, "add Identity");
			} else {
				Identity identity = (Identity) listView.getAdapter().getItem(
						position);
			}
		}
	};

	protected void loadUser(User user) {
		if (user != null) {
			// set avatar
			String avatar_file_name = user.getAvatarFilename();
			if (!TextUtils.isEmpty(avatar_file_name)) {
				mImageWorker.loadImage(avatar_file_name, mAvatar);
			}

			// set name
			String name = user.getName();
			mName.setText(name);

			String attend = getResources().getString(R.string.x_attended,
					user.getCrossQuantity());
			int end = attend.indexOf('\n');
			SpannableString styledText = new SpannableString(attend.replace(
					"\n", ""));
			styledText.setSpan(new RelativeSizeSpan(20 / 12f), 0, end, 0);
			styledText.setSpan(
					new ForegroundColorSpan(getResources().getColor(
							R.color.x_attend)), 0, end, 0);
			styledText.setSpan(new StyleSpan(Typeface.BOLD), 0, end, 0);
			mAttend.setText(styledText);
		}
	}

	protected void loadIdentities(User user) {
		if (user != null) {
			mIdentityAdapter.setNotifyOnChange(false);
			mDeviceAdapter.setNotifyOnChange(false);
			mIdentityAdapter.clear();
			mDeviceAdapter.clear();
			for (Identity id : user.getIdentities()) {
				if (id.isDeviceToken()) {
					mDeviceAdapter.add(id);
				} else {
					mIdentityAdapter.add(id);
				}
			}
			mIdentityAdapter.setNotifyOnChange(true);
			mDeviceAdapter.setNotifyOnChange(true);
			mIdentityAdapter.notifyDataSetChanged();
			// mDeviceAdapter.notifyDataSetChanged();
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
		if (observable instanceof Model) {
			Bundle bundle = (Bundle) data;
			int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
			switch (type) {
			case MeModel.ACTION_TYPE_UPDATE_MY_PROFILE:
				mMe = mModel.Me().getProfile();
				loadUser(mMe);
				loadIdentities(mMe);
				break;
			}
		}
	}

	public class IdentityAdpater extends ArrayAdapter<Identity> {

		private int[] mResource;
		@SuppressWarnings("unused")
		private int[] mDropDownResource;
		private LayoutInflater mInflater;

		public IdentityAdpater(Context context, int[] resource,
				List<Identity> objects) {
			super(context, resource[0], objects);
			init(context, resource);
		}

		private void init(Context context, int[] resource) {
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
				ViewGroup parent, int[] resource) {
			View view;
			SparseArray<View> holder = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				view = mInflater.inflate(resource[type], parent, false);
				holder = creatViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				Object obj = view.getTag();
				if (obj == null) {
					holder = creatViewHolder(view);
					view.setTag(holder);
				} else {
					holder = (SparseArray<View>) obj;
				}
			}

			ImageView icon = (ImageView) holder.get(R.id.identity_icon);
			TextView main = (TextView) holder.get(R.id.identity_main);
			TextView alt = (TextView) holder.get(R.id.identity_alt);
			ImageView icon_type = (ImageView) holder
					.get(R.id.identity_type_icon);
			@SuppressWarnings("unused")
			View root = holder.get(R.id.list_identity_root);

			Identity id = getItem(position);

			if (icon != null) {
				boolean flag = false;
				if (type == 1) {
					icon.setImageResource(R.drawable.device_phone);
					flag = true;
				} else {
					if (!TextUtils.isEmpty(id.getAvatarFilename())) {
						mImageWorker.loadImage(id.getAvatarFilename(), icon);
					}
				}
			}

			if (main != null) {
				main.setText(id.getExternalUsername());
			}

			if (alt != null) {
				alt.setText(id.getExternalId());
			}

			if (icon_type != null) {
				if (type != 1) {
					int provider = Provider.getValue(id.getProvider());
					icon_type.setImageLevel(provider);
					icon_type.setVisibility(View.VISIBLE);
				}
			}
			return view;
		}

		private SparseArray<View> creatViewHolder(View view) {
			SparseArray<View> holder = new SparseArray<View>();
			holder.put(R.id.identity_icon,
					view.findViewById(R.id.identity_icon));
			holder.put(R.id.identity_main,
					view.findViewById(R.id.identity_main));
			holder.put(R.id.identity_alt, view.findViewById(R.id.identity_alt));
			holder.put(R.id.identity_type_icon,
					view.findViewById(R.id.identity_type_icon));
			holder.put(R.id.list_identity_root,
					view.findViewById(R.id.list_identity_root));
			return holder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			Identity id = getItem(position);
			if (id != null) {
				return id.getId();
			} else {
				return Identity.NO_ID;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#getItemViewType(int)
		 */
		@Override
		public int getItemViewType(int position) {
			Identity id = null;
			if (position < super.getCount()) {
				id = getItem(position);

			}

			if (id != null && id.isDeviceToken()) {
				return 1;
			}

			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#getViewTypeCount()
		 */
		@Override
		public int getViewTypeCount() {
			return 2;
		}

	}
}
