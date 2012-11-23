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

//	public static final DateFormat sfmt_dt = new SimpleDateFormat(
//			"yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
//	public static final DateFormat sfmt_d = new SimpleDateFormat("yyyy-MM-ddZ",
//			Locale.US);
//	public static final DateFormat sfmt_t = new SimpleDateFormat("HH:mm:ssZ",
//			Locale.US);
//	static {
//		sfmt_dt.setTimeZone(TimeZone.getTimeZone("UTC"));
//	}

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
		if (TextUtils.isEmpty(timezone)){
			mTimezone =  Tool.gmtWalkaround(Tool.localTimeZoneString());
		}else{
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
		mTimezone = json.optString("timezone",  Tool.gmtWalkaround(Tool.localTimeZoneString()));

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

	public CharSequence getRelativeStringFromNow(Resources res) {
		boolean hasTime = !TextUtils.isEmpty(mTime);
		boolean hasDate = !TextUtils.isEmpty(mDate);

		try {
			if (hasTime && hasDate) {
				String datetimestr = String.format("%s %s", mDate, mTime);
				Date target = Const.UTC_DATE_TIME_FORMAT.parse(datetimestr);
				return Tool.getXRelativeString(target, res);
			}
			if (hasDate){
				String datetimestr = mDate;
				Date target = Const.UTC_DATE_FORMAT.parse(datetimestr);
				return Tool.getXRelativeString(target, res);
			}
			if (hasTime){
				//??
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
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
}
