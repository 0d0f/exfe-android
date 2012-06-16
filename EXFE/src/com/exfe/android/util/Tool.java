package com.exfe.android.util;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.debug.Log;

import android.content.res.Resources;
import android.text.TextUtils;

public class Tool {
	private static final String TAG = Tool.class.getSimpleName();

	private static Pattern PatternJSON = Pattern.compile("^\\{.*\\}*$");

	public static boolean isJson(String str) {
		if (TextUtils.isEmpty(str)) {
			return false;
		}
		String s = str.trim();
		if (s.startsWith("{") && s.endsWith("}")) {
			return true;
		}
		return false;
		// Log.v(TAG, "result string %s", str);
		// Matcher m = PatternJSON.matcher(str.trim());
		// return m.matches();
	}

	public static String parseString(JSONObject json, String name) {
		return parseString(json, name, "");
	}

	public static String parseString(JSONObject json, String name,
			String defValue) {
		if (json == null || !json.has(name)) {
			return null;
		}

		if (!json.isNull(name)) {
			return json.optString(name, defValue);
		} else {
			return defValue;
		}
	}

	public static Date parseDate(JSONObject json, String name) {
		return parseDate(json, name, null);
	}

	public static Date parseDate(JSONObject json, String name, Date defValue) {

		if (json == null || !json.has(name)) {
			return null;
		}

		String result = null;
		long num = 0;

		if (!json.isNull(name)) {
			result = json.optString(name, null);
			num = json.optLong(name, 0);
		}
		if (TextUtils.isEmpty(result)) {
			return defValue;
		}

		if (num != 0 && Long.valueOf(result) == num) {
			Date d = new Date(num * 1000);
			return d;
		}

		try {
			Date d = Const.STD_DATE_FORMAT.parse(result);
			return d;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return defValue;
	}

	public static final long SECOND = 1;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	public static final long MONTH = 30 * DAY;
	public static final long YEAR = 12 * MONTH;

	public static Date NOW = null;

	public static String getRelativeStringFromNow(Date target, Resources res) {
		if (NOW != null) {
			return getRelativeString(target, NOW, res);
		}
		return getRelativeString(target, new Date(), res);
	}

	public static String getRelativeString(Date target, Date base, Resources res) {

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
				relative = res.getQuantityString(R.plurals.minutes_ago, minues,
						minues);
			} else {
				relative = res.getQuantityString(R.plurals.minutes_later,
						minues, minues);
			}
		} else if (delta < 1 * DAY) {
			if (isNegative) {
				relative = res.getQuantityString(R.plurals.hours_ago, hours,
						hours);
			} else {
				relative = res.getQuantityString(R.plurals.hours_later, hours,
						hours);
			}
		} else if (delta < 1 * MONTH) {
			if (isNegative) {
				relative = res
						.getQuantityString(R.plurals.days_ago, days, days);
			} else {
				relative = res.getQuantityString(R.plurals.days_later, days,
						days);
			}
		} else if (delta < 1 * YEAR) {
			if (isNegative) {
				relative = res.getQuantityString(R.plurals.months_ago, months,
						months);
			} else {
				relative = res.getQuantityString(R.plurals.months_later,
						months, months);
			}
		} else {
			if (isNegative) {
				relative = res.getQuantityString(R.plurals.years_ago, years,
						years);
			} else {
				relative = res.getQuantityString(R.plurals.years_later, years,
						years);
			}
		}
		return relative;
	}
}
