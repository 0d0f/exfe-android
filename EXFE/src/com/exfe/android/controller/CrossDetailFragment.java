package com.exfe.android.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Const;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.CrossesModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Place;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.Rsvp;
import com.exfe.android.model.entity.Widget;
import com.exfe.android.util.Tool;
import com.exfe.android.view.CountControls;

public class CrossDetailFragment extends Fragment implements Observer {

	public static final String FIELD_CROSS_ID = "cross_id";

	private DisplayMetrics mDm = null;
	private long mCrossId = 0;
	private Cross mCross = null;
	private Invitation mMyInvitation = null;
	// private long mIdentityId = 0;
	private View mPopupView = null;
	private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

	private ViewGroup mToolBar = null;
	// private List<Invitation> mSelectedIds = new ArrayList<Invitation>();
	// for mutiple choice.
	private Invitation mSelectedInv = null;
	private View mSelectedView = null;

	private ImageWorker mImageWorker = null;

	private Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);

		mImageWorker = new ImageFetcher(mModel.getAppContext(), -1, -1);
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
		// mImageWorker.setLoadingImage(resId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
		View v = inflater.inflate(R.layout.fragment_cross_detail, container,
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

		mCrossId = getArguments().getLong(FIELD_CROSS_ID, 0);
		if (mCrossId > 0) {
			mCross = mModel.Crosses().getCrossById(mCrossId);
		} else {
			mCross = null;
		}

		mDm = getResources().getDisplayMetrics();

		View v = null;

		v = getActivity().findViewById(R.id.x_rsvp_action_bar);
		if (v != null) {
			mToolBar = (ViewGroup) v;

			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click...");
				}
			});
		}

		v = getActivity().findViewById(R.id.x_widget_background);
		if (v != null) {
			final ImageView bgView = (ImageView) v;

			int dwidth = 880;
			int dheight = 495;
			int vwidth = mDm.widthPixels;
			int vheight = (int) (108 * mDm.density);

			Matrix mDrawMatrix = new Matrix();

			float scale;
			float dx = 0, dy = 0;

			if (dwidth * vheight > vwidth * dheight) {
				scale = (float) vheight / (float) dheight;
				dx = (vwidth - dwidth * scale) * 0.5f;
			} else {
				scale = (float) vwidth / (float) dwidth;
				dy = (vheight - dheight * scale) * 1.0f;
			}

			mDrawMatrix.setScale(scale, scale);
			mDrawMatrix.postTranslate(dx, dy);
			bgView.setScaleType(ScaleType.MATRIX);
			bgView.setImageMatrix(mDrawMatrix);
		}

		// bar me
		v = getActivity().findViewById(R.id.x_rsvp_accept);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}
		v = getActivity().findViewById(R.id.x_rsvp_interested);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}
		v = getActivity().findViewById(R.id.x_rsvp_decline);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}

		// bar others
		v = getActivity().findViewById(R.id.x_rsvp_others_accept);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}
		v = getActivity().findViewById(R.id.x_rsvp_others_ignore);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}
		v = getActivity().findViewById(R.id.x_rsvp_others_decline);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}
		v = getActivity().findViewById(R.id.x_rsvp_others_change_mate);
		if (v != null) {
			CountControls cc = (CountControls) v;
			cc.setOnZoomInClickListener(mToolBarClickListener);
			cc.setOnZoomOutClickListener(mToolBarClickListener);
		}

		// bar others
		v = getActivity().findViewById(R.id.x_rsvp_current_selection);
		if (v != null) {
			v.setOnClickListener(mToolBarClickListener);
		}

		showCross(mCross);

		showToolBar();
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
		View v = getActivity().findViewById(id);
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

			ImageView bgView = (ImageView) getActivity().findViewById(
					R.id.x_widget_background);

			Widget w = cross.getWidgetByCategory("Background");
			if (w.getJson() != null) {
				String pic = w.getJson().optString("image");
				if (!TextUtils.isEmpty(pic)) {
					String url = null;
					if (pic.startsWith("http://") || pic.startsWith("https://")) {
						url = pic;
					} else {
						String jpg = pic.toLowerCase();
						if (!jpg.matches("^.+\\.(jpg|png|gif)$")) {
							jpg = String.format("%s.jpg", jpg);
						}
						url = Const.getWidgetImgURL(jpg);
					}
					mImageWorker.loadImage(url, bgView);
				}
			}

			fillText(R.id.x_title, cross.getTitle());

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

			fillText(R.id.x_description, cross.getDescription());

			fillAvatars(cross);

			ImageView mv = (ImageView) getActivity().findViewById(R.id.x_map);
			View mvf = getActivity().findViewById(R.id.x_map_frame);
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
		}

	}

	private void fillAvatars(Cross cross) {
		TableLayout group = (TableLayout) getActivity().findViewById(
				R.id.x_exfee);
		// group.removeAllViews();

		TableRow row = null;
		LayoutInflater inflater = getActivity().getLayoutInflater();
		int column = 6;
		int i = 0;
		for (Invitation inv : cross.getExfee().getInvitations()) {
			if (inv.getRsvpStatus() == Rsvp.NOTIFICATION) {
				continue;
			}

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
				vg.setOnClickListener(mAvatarClickListener);
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
				mMyInvitation = inv;
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

	private void showToolBar() {
		if (mMyInvitation != null && mToolBar != null) {

			if (!isSelectedInvEmpty()) {
				switchToolBar(R.id.x_rsvp_action_bar_others);
				return;
			}

			int rsvp = mMyInvitation.getRsvpStatus();

			switch (rsvp) {
			case Rsvp.ACCEPTED:
			case Rsvp.DECLINED:
			case Rsvp.INTERESTED:
				switchToolBar(R.id.x_rsvp_current_selection);
				ImageView v = (ImageView) mToolBar
						.findViewById(R.id.x_rsvp_current_selection);
				// v.getCompoundDrawables()[1].setLevel(rsvp);
				v.setImageLevel(rsvp);
				break;
			default:
				switchToolBar(R.id.x_rsvp_action_bar_me);
				break;
			}
		}
	}

	private void switchToolBar(int id) {
		int i = 0;
		int c = mToolBar.getChildCount();
		if (mToolBar != null) {

			for (i = 0; i < c; i++) {
				View v = mToolBar.getChildAt(i);
				if (v.getId() == id) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	private boolean isSelectedInvEmpty() {
		return mSelectedInv == null;
	}

	private void addInv(Invitation inv, View view) {
		mSelectedInv = inv;
		if (mSelectedView != null) {
			mSelectedView.setSelected(false);
			ImageView rsvpImage = (ImageView) mSelectedView
					.findViewById(R.id.x_exfer_rsvp);
			if (rsvpImage != null) {
				rsvpImage.setVisibility(View.INVISIBLE);
			}
		}
		mSelectedView = view;
		mSelectedView.setSelected(true);

		ImageView rsvpImage = (ImageView) mSelectedView
				.findViewById(R.id.x_exfer_rsvp);
		if (rsvpImage != null) {
			rsvpImage.setVisibility(View.VISIBLE);
		}
	}

	private boolean containInv(Invitation inv) {
		return mSelectedInv != null && inv != null
				&& mSelectedInv.getId() == inv.getId();
	}

	private boolean removeInv(Invitation inv) {
		if (mSelectedView != null) {
			mSelectedView.setSelected(false);
			ImageView rsvpImage = (ImageView) mSelectedView
					.findViewById(R.id.x_exfer_rsvp);
			if (rsvpImage != null) {
				rsvpImage.setVisibility(View.INVISIBLE);
			}
		}
		mSelectedInv = null;
		mSelectedView = null;
		return true;
	}

	private boolean updateInv(int rsvp) {
		if (mSelectedInv != null) {
			final int old_rsvp = mSelectedInv.getRsvpStatus();
			final Invitation inv_temp = mSelectedInv.cloneSelf();
			submitRsvp(inv_temp, rsvp);
			return true;
		}
		return false;
	}

	private boolean updateMyInv(int rsvp) {
		if (mMyInvitation != null) {
			final int old_rsvp = mMyInvitation.getRsvpStatus();
			submitRsvp(mMyInvitation, rsvp);
			return true;
		}
		return false;
	}

	private boolean increaseMates() {
		if (mSelectedInv != null) {
			int m = mSelectedInv.getMates();
			if (m < 9) {
				final int newM = m + 1;
				final Exfee exfee = mCross.getExfee().cloneSelf();
				submitMates(exfee, newM);
				return true;
			}
		}
		return false;
	}

	private boolean decreaseMates() {
		if (mSelectedInv != null) {
			int m = mSelectedInv.getMates();
			if (m > 0) {
				final int newM = m - 1;
				final Exfee exfee = mCross.getExfee().cloneSelf();
				submitMates(exfee, newM);
				return true;
			}
		}
		return false;
	}

	private void submitRsvp(final Invitation inv, final int rsvp) {
		Runnable run = new Runnable() {

			@Override
			public void run() {

				inv.setRsvpStatus(rsvp);
				List<Rsvp> rsvps = new ArrayList<Rsvp>();
				Rsvp rsvp = inv.getRsvpObject();
				rsvp.setByIdentity(mMyInvitation.getIdentity());
				rsvps.add(rsvp);
				// submit the change to server
				Response res = mModel.getServer().updateRSVP(
						mCross.getExfee().getId(), rsvps);
				if (res.getCode() != HttpStatus.SC_OK) {
					Log.w(TAG, "Submit fail");
					return;
				}

				// update the local cache
				JSONArray array = res.getResponse().optJSONArray("rsvp");
				if (array == null || array.length() == 0) {
					return;
				}

				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject json = array.getJSONObject(i);
						Rsvp r = (Rsvp) EntityFactory.create(json);
						r.loadFromDao(mModel.getHelper());
						for (Invitation in : mCross.getExfee().getInvitations()) {
							if (in.getIdentity().getId() == r.getIdentity()
									.getId()) {
								in.setByIdentity(r.getByIdentity());
								in.setRsvpStatus(r.getRsvpStatus());
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mCross.saveToDao(mModel.getHelper());

				int c_count = 0;
				int a_count = 0;
				for (Invitation i : mCross.getExfee().getInvitations()) {
					int p = i.getMates() + 1;
					if (i.getRsvpStatus() == Rsvp.ACCEPTED) {
						c_count += p;
					}
					a_count += p;
				}
				final int confirmed_count = c_count;
				final int all_count = a_count;

				if (mHandler != null) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							fillText(R.id.x_attendee_count,
									String.valueOf(confirmed_count));
							fillText(R.id.x_attendee_all,
									String.format("/%d", all_count));
							showDropdown(getAnchorView());
							fillAvatars(mCross);
							showToolBar();
						}
					});

				}
			}
		};
		new Thread(run).start();
	}

	private void submitMates(final Exfee exfee, final int mates) {
		Runnable run = new Runnable() {

			@Override
			public void run() {

				for (Invitation inv : exfee.getInvitations()) {
					if (inv.getId() == mSelectedInv.getId()) {
						inv.setMates(mates);
					}
				}
				Response resp = mModel.getServer().editExfee(
						mMyInvitation.getIdentity(), exfee);
				if (resp.getCode() != HttpStatus.SC_OK) {
					Log.w(TAG, "Submit fail");
					return;
				}

				Exfee newExfee = (Exfee) EntityFactory.create(resp
						.getResponse().optJSONObject("exfee"));
				mSelectedInv.setMates(mates);

				newExfee.saveToDao(mModel.getHelper());

				if (mSelectedView != null) {
					mSelectedView.post(new Runnable() {

						@Override
						public void run() {
							showDropdown(getAnchorView());
						}
					});

				}

			}
		};
		new Thread(run).start();
	}

	private ViewGroup getAnchorView() {
		return (ViewGroup) mSelectedView;
	}

	private View.OnClickListener mAvatarClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Object obj = v.getTag();
			if (obj != null) {
				Invitation inv = (Invitation) obj;
				boolean selected = containInv(inv);
				Log.d(TAG, "avatar clicked: %s", inv.getId());
				if (selected) {
					removeInv(inv);
				} else {
					addInv(inv, v);
				}
				if (!isSelectedInvEmpty()) {
					showDropdown(getAnchorView());
				} else {
					hideDropdown(getAnchorView());
				}
				showToolBar();
			}
		}
	};

	protected void showDropdown(final View v) {

		if (v == null) {
			return;
		}

		if (mPopupView == null) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			mPopupView = inflater.inflate(R.layout.avatar_popup, null);
			SparseArray<TextView> holder = new SparseArray<TextView>();
			holder.append(R.id.textView1,
					(TextView) mPopupView.findViewById(R.id.textView1));
			holder.append(R.id.textView2,
					(TextView) mPopupView.findViewById(R.id.textView2));
			holder.append(R.id.textView3,
					(TextView) mPopupView.findViewById(R.id.textView3));
			mPopupView.setTag(holder);
		}
		hideDropdown(v);
		@SuppressWarnings("unchecked")
		SparseArray<TextView> holder = (SparseArray<TextView>) mPopupView
				.getTag();
		Invitation inv = (Invitation) v.getTag();
		Resources res = getResources();
		if (inv.isHost()) {
			String str = res.getString(R.string.two_sentences, res
					.getString(R.string.host), res.getString(Rsvp
					.getRsvpStatusResourceId(inv.getRsvpStatus())));
			holder.get(R.id.textView1).setText(str);
		} else {
			String str = null;
			int mates = inv.getMates();
			if (mates > 0) {
				str = res.getQuantityString(R.plurals.rsvp_with_mates, mates,
						res.getString(Rsvp.getRsvpStatusResourceId(inv
								.getRsvpStatus())), mates);
			} else {
				str = res.getString(R.string.one_sentence, res.getString(Rsvp
						.getRsvpStatusResourceId(inv.getRsvpStatus())));
			}
			holder.get(R.id.textView1).setText(Html.fromHtml(str));
		}

		if (inv.getIdentity() != null) {
			Identity ident = inv.getIdentity();
			TextView tv = holder.get(R.id.textView2);
			int provider = Provider.getValue(ident.getProvider());
			tv.getCompoundDrawables()[0].setLevel(provider);
			if (provider == Provider.TWITTER) {
				tv.setText(String.format("@%s", inv.getIdentity()
						.getExternalUsername()));
			} else {
				tv.setText(inv.getIdentity().getExternalUsername());
			}
			tv.requestLayout();
		}

		if (inv.getByIdentity() != null) {
			TextView tv = holder.get(R.id.textView3);
			if (inv.getIdentity() != null
					&& inv.getByIdentity().getConnectedUserId() == inv
							.getIdentity().getConnectedUserId()) {
				tv.setText(getResources().getString(R.string.one_sentence,
						Tool.getXRelativeString(inv.getUpdateAt(), res)));
			} else {
				tv.setText(getResources().getString(R.string.xxx_by_xxx,
						Tool.getXRelativeString(inv.getUpdateAt(), res),
						inv.getByIdentity().getName()));
			}
		}

		int[] location = new int[2];
		v.getLocationOnScreen(location);

		Object tag = v.getTag(R.id.field_attached);
		int type = 0;
		if (tag != null) {
			int index = (Integer) tag;
			type = index / 3 % 2;
		}

		final WindowManager.LayoutParams params = mParams;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_TOAST;
		params.setTitle("Toast");

		if (type == 0) {
			params.gravity = Gravity.TOP | Gravity.LEFT;
			params.x = location[0];
			params.y = location[1] - (int) (100 * mDm.density);
		} else {
			params.gravity = Gravity.TOP | Gravity.RIGHT;
			params.x = mDm.widthPixels - location[0] - v.getWidth();
			params.y = location[1] - (int) (100 * mDm.density);
		}
		mPopupView.getBackground().setLevel(type);
		getActivity().getWindowManager().addView(mPopupView, params);
		mPopupView.setTag(R.id.field_attached, Boolean.TRUE);
		showForSeconds(mPopupView, 6);
	}

	protected void hideDropdown(View v) {
		if (mPopupView != null) {
			Object obj = mPopupView.getTag(R.id.field_attached);
			if (obj != null && (Boolean) obj == true) {
				getActivity().getWindowManager().removeView(mPopupView);
				mPopupView.setTag(R.id.field_attached, Boolean.FALSE);
			}
		}
	}

	protected void showForSeconds(final View v, final int seconds) {
		if (v != null) {
			// v.setVisibility(View.VISIBLE);
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
										(long) (seconds * 1000 * 0.1));
								return;
							}
						}

						// if (v.getVisibility() == View.VISIBLE) {
						// v.setVisibility(View.INVISIBLE);
						hideDropdown(null);
						v.setTag(R.id.field_time_out, null);
						// }
					}
				}, (long) (seconds * 1000 * 0.8));
			}
		}
	}

	private View.OnClickListener mToolBarClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			Log.d(TAG, "Toobar button %d clicked.", id);
			int rsvp = -1;

			switch (id) {
			case R.id.x_rsvp_current_selection:
				switchToolBar(R.id.x_rsvp_action_bar_me);
				break;
			case R.id.x_rsvp_accept:
				if (rsvp == -1) {
					rsvp = Rsvp.ACCEPTED;
				}
			case R.id.x_rsvp_decline:
				if (rsvp == -1) {
					rsvp = Rsvp.DECLINED;
				}
			case R.id.x_rsvp_interested:
				if (rsvp == -1) {
					rsvp = Rsvp.INTERESTED;
				}
				if (rsvp != -1) {
					updateMyInv(rsvp);
				}
				break;
			case R.id.x_rsvp_others_accept:
				if (rsvp == -1) {
					rsvp = Rsvp.ACCEPTED;
				}
			case R.id.x_rsvp_others_decline:
				if (rsvp == -1) {
					rsvp = Rsvp.DECLINED;
				}
			case R.id.x_rsvp_others_ignore:
				if (rsvp == -1) {
					rsvp = Rsvp.NORESPONSE;
				}
				// update others rsvp status
				if (rsvp != -1) {
					updateInv(rsvp);
				}
				break;
			// R.id.x_rsvp_others_change_mate
			case R.id.zoom_in:
				increaseMates();
				// fillAvatars(mCross);
				break;
			case R.id.zoom_out:
				decreaseMates();
				// fillAvatars(mCross);
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mModel.Crosses().updateLastView(mCross);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		hideDropdown(getAnchorView());
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {
		Bundle bundle = (Bundle) data;
		int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
		switch (type) {
		case CrossesModel.ACTION_TYPE_UPDATE_CROSSES:
			mModel.mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (mCross == null && mCrossId != 0) {
						mCross = mModel.Crosses().getCrossById(mCrossId);
						if (mCross != null) {
							showCross(mCross);
							showToolBar();
						}
					}
				}
			});
			break;
		}

	}

}
