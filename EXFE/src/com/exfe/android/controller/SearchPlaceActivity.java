package com.exfe.android.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exfe.android.Activity;
import com.exfe.android.Application;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.maps.ItemizedOverlay;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Place;
import com.exfe.android.util.JSONHelper;
import com.exfe.android.util.Tool;
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

	protected Model mModel = null;
	private MapController mMapController;
	private MapView mMapView;
	private ResultsOverlay mListOverlay;
	private CustomOverlay mCustomOverlay;
	private MyLocationOverlay myLocationOverlay;
	private PlaceAdapter mAdapter;
	private ListView mList;
	private EditText mInput;
	private TextView mKeyWordResult;
	private Place mKeyWordPlace;
	private Place mSelectPlace;
	private InputMethodManager mIMM;
	private float mDensity;

	private Place mPlace;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getApplicationContext()).getModel();
		mModel.addObserver(this);
		setContentView(R.layout.activity_search_place);

		mDensity = getResources().getDisplayMetrics().density;

		// mLocationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		View v = findViewById(R.id.btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.exfee_search_result);
		if (v != null) {
			mList = (ListView) v;
			mKeyWordResult = (TextView) getLayoutInflater().inflate(
					R.layout.listitem_place_result_head, mList, false);
			mList.addHeaderView(mKeyWordResult);
			mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View v,
						int position, long id) {

					dissmissCustomeDialog(R.id.map_venue);
					PlaceResult item = (PlaceResult) list.getAdapter().getItem(
							position);
					if (item != null) {
						mListOverlay.setFocus(item);
						mMapController.animateTo(item.getPoint());
						// showPopupItem(item);
						mPlace = item.convertToPlace();
					} else {
						mListOverlay.setFocus(null);
						if (position == 0) {
							mPlace = mKeyWordPlace;
						} else {
							mPlace = null;
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
					// TODO Auto-generated method stub
					if (mList != null && mList.getVisibility() == View.VISIBLE) {
						mList.setVisibility(View.GONE);
					}
					dissmissCustomeDialog(R.id.map_venue);

					mListOverlay.setFocus(null);

				}
			});
			mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(final TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) {

						Runnable run = new Runnable() {

							@Override
							public void run() {

								if (myLocationOverlay != null
										&& myLocationOverlay.getLastFix() != null) {
									final List<PlaceResult> list;
									final String keyword = v.getText()
											.toString();
									if (TextUtils.isEmpty(keyword)) {
										list = searchnearby(myLocationOverlay
												.getLastFix());
									} else {
										list = searchtext(keyword,
												myLocationOverlay.getLastFix());
									}

									mModel.mHandler.post(new Runnable() {

										@Override
										public void run() {
											if (list != null) {
												if (mKeyWordResult != null) {
													mKeyWordResult
															.setText(keyword);
												}
												if (mKeyWordPlace == null) {
													mKeyWordPlace = new Place();
												}
												mKeyWordPlace.setTitle(keyword);
												showPlaces(list);
											}
										}
									});
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

				Runnable run = new Runnable() {

					@Override
					public void run() {
						if (myLocationOverlay.getMyLocation() != null
								&& myLocationOverlay.getLastFix() != null) {
							myLocationOverlay.disableMyLocation();
							final List<PlaceResult> list = searchnearby(myLocationOverlay
									.getLastFix());

							mModel.mHandler.post(new Runnable() {

								@Override
								public void run() {
									myLocationOverlay.disableMyLocation();
									showPlaces(list);
									mMapController.animateTo(myLocationOverlay
											.getMyLocation());
								}
							});
						}
					}
				};
				new Thread(run).start();
			}
		});

		mMapView.getOverlays().add(
				new MapGestureDetectorOverlay(new SimpleGestureDetector()));

		myLocationOverlay.enableMyLocation();
		mMapView.getOverlays().add(myLocationOverlay);

		Drawable drawable = getResources().getDrawable(R.drawable.map_pin);
		mListOverlay = new ResultsOverlay(this, drawable);
		mMapView.getOverlays().add(mListOverlay);

		mCustomOverlay = new CustomOverlay(this, getResources().getDrawable(
				R.drawable.map_pin_blue));
		mMapView.getOverlays().add(mCustomOverlay);
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

			if (dissmissCustomeDialog(R.id.map_bubble)) {
				if (mCustomOverlay.size() > 0) {
					mCustomOverlay.clear();
				}
				return ;
			}

			MapView mapView = mMapView;
			Projection projection = mapView.getProjection();
			GeoPoint location = projection.fromPixels((int) e.getX(),
					(int) e.getY());

			if (dissmissCustomeDialog(R.id.map_venue)) {
				return;
			}

			LayoutInflater inflater = getLayoutInflater();
			View content = inflater.inflate(R.layout.popup_new_place, null);
			content.setId(R.id.map_bubble);
			View btnOK = content.findViewById(R.id.btn_ok);
			View btnCancel = content.findViewById(R.id.btn_cancel);

			SparseArray<Object> holder = new SparseArray<Object>();
			holder.put(R.id.venue_name, content.findViewById(R.id.venue_name));
			holder.put(R.id.venue_description,
					content.findViewById(R.id.venue_description));
			holder.put(R.id.btn_ok, location);
			btnOK.setTag(holder);
			btnOK.setOnClickListener(click);
			btnCancel.setOnClickListener(click);

			Point p = new Point();
			projection.toPixels(location, p);
			p.offset(0, (int) (-30 * mDensity));
			GeoPoint target = projection.fromPixels(p.x, p.y);

			MapView.LayoutParams params = new MapView.LayoutParams(
					(mapView.getWidth() - mapView.getPaddingLeft() - mapView
							.getPaddingRight()) * 2 / 3,
					MapView.LayoutParams.WRAP_CONTENT, target,
					MapView.LayoutParams.BOTTOM_CENTER);

			content.setLayoutParams(params);
			mapView.addView(content);
			mapView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			OverlayItem item = new OverlayItem(location, "", "");
			mCustomOverlay.clear();
			mCustomOverlay.addItem(item);

			WindowManager.LayoutParams para = getWindow().getAttributes();
			if ((para.softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE) == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
				projection.toPixels(location, p);
				p.offset(0, mapView.getHeight() / 6);
				GeoPoint loc = projection.fromPixels(p.x, p.y);
				mapView.getController().animateTo(loc);
			} else {
				mapView.getController().animateTo(location);
			}

		}

		View.OnClickListener click = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				case R.id.btn_ok:
					SparseArray<Object> holder = (SparseArray<Object>) v
							.getTag();
					String name = ((EditText) holder.get(R.id.venue_name))
							.getText().toString();
					String desp = ((EditText) holder
							.get(R.id.venue_description)).getText().toString();
					GeoPoint point = (GeoPoint) holder.get(R.id.btn_ok);

					dissmissCustomeDialog(R.id.map_bubble);
					break;
				case R.id.btn_cancel:
					mCustomOverlay.clear();
					dissmissCustomeDialog(R.id.map_bubble);
				}
			}
		};

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
		super.onPause();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		if (myLocationOverlay != null) {
			myLocationOverlay.disableCompass();
			myLocationOverlay.disableMyLocation();
		}
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
				if (mPlace != null) {
					data.putExtra(RESULT_FIELD_PLACE, mPlace.toJSON()
							.toString());
					setResult(Activity.RESULT_OK, data);
					finish();
				}
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

	private void showPlaces(List<PlaceResult> list) {
		mAdapter = new PlaceAdapter(SearchPlaceActivity.this,
				R.layout.listitem_place_result, list);

		mList.setAdapter(mAdapter);
		if (mList.getVisibility() != View.VISIBLE) {
			mList.setVisibility(View.VISIBLE);
		}

		if (mListOverlay != null) {
			mListOverlay.clear();
			mListOverlay.addAll(list);
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
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
			e.printStackTrace();
		} catch (JSONException e) {
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

	class PlaceResult extends OverlayItem {

		public PlaceResult(JSONObject json) {
			super(new GeoPoint(Tool.getE6(JSONHelper.optDouble(json,
					"geometry/location/lat")), Tool.getE6(JSONHelper.optDouble(
					json, "geometry/location/lng"))), JSONHelper.optString(
					json, "name"), "");
			mJson = json;
			// setMarker(arg0)
		}

		protected JSONObject mJson;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.android.maps.OverlayItem#routableAddress()
		 */
		@Override
		public String routableAddress() {
			return JSONHelper.optString(mJson, "formatted_address");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.android.maps.OverlayItem#getMarker(int)
		 */
		@Override
		public Drawable getMarker(int arg0) {
			// TODO Auto-generated method stub
			return super.getMarker(arg0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.maps.OverlayItem#setMarker(android.graphics.drawable
		 * .Drawable)
		 */
		@Override
		public void setMarker(Drawable arg0) {
			// TODO Auto-generated method stub
			super.setMarker(arg0);
		}

		public String getID() {
			return mJson.optString("id");
		}

		public String getName() {
			return mJson.optString("name");
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

		Place convertToPlace() {
			Place p = new Place();
			p.setTitle(getName());

			String desc = routableAddress();
			if (TextUtils.isEmpty(desc)) {
				desc = getVicinity();
			}
			p.setDescription(desc);

			p.setLng(String.valueOf(getLongitude()));
			p.setLat(String.valueOf(getLatitude()));
			p.setProvider("google");
			p.setExternalId(getID());

			return p;
		}

	}

	public class CustomOverlay extends ItemizedOverlay<OverlayItem> {

		public CustomOverlay(Context context, Drawable defaultMarker) {
			super(context, defaultMarker);
			// TODO Auto-generated constructor stub
			Log.d(TAG, "CustomOverlay is created");
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// TODO Auto-generated method stub
			Log.v(TAG, "CustomOverlay is tapeed");
			return super.onTap(arg0, arg1);
		}

		protected boolean onTap(int index) {
			// OverlayItem item = mOverlays.get(index);
			// mInput.setText(item.getTitle());
			// setFocus(item);
			// mMapController.animateTo(item.getPoint());

			return true;
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
				// return true;
			}

			boolean result = super.onTap(location, mapView);
			return result;
		}

		protected boolean onTap(int index) {
			dissmissCustomeDialog(R.id.map_venue);
			if (index >= 0 && index < mOverlays.size()) {
				PlaceResult item = mOverlays.get(index);
				if (item != null) {
					showPopupItem(item);
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

	void showPopupItem(PlaceResult item) {
		if (item != null) {
			GeoPoint location = item.getPoint();

			LayoutInflater inflater = getLayoutInflater();
			View content = inflater.inflate(R.layout.popup_known_place, null);
			content.setId(R.id.map_venue);
			TextView tv1 = (TextView) content.findViewById(R.id.text1);
			tv1.setText(item.getTitle());
			TextView tv2 = (TextView) content.findViewById(R.id.text2);
			tv2.setText(item.getVicinity());

			Projection projection = mMapView.getProjection();
			Point p = new Point();
			projection.toPixels(location, p);
			p.offset(0, (int) (-40 * mDensity));
			GeoPoint target = projection.fromPixels(p.x, p.y);

			MapView.LayoutParams params = new MapView.LayoutParams(
					(mMapView.getWidth() - mMapView.getPaddingLeft() - mMapView
							.getPaddingRight()) * 2 / 3,
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
			mListOverlay.setFocus(item);
			mMapController.animateTo(center);
		}
	}

	class PlaceAdapter extends ArrayAdapter<PlaceResult> {

		private Context mCtx;
		private int mResId;
		private LayoutInflater mInflater;

		public PlaceAdapter(Context context, int resource,
				List<PlaceResult> objects) {
			super(context, resource, View.NO_ID, objects);
			// TODO Auto-generated constructor stub
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
		public PlaceResult getItem(int position) {
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
			// TODO Auto-generated method stub
			SparseArray<View> holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(mResId, parent, false);
				holder = new SparseArray<View>();
				holder.put(R.id.text1, convertView.findViewById(R.id.text1));
				holder.put(R.id.text2, convertView.findViewById(R.id.text2));
				holder.put(R.id.edit, convertView.findViewById(R.id.edit));

				convertView.setTag(holder);
			} else {
				holder = (SparseArray<View>) convertView.getTag();
			}

			PlaceResult place = getItem(position);

			TextView main = (TextView) holder.get(R.id.text1);
			TextView alt = (TextView) holder.get(R.id.text2);
			ImageView edit = (ImageView) holder.get(R.id.edit);

			if (edit != null) {
				edit.setTag(place);
				edit.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
			}
			if (main != null) {
				main.setText(place.getName());
			}
			if (alt != null) {
				String addr = place.routableAddress();
				if (TextUtils.isEmpty(addr)) {
					addr = place.getVicinity();
				}
				alt.setText(addr);
			}

			return convertView;
		}
	}

}
