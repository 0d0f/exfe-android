package com.exfe.android.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.entity.CrossTime;
import com.exfe.android.model.entity.EFTime;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.exfe.android.util.Tool;
import com.exfe.android.view.OneDaySelector;

public class SetTimeActivity extends Activity implements Observer {

	public static final int GAHTER_ID = 41234;

	public static final String RESULT_FIELD_CROSS_TIME = "cross_time";

	private DateFormat fmt_MMMM_yyyy;

	private TextView mMonthText = null;
	private EditText mInputTime = null;
	private ListView mDateSelector = null;
	private OneDaySelector mTimeSelector = null;
	private DateAdapter mAdapter = null;
	private Calendar mBaseDate;
	private Calendar mSelectedDate;
	private Calendar mSelectedTime;
	private Calendar mNow = Calendar.getInstance();

	private boolean isFromSelect = false;
	private boolean isUserInput = false;

	public SetTimeActivity() {
		// TODO Auto-generated constructor stub
	}

	protected void setSelectedDate(Calendar date) {

		if (mSelectedDate == null) {
			mSelectedDate = (Calendar) date.clone();
		}
		mSelectedDate.clear();
		if (date != null && Tool.hasDate(date)) {
			mSelectedDate.set(Calendar.YEAR, date.get(Calendar.YEAR));
			mSelectedDate.set(Calendar.MONTH, date.get(Calendar.MONTH));
			mSelectedDate.set(Calendar.DATE, date.get(Calendar.DATE));

			// update basedate
			mBaseDate = mSelectedDate;

			// update month
			updateMonth(mSelectedDate);
			// update date selector
			if (mAdapter != null) {
				mAdapter.notifyDataSetInvalidated();
			}
			// notification
			if (mTimeSelector != null) {
				Log.d(TAG, "setSelectedDate, sync mTimeSelector: %s",
						mSelectedDate.getTime());
				mTimeSelector.setCurrentDate(mSelectedDate);
				setSelectedTime(null);
			}
		}
	}

	protected Calendar getSelectedDate() {
		return mSelectedDate;
	}

	protected void setSelectedTime(Calendar time) {
		if (mSelectedTime == null) {
			mSelectedTime = Calendar.getInstance();
		}
		mSelectedTime.clear();
		if (time != null && Tool.hasTime(time)) {
			mSelectedTime.set(Calendar.HOUR, time.get(Calendar.HOUR));
			mSelectedTime.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
			mSelectedTime.set(Calendar.SECOND, time.get(Calendar.MINUTE));
			mSelectedTime.set(Calendar.MILLISECOND,
					time.get(Calendar.MILLISECOND));
			// notification

		}
	}

	protected Calendar getSeletedTime() {
		return mSelectedTime;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_set_time);

		mNow.setTimeInMillis(System.currentTimeMillis());

		fmt_MMMM_yyyy = new SimpleDateFormat(getResources().getString(
				R.string.full_month_and_year));

		Intent it = getIntent();

		CrossTime ct = (CrossTime) EntityFactory.create(it
				.getStringExtra(SetTimeActivity.RESULT_FIELD_CROSS_TIME));

		View v = findViewById(R.id.btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.input_time);
		if (v != null) {
			mInputTime = (EditText) v;
			mInputTime.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (!isFromSelect) {
						isUserInput = true;
					}
				}
			});
		}

		v = findViewById(R.id.month);
		if (v != null) {
			mMonthText = (TextView) v;
		}

		v = findViewById(R.id.time_selector);
		if (v != null) {
			mTimeSelector = (OneDaySelector) v;
			mTimeSelector
					.setDateChangeListener(new OneDaySelector.DateChangeListener() {

						@Override
						public void nextDay(Calendar date) {
							// TODO Auto-generated method stub

						}

						@Override
						public void previousDay(Calendar date) {
							// TODO Auto-generated method stub

						}

						@Override
						public void selectDay(Calendar date) {
							Log.d(TAG, "select day: %s ", date.getTime());
							setSelectedTime(date);
							if (date.get(Calendar.YEAR) != mNow
									.get(Calendar.YEAR)) {
								updateInput(date, Const.LOCAL_TIME_DATE_FORMAT);
							} else {
								updateInput(date,
										Const.LOCAL_TIME_DATE_MMM_DD_FORMAT);
							}
						}
					});

		}

		v = findViewById(R.id.time_top_layer);
		if (v != null) {
			v.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					v.post(gotoToday);
					return true;
				}
			});
		}

		mDateSelector = (ListView) findViewById(R.id.date_selector);

		Calendar cal = null;

		if (ct != null) {
			EFTime eftime = ct.getBeginAt();
			if (eftime != null) {
				int type = eftime.getDateTimeType();
				Date target = eftime.getLocalDateTime();

				if (target != null) {
					Log.d(TAG, "1.3. target  %s  ", target);
					cal = Calendar.getInstance();
					cal.setTime(target);
					mBaseDate = cal;
					if (type == EFTime.DATETIME_TYPE_DATE_ONLY) {
						Tool.clearTime(cal);
					}
					setSelectedDate(cal);
					if (type == EFTime.DATETIME_TYPE_FULL) {
						if (mTimeSelector != null) {
							mTimeSelector.setCurrentDate(cal);
							setSelectedTime(mBaseDate);
						}
					}
					Log.d(TAG, "1.3. mBaseDate  %s  ", mBaseDate.getTime());
					Log.d(TAG, "1.3. selectedDate  %s  ",
							mSelectedDate.getTime());
				}

			}
			if (mInputTime != null) {
				mInputTime.setText(ct.getOrigin());
			}

		}

		if (cal == null) {
			cal = Calendar.getInstance();
			mBaseDate = (Calendar) cal.clone();
			mBaseDate.clear();
			mBaseDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE));
			Log.d(TAG, "1.1.1. mBaseDate  %s  ", mBaseDate.getTime());

			Calendar selcal = (Calendar) cal.clone();
			selcal.clear();
			setSelectedDate(selcal);
		}
		Log.d(TAG, " mBaseDate == mSelectedDate %b  ",
				mBaseDate == mSelectedDate);

		if (mMonthText != null) {
			if (Tool.hasDate(mSelectedDate)) {
				Log.d(TAG, "2.1. mBaseDate  %s  ", mBaseDate.getTime());
				Log.d(TAG, "2.1. mSelectedDate  %s  ", mSelectedDate.getTime());
				updateMonth(mSelectedDate);
			} else {
				Log.d(TAG, "2.2. mBaseDate  %s  ", mBaseDate.getTime());
				Log.d(TAG, "2.2. mSelectedDate  %s  ", mSelectedDate.getTime());
				updateMonth(mBaseDate);
			}
		}

		mAdapter = new DateAdapter(this, R.layout.listitem_rich_date);
		mDateSelector.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mDateSelector.setAdapter(mAdapter);
		mDateSelector.post(gotoToday);
		mDateSelector.setOnItemClickListener(mListener);
		mDateSelector.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mAdapter != null) {
					Calendar mid = mAdapter.getItem(firstVisibleItem
							+ visibleItemCount / 2);
					updateMonth(mid);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}
		});
	}

	private Runnable gotoToday = new Runnable() {

		@Override
		public void run() {
			if (mDateSelector != null) {
				int first = mDateSelector.getFirstVisiblePosition();
				int last = mDateSelector.getLastVisiblePosition();
				mDateSelector.setSelection(Integer.MAX_VALUE / 2
						- (last - first) / 2);
			}
		}
	};

	private void updateMonth(Calendar date) {
		Log.d(TAG, "update month...");
		if (mMonthText != null) {
			if (date != null) {
				Calendar cal = (Calendar) date.clone();
				Log.d(TAG, "update month: %s", cal.getTime());
				mMonthText.setText(fmt_MMMM_yyyy.format(cal.getTime()));
				Log.d(TAG, "updated month: %s", mMonthText.getText().toString());
			}
		}
	}

	private void updateInput(Calendar date, DateFormat fmt) {
		Log.d(TAG, "update input time...");
		if (mInputTime != null) {
			if (date != null) {
				Log.d(TAG, "update input time: %s", fmt.format(date.getTime()));
				isFromSelect = true;
				isUserInput = false;
				mInputTime.setText(fmt.format(date.getTime()));
				isFromSelect = false;
			}
		}
	}

	AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
			// TODO Auto-generated method stub
			DateAdapter adapter = (DateAdapter) list.getAdapter();
			Calendar cal = adapter.getItem(pos);

			setSelectedDate(cal);
			Tool.clearTime(cal);
			if (cal.get(Calendar.YEAR) != mNow.get(Calendar.YEAR)) {
				updateInput(cal, Const.LOCAL_DATE_FORMAT);
			} else {
				updateInput(cal, Const.LOCAL_DATE_MMM_DD_FORMAT);
			}
		}

	};

	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	private void saveResultAndFinish(String abc) {
		Intent data = new Intent();
		data.putExtra(RESULT_FIELD_CROSS_TIME, abc);
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();

			switch (id) {
			case R.id.btn_action:
				if (mInputTime != null) {
					final String raw = mInputTime.getText().toString().trim();
					if (TextUtils.isEmpty(raw)) {
						saveResultAndFinish(null);
					} else {
						// isUserInput
						Runnable run = new Runnable() {

							@Override
							public void run() {
								Response result = mModel.getServer()
										.formatTime(raw);

								hideProgressBar();
								if (result != null) {
									int code = result.getCode();
									switch (code) {
									case HttpStatus.SC_OK:
										JSONObject resp = result.getResponse();
										final JSONObject time = resp
												.optJSONObject("cross_time");
										if (time != null) {
											CrossTime ct = (CrossTime) EntityFactory
													.create(time);
											if (ct != null) {
												mModel.mHandler
														.post(new Runnable() {

															@Override
															public void run() {

																saveResultAndFinish(time
																		.toString());
															}
														});
											}
										}
										break;
									case HttpStatus.SC_NOT_FOUND:
										showToast("API not found.");
										break;
									default:
										showToast("Unhandled error.");
										break;
									}
								} else {
									showToast("Bad response.");
								}
							}
						};
						showProgressBar("Parsing", "Analyzing time text...");
						new Thread(run).start();
					}
				}
				break;
			case R.id.btn_back:
				setResult(Activity.RESULT_CANCELED, null);
				finish();
				break;
			default:
				break;
			}
		}

	};

	class DateAdapter extends BaseAdapter {

		private static final long ONE_MINUTE = 60 * 1000;
		private static final long ONE_HOUR = 60 * ONE_MINUTE;
		private static final long ONE_DAY = 24 * ONE_HOUR;

		private static final int MAX_CACHE_SIZE = 200;

		private SimpleDateFormat sDD_FORMATTER = new SimpleDateFormat("dd");
		private SimpleDateFormat sWEEKDAY_FORMATTER = new SimpleDateFormat(
				"EEEE");

		private int mBasePosition;
		private int mAllCount;
		private SparseArray<Calendar> mDateCache = new SparseArray<Calendar>();
		private int mResource;
		private Context mContext;
		private LayoutInflater mInflater;

		public DateAdapter(Context c, int resource) {
			mContext = c;
			mInflater = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResource = resource;
			mBasePosition = Integer.MAX_VALUE / 2;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Integer.MAX_VALUE;
		}

		@Override
		public Calendar getItem(int position) {
			if (mDateCache.indexOfKey(position) >= 0) {
				Calendar d = mDateCache.get(position);
				return d;
			} else {
				Calendar d = Calendar.getInstance();
				Tool.clearTime(d);
				d.add(Calendar.DATE, position - mBasePosition);
				if (mDateCache.size() > MAX_CACHE_SIZE) {
					mDateCache.clear();
				}
				mDateCache.put(position, d);
				return d;
			}
		}

		@Override
		public long getItemId(int position) {
			return position - mBasePosition;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (!mDataValid) {
			// throw new
			// IllegalStateException("this should only be called when the cursor is valid");
			// }

			View v;
			if (convertView == null) {
				v = newView(mContext, position, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, position);
			return v;
		}

		public View newView(Context context, int position, ViewGroup parent) {
			View v = mInflater.inflate(mResource, parent, false);
			SparseArray<View> holder = new SparseArray<View>();
			holder.put(R.id.text1, v.findViewById(R.id.text1));
			holder.put(R.id.text2, v.findViewById(R.id.text2));
			holder.put(R.id.root, v.findViewById(R.id.root));
			v.setTag(holder);
			return v;
		}

		public void bindView(View view, Context context, int position) {
			SparseArray<View> holder = (SparseArray<View>) view.getTag();
			TextView tv1 = (TextView) holder.get(R.id.text1);
			TextView tv2 = (TextView) holder.get(R.id.text2);
			View root = holder.get(R.id.root);
			Calendar cal = getItem(position);
			boolean selected = false;

			if (getSelectedDate() != null) {
				if (Tool.isSameDay(getSelectedDate(), cal)) {
					selected = true;
				}
			}

			if (tv1 != null) {
				if (Tool.isSameDay(cal, mNow)) {
					tv1.setText(R.string.today);
				} else {
					tv1.setText(sDD_FORMATTER.format(cal.getTime()));
				}
				tv1.setSelected(selected);
			}
			if (tv2 != null) {
				tv2.setText(sWEEKDAY_FORMATTER.format(cal.getTime()));
				tv2.setSelected(selected);
			}
			if (root != null) {
				root.setBackgroundColor(mContext.getResources().getColor(
						selected ? R.color.rich_time_background
								: R.color.rich_date_background));
			}
		}

	}

}
