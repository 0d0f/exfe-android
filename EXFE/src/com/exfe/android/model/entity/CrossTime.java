package com.exfe.android.model.entity;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

public class CrossTime extends Entity {

	public static final int MARK_FORMAT = 0;
	public static final int MARK_ORIGINAL = 1;

	private Long mId;
	private EFTime mBeginAt;
	private String mOrigin;
	private int mOriginMarkType;

	public CrossTime(EFTime beginAt, String origin, int markType) {
		mType = EntityFactory.TYPE_CROSSTIME;
		mBeginAt = beginAt;
		mOrigin = origin;
		mOriginMarkType = markType;
	}

	public CrossTime(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);

		mType = EntityFactory.TYPE_CROSSTIME;

		mBeginAt = (EFTime) EntityFactory
				.create(json.optJSONObject("begin_at"));
		mOrigin = json.optString("origin", "");
		mOriginMarkType = json.optInt("outputformat", 0);
	}

	public JSONObject toJSON(boolean deep) {
		JSONObject json = super.toJSON(deep);
		try {
			json.put("begin_at", mBeginAt.toJSON());
			json.put("outputformat", mOriginMarkType);
			json.put("origin", mOrigin);
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
	 * @return the beginAt
	 */
	public EFTime getBeginAt() {
		return this.mBeginAt;
	}

	/**
	 * @param beginAt
	 *            the beginAt to set
	 */
	public void setBeginAt(EFTime beginAt) {
		this.mBeginAt = beginAt;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return this.mOrigin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.mOrigin = origin;
	}

	/**
	 * @return the originMark
	 */
	public int getOriginMarkType() {
		return this.mOriginMarkType;
	}

	/**
	 * @param originMark
	 *            the originMark to set
	 */
	public void setOriginMarkType(int originMarkType) {
		this.mOriginMarkType = originMarkType;
	}

	public String getLongLocalTimeSring(Resources res) {
		return getLongLocalTimeSring(mBeginAt, true, res);
	}
	
	public String getLongLocalTimeSring(boolean needTimeZone, Resources res) {
		return getLongLocalTimeSring(mBeginAt, needTimeZone, res);
	}

	public String getLongLocalTimeSring(EFTime eftime, boolean needTimeZone,
			Resources res) {
		// use default time and time zone
		GregorianCalendar now = new GregorianCalendar();
		String tz = "";
		// use test mock time
		if (Tool.NOW != null) {
			now.setTimeInMillis(Tool.NOW.getTime());
		}
		// use the test input time zone
		if (Tool.NOW != null && !TextUtils.isEmpty(Tool.TIME_ZONE)) {
			tz = Tool.TIME_ZONE;
			now.setTimeZone(TimeZone.getTimeZone(Tool
					.converTimeZoneId(Tool.TIME_ZONE)));
		}

		TimeZone current_tz = now.getTimeZone();
		TimeZone target_tz = current_tz;
		if (!TextUtils.isEmpty(eftime.getTimezone())) {
			target_tz = TimeZone.getTimeZone(Tool.converTimeZoneId(eftime
					.getTimezone()));
		}
		boolean same_tz = Tool.isSameTimeZone(current_tz, target_tz);

		StringBuilder sb = new StringBuilder();
		if (mOriginMarkType == MARK_ORIGINAL) {
			sb.append(mOrigin);
			if (!same_tz  && needTimeZone) {
				if (!TextUtils.isEmpty(eftime.getTimezone())) {
					Tool.appendSpaceWhenNeeded(sb);
					sb.append(eftime.getTimezone());
				}
			}
		} else {
			// assume now/today if no time/date info.
			GregorianCalendar then = new GregorianCalendar();
			GregorianCalendar then_in_here = new GregorianCalendar();
			boolean hasTime = !TextUtils.isEmpty(eftime.getTime());
			boolean hasDate = !TextUtils.isEmpty(eftime.getDate());
			boolean hasTimeWord = !TextUtils.isEmpty(eftime.getTimeWord());
			boolean hasDateWord = !TextUtils.isEmpty(eftime.getDateWord());

			try {
				boolean skip = false;
				DateFormat parser = null;
				String timestr = null;
				if (hasTime && hasDate) {
					parser = Const.UTC_DATE_TIME_FORMAT;
					timestr = String.format("%s %s", eftime.getDate(),
							eftime.getTime());
				} else if (hasTime) {
					parser = Const.UTC_TIME_HHMMSS_FORMAT;
					timestr = eftime.getTime();
				} else if (hasDate) {
					parser = Const.UTC_DATE_FORMAT;
					timestr = eftime.getDate();
				} else {
					skip = true;
				}
				if (!skip) {
					Date beginTime = parser.parse(timestr);
					then.setTime(beginTime);
					then_in_here.setTime(beginTime);
					then_in_here.setTimeZone(current_tz);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Tool.appendSpaceWhenNeeded(sb);
			// Time_word
			if (hasTimeWord) {
				sb.append(eftime.getTimeWord());
			}
			Tool.appendSpaceWhenNeeded(sb);
			if (hasTimeWord && hasTime) {
				sb.append("at");
			}
			Tool.appendSpaceWhenNeeded(sb);
			// Time
			if (hasTime) {
				// handle the date format
				SimpleDateFormat dfFormator = new SimpleDateFormat("h:mma",
						Locale.US);
				dfFormator.setTimeZone(then_in_here.getTimeZone());
				sb.append(dfFormator.format(then_in_here.getTime()));
			}
			Tool.appendSpaceWhenNeeded(sb);
			// timezone
			if (!same_tz && needTimeZone) {
				if (hasTime) {
					// location_zone
					sb.append(tz);
				} else if (hasTimeWord) {
					// orginal_zone
					sb.append(eftime.getTimezone());
				}
			}
			Tool.appendSpaceWhenNeeded(sb);
			// Date_word
			if (hasDateWord) {
				sb.append(eftime.getDateWord());
			}
			Tool.appendSpaceWhenNeeded(sb);
			if (sb.length() > 0 && hasDate) {
				sb.append("on");
			}
			Tool.appendSpaceWhenNeeded(sb);
			// Date
			if (hasDate) {
				// handle the date format
				GregorianCalendar target = then_in_here;
				if (!same_tz && !hasTime) {
					target = then;
				}
				String fmt = "E, MMM d";
				if (now.get(Calendar.YEAR) != target.get(Calendar.YEAR)) {
					fmt = "E, MMM d, yyyy";
				}
				SimpleDateFormat dfFormator = new SimpleDateFormat(fmt,
						Locale.US);
				dfFormator.setTimeZone(target.getTimeZone());
				sb.append(dfFormator.format(target.getTime()));
			}
		}
		return sb.toString().trim();
	}

	public void saveToDao(DatabaseHelper dbhelper) {
		try {
			Dao<CrossTime, Long> dao = dbhelper.getCachedDao(getClass());
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
