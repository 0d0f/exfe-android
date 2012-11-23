package com.exfe.android.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.exfe.android.debug.Log;
import com.exfe.android.util.Tool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.GetChars;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class OneDaySelector extends View {
	
	protected final String TAG = getClass().getSimpleName();

	private static final long ONE_MINUTE = 60 * 1000;
	private static final long ONE_HOUR = 60 * ONE_MINUTE;
	private static final long ONE_DAY = 24 * ONE_HOUR;

	private static final float GAP_X = 15;
	private static final long LARGE_STEP = 15 * ONE_MINUTE;
	private static final long SMALL_STEP = ONE_MINUTE;
	private static final long SMALL_FULL_RANGE = 15 * ONE_MINUTE;
	private static final int HOUR_PER_PAGE = 15;
	private static final long LARGE_FULL_RANGE = HOUR_PER_PAGE * ONE_HOUR;
	private static final float SCROLL_PADDING = 20;

	private static final SimpleDateFormat sHH_MM_FORMATTER = new SimpleDateFormat(
			"kk:mm, dd");
	private static final SimpleDateFormat sHH_AM_FORMATTER = new SimpleDateFormat(
			"hh a", Locale.US);

	public static final boolean DEBUG_MODE = true;

	private float mBaseY = 0.0f;
	private Calendar mDrawCalendar = Calendar.getInstance();
	private Date mTempTime = new Date();
	private Date mDownTime = new Date();
	private boolean isSelecting = false;
	private Paint mPaint = null;
	private float mDensity = 1f;
	private float mBarHeightPerHour = 0;
	private Scroller mScroller = null;
	private float mMaxScrollX = 0.0f;
	private float mMaxScrollYTop = 0.0f;
	private float mMaxScrollYBottom = 0.0f;
	private float mMaxTouchTop = 0.0f;
	private float mMaxTouchBottom = 0.0f;
	private float mScrollYPadding = 0.0f;

	private PointF mCurrentPoint = new PointF();
	private PointF mDownPoint = new PointF();
	private PointF mPressPoint = new PointF();
	private long mDeltaMajor = 0L;
	private long mDeltaMinor = 0L;

	private Calendar mNow = Calendar.getInstance();
	private Calendar mCurrentDate = null;
	private Calendar mStartTime = null;
	private boolean hasTime = false;

	private DateChangeListener mDateChangeListener = null;

	public OneDaySelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public OneDaySelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public OneDaySelector(Context context) {
		super(context);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mScroller = new Scroller(getContext());
		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		mDensity = dm.density;
		mMaxScrollX = 0;
		mScrollYPadding = SCROLL_PADDING * mDensity;
		mDetector.setIsLongpressEnabled(false);
		
		mCurrentDate = (Calendar) mNow.clone();
		mCurrentDate.clear();
		mCurrentDate.set(mNow.get(Calendar.YEAR), mNow.get(Calendar.MONDAY),
				mNow.get(Calendar.DATE));
		mStartTime = (Calendar) mNow.clone();
		mStartTime.clear();
	}

	public void setCurrentDate(Calendar day) {
		if (mCurrentDate == null) {
			mCurrentDate = Calendar.getInstance();
		}
		mCurrentDate.clear();
		if (day == null) {
			return;
		}
		boolean hasTime = Tool.hasTime(day);
		mCurrentDate.set(day.get(Calendar.YEAR), day.get(Calendar.MONDAY),
				day.get(Calendar.DATE));
		Log.d(TAG, "set mCurrentDate: %s from day %s", mCurrentDate.getTime(),
				day.getTime());
		mStartTime.clear();
		hasTime = false;
		if (hasTime) {
			hasTime = true;
			mStartTime.set(day.get(Calendar.YEAR), day.get(Calendar.MONDAY),
					day.get(Calendar.DATE), day.get(Calendar.HOUR_OF_DAY),
					day.get(Calendar.MINUTE), day.get(Calendar.SECOND));
		}
		invalidate();
	}

	public Calendar getCurrentDate() {
		return mCurrentDate;
	}

	public void setStartTime(Calendar startTime) {
		if (startTime != null) {
			mStartTime = startTime;
		}
	}

	public Calendar getStartTime() {
		return mStartTime;
	}

	public interface DateChangeListener {
		void nextDay(Calendar date);

		void previousDay(Calendar date);

		void selectDay(Calendar date);
	}

	protected void moveToNextDay() {
		mCurrentDate.add(Calendar.DATE, 1);
		if (mDateChangeListener != null) {
			mDateChangeListener.nextDay(mCurrentDate);
		}
	}

	protected void moveToPreviousDay() {
		mCurrentDate.add(Calendar.DATE, -1);
		if (mDateChangeListener != null) {
			mDateChangeListener.previousDay(mCurrentDate);
		}
	}

	public void setDateChangeListener(DateChangeListener listener) {
		mDateChangeListener = listener;
	}

	class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {

		int mWidth = 0;
		int mHeight = 0;
		MotionEvent mDown = null;

		boolean isStoppedFling = false;
		long deltaMajor = 0;

		@Override
		public boolean onDown(MotionEvent e) {

			isStoppedFling = false;
			if (!mScroller.isFinished()) { // is flinging
				mScroller.forceFinished(true); // to stop flinging on touch
				isStoppedFling = true;
				return true;
			}

			mWidth = getWidth();
			mHeight = getHeight();
			isSelecting = false;
			deltaMajor = 0;

			mCurrentPoint.x = 0;
			mCurrentPoint.y = 0;
			mPressPoint.x = 0;
			mPressPoint.y = 0;
			mDownPoint.x = e.getX();
			mDownPoint.y = e.getY();
			invalidate();

			// Log.d("Time", "DOWN: %d, (%f, %f)",
			// e.getAction(),e.getX(),e.getY());
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onFling(android
		 * .view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// Log.d("Time", "Fling: [%f, %f]", velocityX, velocityY);

			if (isSelecting) {
				return super.onFling(e1, e2, velocityX, velocityY);
			} else {
				mScroller.fling(getScrollX(), getScrollY(), -(int) velocityX,
						-(int) velocityY, -(int) mMaxScrollX,
						(int) mMaxScrollX, (int) mMaxScrollYTop,
						(int) mMaxScrollYBottom);
				invalidate();
				return true;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onScroll(android
		 * .view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			boolean result = false;
			if (isSelecting) {
				int action = e2.getAction();
				float x = e2.getX();
				float y = e2.getY();

				float dx = (e2.getX() - e1.getX());
				float dy = (e2.getY() - e1.getY());
				long deltaMiner = 0;

				// //if true accept only x or y action
				// // false accept both directions action
				// if (Math.abs(dx) <= GAP_X * mDensity) {
				deltaMajor = ((long) Math
						.round(dy / mHeight * LARGE_FULL_RANGE))
						/ LARGE_STEP
						* LARGE_STEP;
				// } else {
				int sign = (int) Math.signum(Math.round(dx));
				float minerOffset = Math.abs(dx) - GAP_X * mDensity;
				if (minerOffset < 0) {
					minerOffset = 0;
				}
				deltaMiner = ((long) Math.round(minerOffset
						/ (mWidth / 2 - Math.round(GAP_X * mDensity) / 2)
						* SMALL_FULL_RANGE))
						/ SMALL_STEP * SMALL_STEP;
				// // Miner cannot larget than LARGE_STEP
				// if (deltaMiner > LARGE_STEP) {
				// deltaMiner = LARGE_STEP;
				// }
				deltaMiner *= sign;
				// }

				long delta = deltaMajor + deltaMiner;
				// Log.d("Time", "onScroll: %d, (%f, %f):  %d, %s", action,
				// (e2.getX() - e1.getX()) / mWidth * 100,
				// (e2.getY() - e1.getY()) / mHeight * 100, delta
				// / ONE_MINUTE,
				// sHH_MM_FORMATTER.format(mDownTime));

				switch (action) {
				case MotionEvent.ACTION_MOVE:
					mTempTime.setTime(mDownTime.getTime() + delta);

					mDeltaMajor = deltaMajor;
					mDeltaMinor = deltaMiner;
					mCurrentPoint.x = e2.getX();
					mCurrentPoint.y = e2.getY();

					// .d("Time", "MOVE...");
					invalidate();
					result = true;
					break;
				default:
					// Log.d("Time", "Other...");
					break;
				}
			} else {
				float target = distanceY + getScrollY();
				if (target < mMaxScrollYTop) {
					target = mMaxScrollYTop;
				} else if (target > mMaxScrollYBottom) {
					target = mMaxScrollYBottom;
				}
				scrollTo(0, (int) target);
				// invalidate();
				result = true;
			}

			if (!result) {
				result = super.onScroll(e1, e2, distanceX, distanceY);
			}
			return result;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.GestureDetector.SimpleOnGestureListener#onShowPress(
		 * android.view.MotionEvent)
		 */
		@Override
		public void onShowPress(MotionEvent e) {
			// relocate time & position

			// Log.d("Time", "onShowPress: %d, (%f, %f)", e.getAction(),
			// e.getX(),e.getY());
			if (isStoppedFling) {
				isStoppedFling = false;
				return;
			}

			if (isSelecting == false) {
				isSelecting = true;
				
				hasTime = true;

				long delta = ((long) Math
						.round((e.getY() + getScrollY() - mBaseY) / mHeight
								* LARGE_FULL_RANGE))
						/ LARGE_STEP * LARGE_STEP;
				
				long time = mCurrentDate.getTimeInMillis() + delta;
				mDownTime.setTime(time);
				mTempTime.setTime(time);
				
				mPressPoint.x = e.getX();
				mPressPoint.y = e.getY();

				invalidate();
			}
			super.onShowPress(e);
		}

	}

	GestureDetector mDetector = new GestureDetector(getContext(),
			new SimpleGestureListener());

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Log.d("Time Touch", " abs: %f, offset %d [%f %f]", event.getY(),
			// getScrollY(), mMaxTouchTop,
			// mMaxTouchBottom);
			if (event.getY() + getScrollY() >= mMaxTouchTop
					&& event.getY() + getScrollY() <= mMaxTouchBottom) {
				// Log.d("Time Touch", "ACTION_DOWN");
				result = mDetector.onTouchEvent(event);
			} else {
				// Log.d("Time Touch", "ACTION_DOWN ignore");
			}
		} else {
			result = mDetector.onTouchEvent(event);
		}

		if (isSelecting == true && event.getAction() == MotionEvent.ACTION_UP) {
			// Log.d("Time", "MOVE end UP now...");
			isSelecting = false;

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mTempTime.getTime());
			setStartTime(cal);
			if (mDateChangeListener != null) {
				mDateChangeListener.selectDay(getStartTime());
			}
			invalidate();
		}

		if (!result) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// stopScrolling();
				result = true;
			}
		}
		return result;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see android.view.View#onMeasure(int, int)
	// */
	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// // TODO Auto-generated method stub
	// // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	//
	// setMeasuredDimension(
	// getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
	// (int) (mScreenHeight * 5)
	// );
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ScrollView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			int startHour = 8;

			mBarHeightPerHour = (b - t - getPaddingTop() - getPaddingBottom())
					* 1.0f / HOUR_PER_PAGE;
			mBaseY = getPaddingTop() - mBarHeightPerHour * startHour;

			mMaxTouchTop = getYPositionFromRelative(ONE_HOUR * 0);
			mMaxTouchBottom = getYPositionFromRelative(ONE_HOUR * 24
					- startHour);

			mMaxScrollYTop = mMaxTouchTop - mScrollYPadding;
			mMaxScrollYBottom = mMaxTouchBottom + mScrollYPadding
					- (b - t - getPaddingTop() - getPaddingBottom());

		}
	}

	protected float getYPositionFromRelative(Long milli) {
		return (milli / ONE_MINUTE) * mBarHeightPerHour / 60 + mBaseY;
	}

	protected float getYPositionFromTime(Date target) {
		return getYPositionFromRelative(target.getTime()
				- mCurrentDate.getTimeInMillis());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ScrollView#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.draw(canvas);

		int width = getWidth();
		int height = getHeight();

		if (mBarHeightPerHour > 0) {
			mDrawCalendar.setTime(mCurrentDate.getTime());
			for (int i = 0; i <= 24; i++) {
				mDrawCalendar.set(Calendar.HOUR_OF_DAY, i);
				int hour_of_day = i;

				boolean isWhite = hour_of_day >= 6 && hour_of_day <= 18;
				if (hour_of_day % 3 == 0) {
					mPaint.setColor(Color.DKGRAY);
					canvas.drawText(
							sHH_AM_FORMATTER.format(mDrawCalendar.getTime()),
							0, getYPositionFromRelative((i) * ONE_HOUR)
									- (mPaint.ascent() + mPaint.descent()) / 2,
							mPaint);
					if (hour_of_day % 6 == 0) {
						mPaint.setColor(Color.LTGRAY);
						canvas.drawLine(30 * mDensity,
								getYPositionFromRelative((i) * ONE_HOUR),
								width,
								getYPositionFromRelative((i) * ONE_HOUR),
								mPaint);
					}
				}

			}

			if (mStartTime != null && hasTime) {
				mPaint.setColor(Color.BLACK);
				canvas.drawLine(0, getYPositionFromTime(mStartTime.getTime()),
						width, getYPositionFromTime(mStartTime.getTime()),
						mPaint);
				canvas.drawText(sHH_MM_FORMATTER.format(mStartTime.getTime()),
						20 * mDensity, 140 * mDensity + getScrollY(), mPaint);
			}

			if (Tool.isSameDay(mCurrentDate, mNow)) {
				mPaint.setColor(Color.BLACK);
				float y = getYPositionFromTime(mNow.getTime());
				canvas.drawText("Now->", getWidth() - 30 * mDensity, y, mPaint);
			}
		}

		if (DEBUG_MODE) {
			if (isSelecting) {
				mPaint.setColor(Color.RED);
				canvas.drawText(sHH_MM_FORMATTER.format(mTempTime),
						20 * mDensity, 120 * mDensity + getScrollY(), mPaint);
				canvas.drawLine(0, getYPositionFromTime(mTempTime), width,
						getYPositionFromTime(mTempTime), mPaint);

				mPaint.setColor(Color.BLUE);
				canvas.drawText(sHH_MM_FORMATTER.format(mDownTime),
						20 * mDensity, 100 * mDensity + getScrollY(), mPaint);

				if (mDownPoint.x > 0 && mDownPoint.y > 0) {
					mPaint.setColor(Color.BLUE);
					canvas.drawLine(mDownPoint.x, 0 + getScrollY(),
							mDownPoint.x, height + getScrollY(), mPaint);
					canvas.drawLine(0, mDownPoint.y + getScrollY(), width,
							mDownPoint.y + getScrollY(), mPaint);
				}
				if (mPressPoint.x > 0 && mPressPoint.y > 0) {
					mPaint.setColor(Color.BLUE);
					canvas.drawCircle(mPressPoint.x, mPressPoint.y
							+ getScrollY(), 6 * mDensity, mPaint);
				}

				if (mCurrentPoint.x > 0 && mCurrentPoint.y > 0) {
					mPaint.setColor(Color.GREEN);
					canvas.drawLine(mCurrentPoint.x, 0 + getScrollY(),
							mCurrentPoint.x, height + getScrollY(), mPaint);
					canvas.drawText(String.valueOf(mDeltaMinor / ONE_MINUTE),
							mCurrentPoint.x, 20 * mDensity + getScrollY(),
							mPaint);
					canvas.drawLine(0, mCurrentPoint.y + getScrollY(), width,
							mCurrentPoint.y + getScrollY(), mPaint);
					canvas.drawText(String.valueOf(mDeltaMajor / ONE_MINUTE),
							0, mCurrentPoint.y + getScrollY(), mPaint);
				}
			}
		}

		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
		}

	}
}
