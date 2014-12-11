package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.util.Tool;
import com.j256.ormlite.dao.Dao;

public class EFTime extends Entity {

	public static final int DATETIME_TYPE_UNKNOWN = 0;
	public static final int DATETIME_TYPE_FULL = 1;
	public static final int DATETIME_TYPE_DATE_ONLY = 2;
	public static final int DATETIME_TYPE_TIME_ONLY = 3;

	private long mId = NO_ID;
	private String mDateWord;
	private String mDate;
	private String mTimeWord;
	private String mTime;
	private String mTimezone;

	public EFTime(String dateWord, String date, String timeWord, String time,
			String timezone) {
		mType = EntityFactory.TYPE_EFTIME;
		mDateWord = dateWord;
		mDate = date;
		mTimeWord = timeWord;
		mTime = time;
		if (TextUtils.isEmpty(timezone)) {
			mTimezone = Tool.gmtWalkaround(Tool.localTimeZoneString());
		} else {
			mTimezone = timezone;
		}
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
		mTimezone = json.optString("timezone",
				Tool.gmtWalkaround(Tool.localTimeZoneString()));

	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
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
	 * @return the id
	 */
	public long getId() {
		return this.mId;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.mId = id;
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

	@Override
	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<EFTime, Long> dao = dbhelper.getCachedDao(getClass());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadFromDao(DatabaseHelper dbhelper) {
		// TODO Auto-generated method stub

	}

	public int getDateTimeType() {
		boolean hasTime = !TextUtils.isEmpty(mTime);
		boolean hasDate = !TextUtils.isEmpty(mDate);
		if (hasDate && hasTime) {
			return DATETIME_TYPE_FULL;
		}
		if (hasDate && !hasTime) {
			return DATETIME_TYPE_DATE_ONLY;
		}
		if (!hasDate && hasTime) {
			return DATETIME_TYPE_TIME_ONLY;
		}
		return DATETIME_TYPE_UNKNOWN;
	}

	public CharSequence getRelativeStringFromNow(Resources res) {
		Date target = getUTCDateTime();
		if (target != null){
			return Tool.getXRelativeString(target, res);
		}
		return "";
	}

	public Date getLocalDateTime() {
		int type = getDateTimeType();
		Date target_utc = getUTCDateTime();
		if (target_utc != null){
			Date target = new Date();
			target.setTime(target_utc.getTime());
			return target;
		}
		return null;
	}
	
	public Date getUTCDateTime() {
		int type = getDateTimeType();
		try {
			if (type == DATETIME_TYPE_FULL) {
				String datetimestr = String.format("%s %s", mDate, mTime);
				Date target = Const.UTC_DATE_TIME_FORMAT.parse(datetimestr);
				return target;
			}
			if (type == DATETIME_TYPE_DATE_ONLY) {
				String datetimestr = mDate;
				Date target = Const.UTC_DATE_FORMAT.parse(datetimestr);
				return target;
			}
			if (type == DATETIME_TYPE_TIME_ONLY) {
				return null;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
