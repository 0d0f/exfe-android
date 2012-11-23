package com.exfe.android.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Activity;
import com.exfe.android.R;
import com.exfe.android.controller.CrossListFragment.OrmLiteCursorLoader;
import com.exfe.android.controller.ProfileActivity.IdentityAdpater;
import com.exfe.android.controller.SearchPlaceActivity.PlaceResult;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.flurry.android.FlurryAgent;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;

public class SearchExfeeActivity extends Activity implements Observer {

	// public static final int GAHTER_ID = 41234;
	public static final String RESULT_FIELD_IDENTITY = "identity";

	static final int LOADER_QUERY_LOCAL = 1;
	static final int LOADER_QUERY_NETWORK = 2;

	private Cross mCross = null;
	private ImageWorker mImageWorker = null;
	private IdentityAdpater mIdentityAdapter = null;
	private ListView mList = null;
	private TextView mInput = null;

	public SearchExfeeActivity() {
		// TODO Auto-generated constructor stub
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_search_exfee);

		mImageWorker = new ImageFetcher(mModel.getAppContext(), getResources()
				.getDimensionPixelSize(R.dimen.small_avatar_width),
				getResources().getDimensionPixelSize(
						R.dimen.small_avatar_height));
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
		mImageWorker.setLoadingImage(R.drawable.default_avatar);

		View v = findViewById(R.id.btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.exfee_list_input);
		if (v != null) {
			mInput = (TextView) v;
			mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(final TextView v, int actionId,
						KeyEvent event) {

					if (actionId == EditorInfo.IME_ACTION_SEARCH) {

						Runnable run = new Runnable() {

							@Override
							public void run() {
								final List<PlaceResult> list;
								final String keyword = v.getText().toString();

								Response result = mModel.getServer()
										.searchIdentities(keyword);
								if (result != null) {
									try {
										int code = result.getCode();
										switch (code) {
										case HttpStatus.SC_OK:

											JSONObject resp = result
													.getResponse();
											JSONArray idents = resp
													.getJSONArray("identities");

											final List<Identity> l = new ArrayList<Identity>();
											for (int i = 0; i < idents.length(); i++) {
												JSONObject json = idents
														.getJSONObject(i);
												Identity ident = (Identity) EntityFactory
														.create(json);
												if (ident != null) {
													l.add(ident);
												}
											}

											mModel.mHandler
													.post(new Runnable() {

														@Override
														public void run() {
															// fill result
															if (mIdentityAdapter != null) {
																mIdentityAdapter
																		.setNotifyOnChange(false);
																mIdentityAdapter
																		.clear();
																for (Identity id : l) {
																	mIdentityAdapter
																			.add(id);
																}
																mIdentityAdapter
																		.setNotifyOnChange(true);
																mIdentityAdapter
																		.notifyDataSetChanged();
															}
														}
													});
											break;
										case HttpStatus.SC_NOT_FOUND:
											break;
										default:
											break;
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

							}

						};
						new Thread(run).start();

						return true;
					}

					return false;
				}
			});
		}

		v = findViewById(R.id.btn_invite);
		if (v != null) {
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mInput != null) {
						String identity = mInput.getText().toString().trim();
						final int provider = Provider.checkType(identity);
						if (provider > Provider.UNKNOWN) {
							final String external_username = Provider
									.extraExternalUsername(identity, provider);

							Runnable run = new Runnable() {

								@Override
								public void run() {
									Response result = mModel
											.getServer()
											.getIdentity(
													Provider.getString(provider),
													null, external_username);

									if (result != null) {
										try {
											int code = result.getCode();
											switch (code) {
											case HttpStatus.SC_OK:

												JSONObject resp = result
														.getResponse();
												JSONArray idents = resp
														.getJSONArray("identities");

												List<Identity> l = new ArrayList<Identity>();
												for (int i = 0; i < idents
														.length(); i++) {
													JSONObject json = idents
															.getJSONObject(i);
													Identity ident = (Identity) EntityFactory
															.create(json);
													if (ident != null) {
														l.add(ident);
													}
												}

												if (l != null && l.size() > 0) {
													final Identity ident = l.get(0);
													mModel.mHandler
															.post(new Runnable() {

																@Override
																public void run() {

																	Intent data = new Intent();
																	data.putExtra(
																			RESULT_FIELD_IDENTITY,
																			ident.toString());
																	setResult(
																			Activity.RESULT_OK,
																			data);
																	finish();
																}
															});
												}
												break;
											case HttpStatus.SC_NOT_FOUND:
												break;
											default:
												break;
											}
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							};
							new Thread(run).start();
						}
					}
				}
			});
		}

		mIdentityAdapter = new IdentityAdpater(this,
				R.layout.listitem_search_identity, new ArrayList<Identity>());
		v = findViewById(R.id.exfee_search_result);
		if (v != null) {
			mList = (ListView) v;
			mList.setAdapter(mIdentityAdapter);
			mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View view,
						int pos, long id) {

					IdentityAdpater adapter = (IdentityAdpater) list
							.getAdapter();
					Identity ident = adapter.getItem(pos);
					Intent data = new Intent();
					data.putExtra(RESULT_FIELD_IDENTITY, ident.toString());
					setResult(Activity.RESULT_OK, data);
					finish();

				}
			});
		}

		mCross = new Cross();

	}

	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			Intent data = new Intent();

			switch (id) {
			case R.id.btn_action:
				// data.putExtra("cross", mCross.toString());
				// setResult(Activity.RESULT_OK, data);
				setResult(Activity.RESULT_CANCELED, data);
				finish();
				break;
			default:
				break;
			}
		}
	};

	public class IdentityAdpater extends ArrayAdapter<Identity> {

		private int mResource;
		@SuppressWarnings("unused")
		private int mDropDownResource;
		private LayoutInflater mInflater;

		public IdentityAdpater(Context context, int resource,
				List<Identity> objects) {
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
			SparseArray<View> holder = null;
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
				if (!TextUtils.isEmpty(id.getAvatarFilename())) {
					mImageWorker.loadImage(id.getAvatarFilename(), icon);
				}
			}

			if (main != null) {
				if (Provider.TWITTER == Provider.getValue(id.getProvider())) {
					main.setText(id.getName());
				} else {
					main.setText(id.getExternalUsername());
				}
			}

			if (alt != null) {
				if (Provider.TWITTER == Provider.getValue(id.getProvider())) {
					alt.setText("@" + id.getExternalUsername());
				} else {
					alt.setText(id.getExternalId());
				}
			}

			if (icon_type != null) {
				int provider = Provider.getValue(id.getProvider());
				icon_type.setImageLevel(provider);
				icon_type.setVisibility(View.VISIBLE);
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

	}

}
