package com.exfe.android.model.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.exfe.android.R;

import android.content.res.Resources;
import android.text.method.DateTimeKeyListener;

public class EFTime extends Entity {

	public static final long SECOND = 1;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	public static final long MONTH = 30 * DAY;
	public static final long YEAR = 12 * MONTH;

	public static final DateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	
	private String mDateWord;
	private String mDate;
	private String mTimeWord;
	private String mTime;
	private String mTimezone;

	public EFTime(String dateWord, String date, String timeWord, String time, String timezone) {
		mType = EntityFactory.TYPE_EFTIME;
		mDateWord = dateWord;
		mDate = date;
		mTimeWord = timeWord;
		mTime = time;
		mTimezone = timezone;
	}

	public EFTime(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_EFTIME;

		mDateWord = json.optString("date_word", "");
		mDate = json.optString("date", "");
		mTimeWord = json.optString("time_word", "");
		mTime = json.optString("time", "");
		mTimezone = json.optString("timezone", "");

	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("date_word", mDateWord);
			json.put("date", mDate);
			json.put("time_word", mTimeWord);
			json.put("time", mTime);
			json.put("timezone", mTimezone);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the dateWord
	 */
	public String getDateWord() {
		return this.mDateWord;
	}

	/**
	 * @param dateWord
	 *            the dateWord to set
	 */
	public void setDateWord(String dateWord) {
		this.mDateWord = dateWord;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return this.mDate;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.mDate = date;
	}

	/**
	 * @return the timeWord
	 */
	public String getTimeWord() {
		return this.mTimeWord;
	}

	/**
	 * @param timeWord
	 *            the timeWord to set
	 */
	public void setTimeWord(String timeWord) {
		this.mTimeWord = timeWord;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return this.mTime;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.mTime = time;
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return this.mTimezone;
	}

	/**
	 * @param timezone
	 *            the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.mTimezone = timezone;
	}

	static final EFTime NOW = null;

	public String getRelativeStringToNow(Resources res) {
		return getRelativeString(new Date(System.currentTimeMillis()), res);
	}

	public String getRelativeString(Date base, Resources res) {
		try {
			String datetimestr = String.format("%s %s%s", mDate, mTime,
					mTimezone);
			Date target = sfmt.parse(datetimestr);
			long delta = (target.getTime() - base.getTime()) / 1000;
			boolean isNegative = false;
			if (delta < 0) {
				isNegative = true;
				delta = Math.abs(delta);
			}

			long t = delta;
			int seconds = (int) (t % 60);
			t /= 60;
			int minues = (int) (t % 60);
			t /= 60;
			int hours = (int) (t % 24);
			t /= 24;
			int days = (int) (t % 30);
			t /= 30;
			int months = (int) (t % 12);
			t /= 12;
			int years = (int) t;

			String relative = null;
			if (delta < 1 * MINUTE) {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.seconds_ago,
							seconds, seconds);
				} else {
					relative = res.getQuantityString(R.plurals.seconds_later,
							seconds, seconds);
				}
			} else if (delta < 1 * HOUR) {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.minutes_ago,
							minues, minues);
				} else {
					relative = res.getQuantityString(R.plurals.minutes_later,
							minues, minues);
				}
			} else if (delta < 1 * DAY) {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.hours_ago,
							hours, hours);
				} else {
					relative = res.getQuantityString(R.plurals.hours_later,
							hours, hours);
				}
			} else if (delta < 1 * MONTH) {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.days_ago, days,
							days);
				} else {
					relative = res.getQuantityString(R.plurals.days_later,
							days, days);
				}
			} else if (delta < 1 * YEAR) {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.months_ago,
							months, months);
				} else {
					relative = res.getQuantityString(R.plurals.months_later,
							months, months);
				}
			} else {
				if (isNegative) {
					relative = res.getQuantityString(R.plurals.years_ago,
							years, years);
				} else {
					relative = res.getQuantityString(R.plurals.years_later,
							years, years);
				}
			}

			return relative;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
}
