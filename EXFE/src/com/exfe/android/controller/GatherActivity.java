package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.CrossTime;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Place;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.Rsvp;
import com.exfe.android.model.entity.Widget;
import com.exfe.android.util.Tool;
import com.flurry.android.FlurryAgent;

public class GatherActivity extends Activity implements Observer {

	public static final int GAHTER_ID = 41234;
	public static final int GATHER_TIME_REQUEST = 55001;
	public static final int GATHER_PLACE_REQUEST = 55002;
	public static final int GATHER_EXFEE_REQUEST = 55003;

	public static final String RESULT_FIELD_CROSS = "cross";

	private ImageWorker mImageWorker = null;

	private boolean isUpdating = false;

	private Cross mCross = null;

	public GatherActivity() {
		// TODO Auto-generated constructor stub
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_gather);

		mImageWorker = new ImageFetcher(mModel.getAppContext(), -1, -1);
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);

		View v = findViewById(R.id.nav_btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.edit_title);
		if (v != null) {
			((EditText) v).addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					if (!isUpdating) {
						if (mCross != null) {
							mCross.setTitle(s.toString());
							Log.d(TAG, "save title %s", mCross.getTitle());
						}
					}
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

		v = findViewById(R.id.edit_description);
		if (v != null) {
			((EditText) v).addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					if (!isUpdating) {
						if (mCross != null) {
							mCross.setDescription(s.toString());
							Log.d(TAG, "save title %s", mCross.getDescription());
						}
					}
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

		v = findViewById(R.id.exfee_root);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.x_rel_date);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}
		v = findViewById(R.id.x_time_date);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}
		v = findViewById(R.id.x_zone);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.x_addr_title);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}
		v = findViewById(R.id.x_addr_desc);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}
		v = findViewById(R.id.x_map);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		mCross = new Cross();
		mCross.setTitle(getResources().getString(R.string.meet_xxx,
				mModel.Me().getProfile().getName()));
		Identity ident = mModel.Me().getDefaultIdentity();
		mCross.setByIdentitiy(ident);
		if (mCross.getExfee() != null
				&& mCross.getExfee().getInvitations() != null) {
			Invitation inv = new Invitation();
			inv.setIdentity(ident);
			inv.setRsvpStatus(Rsvp.ACCEPTED);
			inv.setHost(true);
			inv.setByIdentity(ident);
			mCross.getExfee().getInvitations().add(inv);
		}

		showCross(mCross);
		// fillExfee(mCross.getExfee());

	}

	private void fillText(int id, CharSequence text, int visibility) {
		fillText(id, text, null, visibility);
	}

	private void fillText(int id, CharSequence text, CharSequence def) {
		fillText(id, text, def, View.VISIBLE);
	}

	private void fillText(int id, CharSequence text) {
		fillText(id, text, null, View.VISIBLE);
	}

	private void fillText(int id, CharSequence text, CharSequence def,
			int visibility) {
		View v = findViewById(id);
		if (v != null) {
			TextView tv = (TextView) v;
			if (TextUtils.isEmpty(text)) {
				tv.setText(def);
			} else {
				tv.setText(text);
			}
			if (tv.getVisibility() != visibility) {
				tv.setVisibility(visibility);
			}
		}
	}

	private void showCross(Cross cross) {
		if (cross != null) {
			isUpdating = true;
			// ImageView bgView = (ImageView)findViewById(
			// R.id.x_widget_background);
			//
			// Widget w = cross.getWidgetByCategory("Background");
			// if (w.getJson() != null) {
			// String pic = w.getJson().optString("image");
			// if (!TextUtils.isEmpty(pic)) {
			// String url = null;
			// if (pic.startsWith("http://") || pic.startsWith("https://")) {
			// url = pic;
			// } else {
			// String jpg = pic.toLowerCase();
			// if (!jpg.matches("^.+\\.(jpg|png|gif)$")) {
			// jpg = String.format("%s.jpg", jpg);
			// }
			// url = Const.getWidgetImgURL(jpg);
			// }
			// mImageWorker.loadImage(url, bgView);
			// }
			// }

			fillText(R.id.edit_title, cross.getTitle());

			fillText(R.id.x_attendee_count,
					String.valueOf(cross.getExfee().getAccepted()));
			fillText(R.id.x_attendee_all,
					String.format("/%d", cross.getExfee().getTotal()));

			if (cross.getTime() != null) {
				fillText(R.id.x_rel_date, cross.getTime().getBeginAt()
						.getRelativeStringFromNow(getResources()),
						getText(R.string.sometime));
				fillText(R.id.x_time_date, cross.getTime()
						.getLongLocalTimeSring(false, getResources()));

				if (Tool.isSameWithLocalZone(cross.getTime().getBeginAt()
						.getTimezone())) {
					fillText(R.id.x_zone, "", View.INVISIBLE);
				} else {
					fillText(R.id.x_zone, cross.getTime().getBeginAt()
							.getTimezone(), View.VISIBLE);
				}
			} else {
				fillText(R.id.x_rel_date, getText(R.string.sometime));
				fillText(R.id.x_time_date, "");
				fillText(R.id.x_zone, "", View.INVISIBLE);
			}

			if (cross.getPlace() != null) {
				fillText(R.id.x_addr_title, cross.getPlace().getTitle(),
						getText(R.string.somewhere));
				fillText(R.id.x_addr_desc, cross.getPlace().getDescription());
			} else {
				fillText(R.id.x_addr_title, getText(R.string.somewhere));
				fillText(R.id.x_addr_desc, "");
			}

			fillText(R.id.edit_description, cross.getDescription());

			fillAvatars(cross);

			ImageView mv = (ImageView) findViewById(R.id.x_map);
			View mvf = findViewById(R.id.x_map_frame);
			if (mv != null) {
				Place p = cross.getPlace();
				if (p != null && !TextUtils.isEmpty(p.getLat())
						&& !TextUtils.isEmpty(p.getLng())) {
					if (mvf != null) {
						mvf.setVisibility(View.VISIBLE);
					}
					String center = String.format("%s,%s", p.getLat(),
							p.getLng());

					String dimen = String.format("%dx%d",
							mv.getLayoutParams().width - mv.getPaddingLeft()
									- mv.getPaddingRight(),
							mv.getLayoutParams().height - mv.getPaddingTop()
									- mv.getPaddingBottom());
					String url = String
							.format("http://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=13&size=%s&sensor=false&scale=2",
									center, dimen);
					Log.d(TAG, "MAP: %s", url);
					mImageWorker.loadImage(url, mv);
				} else {
					if (mvf != null) {
						mvf.setVisibility(View.INVISIBLE);
					}
					mv.setImageResource(R.drawable.map_nil);
				}
			}
			isUpdating = false;
		}
	}

	private void fillAvatars(Cross cross) {
		TableLayout group = (TableLayout) findViewById(R.id.exfee_root);
		// group.removeAllViews();

		TableRow row = null;
		LayoutInflater inflater = getLayoutInflater();
		int column = 6;
		int i = 0;
		for (Invitation inv : cross.getExfee().getInvitations()) {
			if (inv.getRsvpStatus() == Rsvp.NOTIFICATION) {
				continue;
			}

			Log.d(TAG, "Fill user: %s %s", inv.getIdentity()
					.getExternalUsername(), inv.getIdentity().getExternalId());

			// start a now row if need.
			if (row == null) {
				int ri = i / column;
				if (group.getChildCount() > ri) {
					row = (TableRow) group.getChildAt(ri);
					if (row.getVisibility() != View.VISIBLE) {
						row.setVisibility(View.VISIBLE);
					}
				} else {
					row = new TableRow(group.getContext());

					TableLayout.LayoutParams params = new TableLayout.LayoutParams(
							TableLayout.LayoutParams.MATCH_PARENT,
							TableLayout.LayoutParams.WRAP_CONTENT);
					group.addView(row, params);
				}
			}

			Identity idet = inv.getIdentity();
			View vg = null;
			int ci = i % column;
			if (row.getChildCount() > ci) {
				vg = row.getChildAt(ci);
				if (vg.getVisibility() != View.VISIBLE) {
					vg.setVisibility(View.VISIBLE);
				}
			} else {
				vg = inflater.inflate(R.layout.comp_avatar, row, false);
				// vg.setOnClickListener(mAvatarClickListener);
				row.addView(vg);
			}

			vg.setTag(inv);
			int status = inv.getRsvpStatus();

			ImageView rsvpImage = (ImageView) vg
					.findViewById(R.id.x_exfer_rsvp);
			if (rsvpImage != null) {
				rsvpImage.setImageLevel(status);
			}

			ImageView iv = (ImageView) vg.findViewById(R.id.x_exfer_icon);
			mImageWorker.loadImage(idet.getAvatarFilename(), iv);
			if (status != Rsvp.ACCEPTED) {
				iv.setAlpha(96);
			} else {
				iv.setAlpha(255);
			}

			TextView tv = (TextView) vg.findViewById(R.id.x_exfer_name);
			tv.setText(idet.getName());

			ImageView frameImage = (ImageView) vg
					.findViewById(R.id.x_exfer_cover);
			if (frameImage != null) {
				int matesLevel = inv.getMates();
				if (matesLevel == 0 && inv.isHost()) {
					matesLevel = 10000;
				}
				frameImage.setImageLevel(matesLevel);
			}

			vg.setTag(R.id.field_attached, i);

			if (inv.getIdentity().getConnectedUserId() == getModel().Me()
					.getUserId()) {
				// mMyInvitation = inv;
			}

			i++;
			// last item. need new line
			if (i % column == 0) {
				row = null;
			}
		}

		int ri = i / column + 1;
		while (ri < group.getChildCount()) {
			View r = group.getChildAt(ri);
			if (r.getVisibility() != View.GONE) {
				r.setVisibility(View.GONE);
			}
		}
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
			case R.id.nav_btn_back:
				setResult(Activity.RESULT_CANCELED, data);
				finish();
				break;
			case R.id.nav_btn_action:
				if (mCross != null) {

					Runnable run = new Runnable() {

						@Override
						public void run() {
							Response result = mModel.getServer().addCross(
									mCross);

							if (result != null) {
								int code = result.getCode();
								switch (code) {
								case HttpStatus.SC_OK:
									JSONObject resp = result.getResponse();
									final JSONObject cross = resp
											.optJSONObject("cross");
									if (cross != null) {
										Cross x = (Cross) EntityFactory
												.create(cross);
										if (x != null) {
											final long cross_id = x.getId();

											mModel.Crosses().addCross(x);

											mModel.mHandler
													.post(new Runnable() {

														@Override
														public void run() {
															Intent data = new Intent();
															data.putExtra(
																	"cross_id",
																	cross_id);
															setResult(
																	Activity.RESULT_OK,
																	data);
															finish();
														}
													});
										}
									}
									break;
								case HttpStatus.SC_NOT_FOUND:
									break;
								default:
									break;
								}
							}

						}
					};
					// Log.d(TAG, "Cross: %s", mCross);
					new Thread(run).start();

				}
				break;
			case R.id.exfee_root:
				data.setClass(GatherActivity.this, SearchExfeeActivity.class);
				startActivityForResult(data, GATHER_EXFEE_REQUEST);
				break;
			case R.id.x_rel_date:
			case R.id.x_time_date:
			case R.id.x_zone:
				data.setClass(GatherActivity.this, SetTimeActivity.class);
				if (mCross != null && mCross.getTime() != null) {
					data.putExtra(SetTimeActivity.RESULT_FIELD_CROSS_TIME,
							mCross.getTime().toJSON().toString());
				}
				startActivityForResult(data, GATHER_TIME_REQUEST);
				break;
			case R.id.x_addr_title:
			case R.id.x_addr_desc:
			case R.id.x_map:
				data.setClass(GatherActivity.this, SearchPlaceActivity.class);
				if (mCross != null && mCross.getPlace() != null) {
					data.putExtra(SearchPlaceActivity.RESULT_FIELD_PLACE,
							mCross.getPlace().toJSON().toString());
				}
				startActivityForResult(data, GATHER_PLACE_REQUEST);
				break;
			default:
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case GATHER_EXFEE_REQUEST:
				Log.d(TAG, "add identity");
				if (data != null) {
					String abc = data
							.getStringExtra(SearchExfeeActivity.RESULT_FIELD_IDENTITY);
					if (!TextUtils.isEmpty(abc) && Tool.isJson(abc)) {
						JSONObject json = null;
						try {
							json = new JSONObject(abc);
							Identity ident = (Identity) EntityFactory
									.create(json);
							Log.d(TAG, "Select identity: %s", ident.getName());
							// add identity
							if (mCross != null
									&& mCross.getExfee() != null
									&& mCross.getExfee().getInvitations() != null) {
								Invitation inv = new Invitation();
								inv.setIdentity(ident);
								inv.setRsvpStatus(Rsvp.NORESPONSE);
								mCross.getExfee().getInvitations().add(inv);
								showCross(mCross);
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
				break;
			case GATHER_TIME_REQUEST:
				Log.d(TAG, "select time");
				if (data != null) {
					String abc = data
							.getStringExtra(SetTimeActivity.RESULT_FIELD_CROSS_TIME);
					if (!TextUtils.isEmpty(abc) && Tool.isJson(abc)) {
						JSONObject json = null;
						try {
							json = new JSONObject(abc);
							CrossTime ct = (CrossTime) EntityFactory
									.create(json);
							Log.d(TAG, "Select time: %s", ct.getOrigin());
							// add place
							if (mCross != null) {
								mCross.setTime(ct);
								showCross(mCross);
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				break;
			case GATHER_PLACE_REQUEST:
				Log.d(TAG, "select place");
				if (data != null) {
					String abc = data
							.getStringExtra(SearchPlaceActivity.RESULT_FIELD_PLACE);
					if (!TextUtils.isEmpty(abc) && Tool.isJson(abc)) {
						JSONObject json = null;
						try {
							json = new JSONObject(abc);
							Place place = (Place) EntityFactory.create(json);
							Log.d(TAG, "Select place: %s", place.getTitle());
							// add place
							if (mCross != null) {
								mCross.setPlace(place);
								showCross(mCross);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}
