package com.exfe.android.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exfe.android.Activity;
import com.exfe.android.Application;
import com.exfe.android.Const;
import com.exfe.android.MessageID;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.maps.ItemizedOverlay;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Place;
import com.exfe.android.net.ReverseGeocodingTask;
import com.exfe.android.util.JSONHelper;
import com.exfe.android.util.Tool;
import com.exfe.android.view.CheckableRelativeLayout;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class SearchPlaceActivity extends MapActivity implements Observer {

	public static final int GAHTER_ID = 41234;

	protected final String TAG = getClass().getSimpleName();

	public static final String RESULT_FIELD_PLACE = "place";
	private static final int UI_MODE_SEARCH = 0;
	private static final int UI_MODE_EDIT = 1;
	private static final int UI_MODE_VENUE_POPUP = 2;

	protected Model mModel = null;
	private MapController mMapController;
	private MapView mMapView;
	private ResultsOverlay mListOverlay;
	private CustomOverlay mCustomOverlay;
	private MyLocationOverlay myLocationOverlay;
	private VenueAdapter mAdapter;
	private ListView mList;
	private EditText mInput;
	private Place mCustomPlace = null;
	private InputMethodManager mIMM;
	private float mDensity;
	private EditText mVenueName = null;
	private EditText mVenueDescription = null;
	private boolean needFixQuery = true;
	private Geocoder mGeoCoder = null;
	private boolean ignoreChange = false;

	private Place mSelectedPlace;
	private int mSelectedIndex;
	private int headCount = 0;

	private int mUIMode = UI_MODE_SEARCH;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MessageID.MSG_ID_TRIGGER_SERACH:
				searchVenues(msg.obj.toString());
				break;
			case MessageID.MSG_ID_NEED_CLEAR_CUSTOM_VENUE:
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SearchPlaceActivity.this);
				DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							setUIMode(UI_MODE_SEARCH);
							if (mCustomOverlay != null) {
								mCustomOverlay.clear();
							}
							mCustomPlace = null;
							mSelectedPlace = null;
							if (mInput != null) {
								mInput.setText("");
							}
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.dismiss();
							break;
						}
					}
				};
				builder.setMessage(
						R.string.do_you_want_to_remove_the_editing_venue)
						.setPositiveButton(android.R.string.ok, clickListener)
						.setNegativeButton(android.R.string.cancel,
								clickListener);
				AlertDialog dia = builder.create();
				dia.show();

				break;
			case ReverseGeocodingTask.MSG_ID_FILL_ADDRESS:
				if (getUIMode() == UI_MODE_EDIT) {
					if (mVenueDescription != null) {
						if (mVenueDescription.length() == 0) {
							ignoreChange = true;
							if (mVenueName != null && mVenueName.length() == 0) {
								mVenueName.setText(R.string.right_here);
							}
							mVenueDescription.setText(msg.obj.toString());
							ignoreChange = false;
						}
					}
				}
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}

	};

	/** Functions in Activity */
	protected Handler mUIHandler = new Handler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MessageID.MSG_ID_SHOW_PROGRESS_BAR:
				Log.i(TAG, "process showProgressBar msg");
				if (mProgressDialog == null) {
					mProgressDialog = new ProgressDialog(
							SearchPlaceActivity.this);
				}
				String[] data = (String[]) msg.obj;
				mProgressDialog.setTitle(data[0]);
				mProgressDialog.setMessage(data[1]);

				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				break;
			case MessageID.MSG_ID_DIMISS_PROGRESS_BAR:
				Log.i(TAG, "process hideProgressBar msg");
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				break;
			case MessageID.MSG_ID_SHOW_TOAST:
				int duration = msg.arg1;
				boolean showNow = msg.arg2 == 1;
				String message = msg.obj.toString();
				if (showNow && mToast != null) {
					mToast.cancel();
				}
				if (mToast == null) {
					mToast = Toast.makeText(SearchPlaceActivity.this, message,
							duration);
				} else {
					mToast.setText(message);
					mToast.setDuration(duration);
				}
				mToast.show();
				break;
			default:
				super.handleMessage(msg);

			}

		}

	};

	protected ProgressDialog mProgressDialog = null;

	public void showProgressBar(final String title, final String message) {
		Message.obtain(mUIHandler, MessageID.MSG_ID_SHOW_PROGRESS_BAR,
				new String[] { title, message }).sendToTarget();
	}

	public void showProgressBar(final String title, final String message,
			long delayMS) {
		Log.i(TAG, "send showProgressBar msg");
		Message msg = Message.obtain(mUIHandler,
				MessageID.MSG_ID_SHOW_PROGRESS_BAR, new String[] { title,
						message });
		mUIHandler.sendMessageDelayed(msg, delayMS);
	}

	public void hideProgressBar() {
		Log.i(TAG, "remove showProgressBar msg");
		mUIHandler.removeMessages(MessageID.MSG_ID_SHOW_PROGRESS_BAR);
		Log.i(TAG, "send hideProgressBar msg");
		Message.obtain(mUIHandler, MessageID.MSG_ID_DIMISS_PROGRESS_BAR)
				.sendToTarget();
	}

	protected Toast mToast = null;

	public void showToast(int msg) {
		showToast(getResources().getString(msg), Toast.LENGTH_SHORT);
	}

	public void showToast(String msg) {
		showToast(msg, Toast.LENGTH_SHORT);
	}

	public void showToast(int msg, int duration) {
		showToast(getResources().getString(msg), duration, false);
	}

	public void showToast(String msg, int duration) {
		showToast(msg, duration, false);
	}

	public void showToast(int msg, int duration, boolean showNow) {
		showToast(getResources().getString(msg), duration, showNow);
	}

	public void showToast(String msg, int duration, boolean showNow) {
		Message.obtain(mUIHandler, MessageID.MSG_ID_SHOW_TOAST, duration,
				showNow ? 1 : 0, msg).sendToTarget();
	}

	/** Functions in Activity */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getApplicationContext()).getModel();
		mModel.addObserver(this);
		setContentView(R.layout.activity_search_place);

		mDensity = getResources().getDisplayMetrics().density;

		mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mGeoCoder = new Geocoder(this);

		Intent it = getIntent();
		String placejson = it.getStringExtra(RESULT_FIELD_PLACE);
		if (!TextUtils.isEmpty(placejson) && Tool.isJson(placejson)) {
			mCustomPlace = (Place) EntityFactory.create(placejson);
		}

		View v = findViewById(R.id.btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.venue_name);
		if (v != null) {
			mVenueName = (EditText) v;
			mVenueName.addTextChangedListener(new TextWatcher() {

				int len = 0;

				@Override
				public void afterTextChanged(Editable s) {
					if (!ignoreChange) {
						if (len > 0 && s.length() == 0) {
							Message.obtain(mHandler,
									MessageID.MSG_ID_NEED_CLEAR_CUSTOM_VENUE)
									.sendToTarget();
						}
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					if (s != null) {
						len = s.length();
					}
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub

				}
			});
		}

		v = findViewById(R.id.venue_description);
		if (v != null) {
			mVenueDescription = (EditText) v;
		}

		v = findViewById(R.id.exfee_search_result);
		if (v != null) {
			mList = (ListView) v;
			mList.setVisibility(View.GONE);
			mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View v,
						int position, long id) {
					Log.d(TAG, "OnItemClick %d", position);

					dissmissCustomeDialog(R.id.map_venue);
					mSelectedIndex = position;
					mAdapter.notifyDataSetChanged();

					if (position < headCount) {
						mListOverlay.setFocus(null);
						mSelectedPlace = mCustomPlace;
						OverlayItem oitem = null;
						if (mCustomOverlay.size() > 0) {
							oitem = mCustomOverlay.getItem(0);
							mCustomOverlay.setFocus(mCustomOverlay.getItem(0));
							mMapController.animateTo(oitem.getPoint());
						}
					} else {
						PlaceResult item = (PlaceResult) list.getAdapter()
								.getItem(position);
						if (item != null) {
							mCustomOverlay.setFocus(null);
							mListOverlay.setFocus(item.getOverlayItem());
							mMapController.animateTo(item.getPoint());
							// showPopupItem(item);
							mSelectedPlace = item.convertToPlace();
						}

					}
				}
			});
			mList.setOnScrollListener(new AbsListView.OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					// TODO Auto-generated method stub
					Log.d(TAG, "onScrollStateChanged");

					if (scrollState != SCROLL_STATE_IDLE) {
						if (mIMM != null && mInput != null) {
							Log.d(TAG, "onScrollStateChanged: hide imm");
							mIMM.hideSoftInputFromWindow(
									mInput.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
						}
					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					Log.d(TAG, "onScroll");

				}
			});
		}

		v = findViewById(R.id.place_input);
		if (v != null) {
			mInput = (EditText) v;
			mInput.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					int previous = getUIMode();
					setUIMode(UI_MODE_SEARCH);
					if (TextUtils.isEmpty(mInput.getText().toString())) {
						mCustomPlace = null;
						mSelectedPlace = null;
						mCustomOverlay.clear();
					}
					if (previous == UI_MODE_EDIT) {
						mListOverlay.clear();
					} else {
						if (mList != null
								&& mList.getVisibility() != View.VISIBLE
								&& mList.getAdapter() != null
								&& mList.getAdapter().getCount() > 0) {
							mList.setVisibility(View.VISIBLE);
						}
					}
				}
			});
			mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(final TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH
							|| actionId == EditorInfo.IME_ACTION_DONE) {
						mHandler.removeMessages(MessageID.MSG_ID_TRIGGER_SERACH);
						searchVenues(v.getText().toString());
						return true;
					}
					return false;
				}
			});
			mInput.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					mHandler.removeMessages(MessageID.MSG_ID_TRIGGER_SERACH);
					Message msg = Message.obtain(mHandler,
							MessageID.MSG_ID_TRIGGER_SERACH, s.toString());
					msg.getTarget().sendMessageDelayed(msg,
							Const.SEARCH_TRIGGER_DELAY_IN_MS);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub

				}
			});
		}

		// Configure the Map
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(false);
		mMapView.setSatellite(false);
		mMapController = mMapView.getController();
		mMapController.setZoom(16); // Zoon 1 is world view

		myLocationOverlay = new MyLocationOverlay(this, mMapView);
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				Log.d(TAG, "runOnFirstFix");
				if (myLocationOverlay.getMyLocation() != null) {
					if (needFixQuery == true) {
						Runnable run = new Runnable() {

							@Override
							public void run() {
								Location loc = myLocationOverlay.getLastFix();
								// myLocationOverlay.disableMyLocation();
								final List<PlaceResult> list = searchnearby(loc);

								mHandler.post(new Runnable() {

									@Override
									public void run() {
										showPlaces(list, null);
									}
								});

							}
						};

						mMapController.animateTo(myLocationOverlay
								.getMyLocation());
						new Thread(run).start();
					} else {
						myLocationOverlay.disableMyLocation();
					}
				}
			}
		});
		mMapView.getOverlays().add(myLocationOverlay);

		Drawable drawable = getResources().getDrawable(R.drawable.map_pin);
		mListOverlay = new ResultsOverlay(this, drawable);
		mMapView.getOverlays().add(mListOverlay);

		mCustomOverlay = new CustomOverlay(this, getResources().getDrawable(
				R.drawable.map_pin_blue));
		mMapView.getOverlays().add(mCustomOverlay);

		mMapView.getOverlays().add(
				new MapGestureDetectorOverlay(new SimpleGestureDetector()));

		if (mCustomPlace != null
				&& (mCustomPlace.hasGeo() || !TextUtils.isEmpty(mCustomPlace
						.getTitle()))) {
			needFixQuery = !mCustomPlace.hasGeo();
			setUIMode(UI_MODE_EDIT);
			if (mMapController != null && mCustomPlace.hasGeo()) {
				mMapController.animateTo(Tool
						.getGeoPointFromLocation(mCustomPlace.getLocation()));
			}
		}
	}

	protected void searchVenues(final String keyword) {
		Runnable run = new Runnable() {

			@Override
			public void run() {

				Location loc = Tool.getLocationFromGeoPoint(mMapView
						.getMapCenter());

				if (loc != null) {
					final List<PlaceResult> list;
					if (TextUtils.isEmpty(keyword)) {
						list = searchnearby(loc);
					} else {
						list = searchtext(keyword.trim(), loc);
					}

					boolean needfill = false;
					if (mInput != null) {
						if (keyword.contentEquals(mInput.getText())) {
							needfill = true;
						}
					}

					if (needfill) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {

								if (list != null) {
									showPlaces(list, keyword.trim());
								}
							}
						});
					}
				}

			}

		};
		setUIMode(UI_MODE_SEARCH);
		showPlaces(null, keyword.trim());
		new Thread(run).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyLongPress(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyLongPress(keyCode, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (mInput != null) {
				mHandler.removeMessages(MessageID.MSG_ID_TRIGGER_SERACH);
				searchVenues(mInput.getText().toString());
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	class SimpleGestureDetector extends GestureDetector.SimpleOnGestureListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onLongPress(
		 * android.view.MotionEvent)
		 */
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onLongPress(e);

			Log.d(TAG, "MapView Gesture Layer, long press detected");

			MapView mapView = mMapView;
			Projection projection = mapView.getProjection();

			if (mCustomPlace == null) {
				mCustomPlace = new Place();
			} else {
				// mCustomPlace.clear();
				mCustomPlace.clearGeo();
			}

			GeoPoint gp = projection.fromPixels((int) e.getX(), (int) e.getY());
			Location loc = Tool.getLocationFromGeoPoint(gp);
			mCustomPlace.setLat(loc.getLatitude());
			mCustomPlace.setLng(loc.getLongitude());
			setUIMode(UI_MODE_EDIT);
			new ReverseGeocodingTask(SearchPlaceActivity.this, mHandler)
					.execute(loc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed
		 * (android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapConfirmed(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
		}

		if (mInput != null) {
			mInput.post(new Runnable() {
				public void run() {
					if (!mInput.requestFocus()) {
						Log.d(TAG, "not focus, delay for 300");
						mInput.postDelayed(this, 300);
					} else {
						Log.d(TAG, "focused, try show ime");
						if (mIMM != null && mInput != null) {
							Log.d(TAG, "show ime...");
							mIMM.showSoftInput(mInput,
									InputMethodManager.SHOW_IMPLICIT);
						}
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
		}
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
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
				if (getUIMode() == UI_MODE_EDIT) {
					if ((mVenueName != null && mVenueName.length() == 0
							&& mVenueDescription != null && mVenueDescription
							.length() > 0)) {
						showToast(R.string.place_title_or_location_is_needed);
						break;
					}
					setUIMode(UI_MODE_SEARCH);
					mSelectedPlace = mCustomPlace;
				}
				if (mSelectedPlace != null) {
					if (!TextUtils.isEmpty(mSelectedPlace.getTitle())
							|| mSelectedPlace.hasGeo()) {
						data.putExtra(RESULT_FIELD_PLACE, mSelectedPlace
								.toJSON().toString());
						setResult(Activity.RESULT_OK, data);
						finish();
					} else {
						showToast(R.string.place_title_or_location_is_needed);
					}
				} else {
					if (mInput != null) {
						// clear place
						if (mInput.getText().toString().isEmpty()) {
							data.putExtra(RESULT_FIELD_PLACE, (String) null);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
					}
				}

				break;
			case R.id.btn_back:
				setResult(Activity.RESULT_CANCELED, null);
				finish();
				break;
			case R.id.edit:
				Log.d(TAG, "edit clicked");
				IVenue venue = (IVenue) v.getTag();
				mCustomPlace = venue.convertToPlace();
				mSelectedPlace = mCustomPlace;
				setUIMode(UI_MODE_EDIT);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void showPlaces(List<PlaceResult> list, String keyword) {

		PlaceWrapper pw = null;
		if (!TextUtils.isEmpty(keyword)) {
			if (mCustomPlace == null) {
				mCustomPlace = new Place();
			} else {
				mCustomPlace.clear();
			}
			mCustomPlace.setTitle(keyword);
			pw = new PlaceWrapper(mCustomPlace);
		}

		updateCustomeLayer(pw);

		if (mAdapter == null) {
			mAdapter = new VenueAdapter(SearchPlaceActivity.this,
					R.layout.listitem_place_result, new ArrayList<IVenue>());
			mList.setAdapter(mAdapter);
		} else {
			mAdapter.clear();
		}
		if (pw != null) {
			mAdapter.add(pw);
			headCount = 1;
			mSelectedIndex = 0;
		} else {
			headCount = 0;
			mSelectedIndex = -1;
		}
		if (list != null) {
			mAdapter.addAllBatch(list);
		}

		if (mList.getVisibility() != View.VISIBLE) {
			if (!mAdapter.isEmpty()) {
				mList.setVisibility(View.VISIBLE);
			}
		}

		if (mListOverlay != null) {
			mListOverlay.clear();
			if (list != null) {
				mListOverlay.addAll(list);
			}
		}
	}

	void updateCustomeLayer(PlaceWrapper pw) {

		if (mCustomOverlay != null) {
			mCustomOverlay.clear();
			if (pw != null && mCustomPlace.hasGeo()) {
				mCustomOverlay.addItem(pw);
			}
		}
	}

	public List<PlaceResult> searchnearby(Location location) {
		// https://developers.google.com/places/documentation/search#PlaceSearchRequests
		// https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=37.7750,-122.4183&radius=500&sensor=false&key=AIzaSyBOLNH8RbXXPmQSYMRXP8XsUDBotoETkfo
		String result = "";
		List<PlaceResult> list = new ArrayList<PlaceResult>();
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;

		try {
			URL url = new URL(
					String.format(
							"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=500&sensor=false&key=AIzaSyCO_MQfEQI-p0r4tlQ3lj0WKLwbMtR5f3A",
							location.getLatitude(), location.getLongitude()));
			Log.d(TAG, "connect (HTTP GET) to ( %s )", url);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.connect();
			Log.d(TAG, "Response Code: %d", urlConnection.getResponseCode());
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			result = sb.toString();

			// Log.d(TAG, "Response String: %s", result);

			JSONObject json = new JSONObject(result);

			if ("OK".equalsIgnoreCase(json.optString("status"))) {
				JSONArray array = json.optJSONArray("results");

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.optJSONObject(i);
					if (obj != null) {
						list.add(new PlaceResult(obj));
					}
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			showToast(String.format("%s: %s", "Network error", e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			showToast(String.format("%s: %s", "Network error", e.getMessage()));
			e.printStackTrace();
		} catch (JSONException e) {
			showToast(String.format("%s: %s", "Parsing response error: ",
					e.getMessage()));
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<PlaceResult> searchtext(String keyword, Location location) {
		// https://developers.google.com/places/documentation/search#TextSearchRequests
		// https://maps.googleapis.com/maps/api/place/textsearch/json?location=37.7750,-122.4183&radius=500&query=%E5%90%83%E9%A5%AD&sensor=false&key=AIzaSyBOLNH8RbXXPmQSYMRXP8XsUDBotoETkfo
		String result = "";
		List<PlaceResult> list = new ArrayList<PlaceResult>();
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;

		try {
			URL url = new URL(
					String.format(
							"https://maps.googleapis.com/maps/api/place/textsearch/json?location=%f,%f&radius=500&query=%s&sensor=false&key=AIzaSyCO_MQfEQI-p0r4tlQ3lj0WKLwbMtR5f3A",
							location.getLatitude(), location.getLongitude(),
							URLEncoder.encode(keyword, "UTF-8")));
			Log.d(TAG, "connect (HTTP GET) to ( %s )", url);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.connect();
			Log.d(TAG, "Response Code: %d", urlConnection.getResponseCode());
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			result = sb.toString();

			// Log.d(TAG, "Response String: %s", result);

			JSONObject json = new JSONObject(result);

			if ("OK".equalsIgnoreCase(json.optString("status"))) {
				JSONArray array = json.optJSONArray("results");

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.optJSONObject(i);
					if (obj != null) {
						list.add(new PlaceResult(obj));
					}
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			showToast(String.format("%s: %s", "Network error", e.getMessage()));
			e.printStackTrace();
		} catch (JSONException e) {
			showToast(String.format("%s: %s", "Parsing response error: ",
					e.getMessage()));
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	class PlaceWrapper implements IVenue {

		protected Place mPlace = null;
		protected OverlayItem itemCache = null;

		public PlaceWrapper(Place place) {
			super();
			setPlace(place);
		}

		public void setPlace(Place p) {
			mPlace = p;
			itemCache = null;
		}

		@Override
		public GeoPoint getPoint() {
			return new GeoPoint(Tool.getE6(Location.convert(String
					.valueOf(mPlace.getLat()))), Tool.getE6(Location
					.convert(String.valueOf(mPlace.getLng()))));
		}

		@Override
		public String getTitle() {
			// TODO Auto-generated method stub
			return mPlace.getTitle();
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return mPlace.getDescription();
		}

		@Override
		public Place convertToPlace() {
			// TODO Auto-generated method stub
			return mPlace;
		}

		@Override
		public OverlayItem getOverlayItem() {
			if (itemCache == null) {
				itemCache = new OverlayItem(getPoint(), getTitle(),
						getDescription());
			}
			return itemCache;
		}

	}

	class PlaceResult implements IVenue {

		protected JSONObject mJson;
		protected OverlayItem itemCache = null;

		public PlaceResult(JSONObject json) {
			mJson = json;
		}

		public String getID() {
			return mJson.optString("id");
		}

		public String getIcon() {
			return mJson.optString("icon");
		}

		JSONObject getGeometry() {
			return mJson.optJSONObject("geometry");
		}

		JSONObject getLocation() {
			JSONObject jo = getGeometry();
			if (jo != null) {
				return jo.optJSONObject("location");
			}
			return null;
		}

		double getLongitude() {
			JSONObject jo = getLocation();
			if (jo != null) {
				return jo.optDouble("lng");
			}
			return 0;
		}

		double getLatitude() {
			JSONObject jo = getLocation();
			if (jo != null) {
				return jo.optDouble("lat");
			}
			return 0;
		}

		String getVicinity() {
			return mJson.optString("vicinity");
		}

		public String getTitle() {
			return JSONHelper.optString(mJson, "name");
		}

		public Place convertToPlace() {
			Place p = new Place();
			p.setTitle(getTitle());
			p.setDescription(getDescription());
			p.setLng(getLongitude());
			p.setLat(getLatitude());
			p.setProvider("google");
			p.setExternalId(getID());
			return p;
		}

		@Override
		public String getDescription() {
			String desc = mJson.optString("formatted_address");
			if (TextUtils.isEmpty(desc)) {
				desc = getVicinity();
			}
			return desc;
		}

		@Override
		public GeoPoint getPoint() {
			return new GeoPoint(Tool.getE6(JSONHelper.optDouble(mJson,
					"geometry/location/lat")), Tool.getE6(JSONHelper.optDouble(
					mJson, "geometry/location/lng")));
		}

		@Override
		public OverlayItem getOverlayItem() {
			if (itemCache == null) {
				itemCache = new OverlayItem(getPoint(), getTitle(),
						getDescription());
			}
			return itemCache;
		}
	}

	public class CustomOverlay extends ItemizedOverlay<PlaceWrapper> {

		public CustomOverlay(Context context, Drawable defaultMarker) {
			super(context, defaultMarker);
			// TODO Auto-generated constructor stub
			Log.d(TAG, "CustomOverlay is created");
		}

		@Override
		public boolean onTap(GeoPoint location, MapView mapView) {
			// TODO Auto-generated method stub
			Log.v(TAG, "CustomOverlay is tapeed");
			if (mList.getVisibility() == View.VISIBLE) {
				mList.setVisibility(View.GONE);
				mapView.requestFocus();
			}

			boolean result = false;
			if (getUIMode() != UI_MODE_EDIT) {
				result = super.onTap(location, mapView);
			}
			return result;
		}

		protected boolean onTap(int index) {
			Log.v(TAG, "CustomOverlay is tapeed at %d", index);
			if (index >= 0 && index < mOverlays.size()) {
				PlaceWrapper item = mOverlays.get(index);
				if (item != null) {
					dissmissCustomeDialog(R.id.map_venue);
					hideEditVenue();
					mListOverlay.setFocus(null);
					mSelectedPlace = mCustomPlace;
					OverlayItem oi = getItem(index);
					setFocus(oi);
					showPopupItem((IVenue) item);
					setUIMode(UI_MODE_VENUE_POPUP);
					return true;
				}
			}
			return false;
		};

	}

	public class ResultsOverlay extends ItemizedOverlay<PlaceResult> {

		public ResultsOverlay(Context context, Drawable defaultMarker) {
			super(context, defaultMarker);
			// TODO Auto-generated constructor stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.maps.ItemizedOverlay#onTap(com.google.android.
		 * maps.GeoPoint, com.google.android.maps.MapView)
		 */
		@Override
		public boolean onTap(GeoPoint location, MapView mapView) {
			Log.v(TAG, "ResultsOverlay Tap");
			if (mList.getVisibility() == View.VISIBLE) {
				mList.setVisibility(View.GONE);
				mapView.requestFocus();
			}
			boolean result = super.onTap(location, mapView);
			return result;
		}

		protected boolean onTap(int index) {
			Log.v(TAG, "ResultsOverlay Tap at %d", index);
			if (index >= 0 && index < mOverlays.size()) {
				PlaceResult item = mOverlays.get(index);
				if (item != null) {
					dissmissCustomeDialog(R.id.map_venue);
					hideEditVenue();
					mCustomOverlay.setFocus(null);
					mSelectedPlace = item.convertToPlace();
					OverlayItem oi = getItem(index);
					setFocus(oi);

					showPopupItem(item);
					setUIMode(UI_MODE_VENUE_POPUP);
					return true;
				}
			}
			return false;
		};

	}

	public class MapGestureDetectorOverlay extends Overlay implements
			GestureDetector.OnGestureListener {
		private GestureDetector gestureDetector;
		private GestureDetector.OnGestureListener onGestureListener;

		public MapGestureDetectorOverlay() {
			gestureDetector = new GestureDetector(SearchPlaceActivity.this,
					this);
		}

		public MapGestureDetectorOverlay(
				GestureDetector.OnGestureListener onGestureListener) {
			this();
			setOnGestureListener(onGestureListener);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (gestureDetector.onTouchEvent(event)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (onGestureListener != null) {
				return onGestureListener.onDown(e);
			}
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (onGestureListener != null) {
				return onGestureListener.onFling(e1, e2, velocityX, velocityY);
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (onGestureListener != null) {
				onGestureListener.onLongPress(e);
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (onGestureListener != null) {
				onGestureListener.onScroll(e1, e2, distanceX, distanceY);
			}
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			if (onGestureListener != null) {
				onGestureListener.onShowPress(e);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (onGestureListener != null) {
				onGestureListener.onSingleTapUp(e);
			}
			return false;
		}

		public boolean isLongpressEnabled() {
			return gestureDetector.isLongpressEnabled();
		}

		public void setIsLongpressEnabled(boolean isLongpressEnabled) {
			gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
		}

		public OnGestureListener getOnGestureListener() {
			return onGestureListener;
		}

		public void setOnGestureListener(OnGestureListener onGestureListener) {
			this.onGestureListener = onGestureListener;
		}
	}

	boolean dissmissCustomeDialog(int id) {
		View v = mMapView.findViewById(id);
		if (v != null) {
			mMapView.removeView(v);
			return true;
		} else {
			return false;
		}
	}

	void showPopupItem(IVenue item) {
		if (item != null) {
			GeoPoint location = item.getPoint();

			LayoutInflater inflater = getLayoutInflater();
			View content = inflater.inflate(R.layout.popup_known_place, null);
			content.setId(R.id.map_venue);
			TextView tv1 = (TextView) content.findViewById(R.id.text1);
			tv1.setText(item.getTitle());
			TextView tv2 = (TextView) content.findViewById(R.id.text2);
			tv2.setText(item.getDescription());
			View edit = content.findViewById(R.id.edit);
			edit.setOnClickListener(mClickListener);
			edit.setTag(item);

			Projection projection = mMapView.getProjection();
			Point p = new Point();
			projection.toPixels(location, p);
			p.offset(0, (int) (-40 * mDensity));
			GeoPoint target = projection.fromPixels(p.x, p.y);

			MapView.LayoutParams params = new MapView.LayoutParams(
					MapView.LayoutParams.WRAP_CONTENT,
					MapView.LayoutParams.WRAP_CONTENT, target,
					MapView.LayoutParams.BOTTOM_CENTER);

			content.setLayoutParams(params);
			mMapView.addView(content);
			mMapView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			GeoPoint center = item.getPoint();

			if (mMapView.getWidth() > mMapView.getHeight()) {
				projection.toPixels(center, p);
				p.offset(0, -mMapView.getHeight() / 3);
				center = projection.fromPixels(p.x, p.y);
			}
			mMapController.animateTo(center);
		}
	}

	interface IVenue extends ItemizedOverlay.IOverlayItem {

		public GeoPoint getPoint();

		public String getTitle();

		public String getDescription();

		public Place convertToPlace();

		public OverlayItem getOverlayItem();
	}

	class VenueAdapter extends ArrayAdapter<IVenue> {

		private Context mCtx;
		private int mResId;
		private LayoutInflater mInflater;

		public VenueAdapter(Context context, int resource, List<IVenue> objects) {
			super(context, resource, View.NO_ID, objects);
			mResId = resource;
			mCtx = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItem(int)
		 */
		@Override
		public IVenue getItem(int position) {
			// TODO Auto-generated method stub
			return super.getItem(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return super.getItemId(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SparseArray<View> holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(mResId, parent, false);
				holder = new SparseArray<View>();
				holder.put(R.id.text_main,
						convertView.findViewById(R.id.text_main));
				holder.put(R.id.text_alt,
						convertView.findViewById(R.id.text_alt));
				holder.put(R.id.edit, convertView.findViewById(R.id.edit));
				holder.put(R.id.list_place_root,
						convertView.findViewById(R.id.list_place_root));
				convertView.setTag(holder);
			} else {
				holder = (SparseArray<View>) convertView.getTag();
			}

			IVenue venue = getItem(position);

			boolean selected = mSelectedIndex == position;

			TextView main = (TextView) holder.get(R.id.text_main);
			TextView alt = (TextView) holder.get(R.id.text_alt);
			View edit = holder.get(R.id.edit);
			CheckableRelativeLayout root = (CheckableRelativeLayout) holder
					.get(R.id.list_place_root);

			if (edit != null) {
				edit.setTag(venue);
				edit.setOnClickListener(mClickListener);
			}

			if (main != null) {
				main.setText(venue.getTitle());
			}

			if (alt != null) {
				alt.setText(venue.getDescription());
			}

			if (root != null) {
				root.setChecked(selected);
			}

			return convertView;
		}

		public void addAllBatch(Collection<? extends IVenue> list) {
			setNotifyOnChange(false);
			for (IVenue item : list) {
				add(item);
			}
			setNotifyOnChange(true);
			notifyDataSetChanged();
		}
	}

	boolean isEditVenueVisible() {
		boolean result = false;
		if (mVenueName != null) {
			View root = (View) mVenueName.getParent();
			result = root.getVisibility() == View.VISIBLE;
		}
		return result;
	}

	void showEditVenue(Place place) {
		if (mVenueName != null && mVenueDescription != null) {
			mSelectedPlace = place;
			View root = (View) mVenueName.getParent();
			ignoreChange = true;
			mVenueName.setText(place.getTitle());
			mVenueDescription.setText(place.getDescription());
			ignoreChange = false;
			root.setVisibility(View.VISIBLE);
			mVenueName.requestFocus();
		}
	}

	void hideEditVenue() {
		if (mSelectedPlace != null && isEditVenueVisible()) {
			View root = (View) mVenueName.getParent();
			mSelectedPlace.setTitle(mVenueName.getText().toString());
			mSelectedPlace.setDescription(mVenueDescription.getText()
					.toString());
			root.setVisibility(View.GONE);
		}
	}

	/**
	 * @return the UIMode
	 */
	public int getUIMode() {
		return mUIMode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setUIMode(int mode) {
		// if (mUIMode != mode) {
		switch (mode) {
		case UI_MODE_SEARCH:
			Log.d(TAG, "UI_MODE_SEARCH");
			if (mList != null && mList.getVisibility() == View.VISIBLE) {
				mList.setVisibility(View.GONE);
			}
			hideEditVenue();
			dissmissCustomeDialog(R.id.map_venue);

			break;
		case UI_MODE_EDIT:
			Log.d(TAG, "UI_MODE_EDIT");
			dissmissCustomeDialog(R.id.map_venue);
			if (mList != null && mList.getVisibility() == View.VISIBLE) {
				mList.setVisibility(View.GONE);
			}
			if (mListOverlay != null) {
				mListOverlay.clear();
			}
			if (mCustomPlace != null) {
				updateCustomeLayer(new PlaceWrapper(mCustomPlace));
				showEditVenue(mCustomPlace);
			} else {
				Log.w(TAG, "No custom place selected");
			}
			break;
		case UI_MODE_VENUE_POPUP:
			Log.d(TAG, "UI_MODE_VENUE_POPUP");
			hideEditVenue();
			if (mList != null && mList.getVisibility() == View.VISIBLE) {
				mList.setVisibility(View.GONE);
			}
			// showPopup...
			break;
		}

		this.mUIMode = mode;
		// }
	}

}
