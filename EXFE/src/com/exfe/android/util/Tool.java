package com.exfe.android.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.FloatMath;

import com.exfe.android.Const;
import com.exfe.android.R;

public class Tool {
	private static final String TAG = Tool.class.getSimpleName();

	private static Pattern PatternJSON = Pattern.compile("^\\{.*\\}*$");
	private static Pattern ZonePattern = Pattern
			.compile("[\\+\\-]\\d\\d:\\d\\d");

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
			Date d = Const.UTC_DATE_TIME_TIMEZONE_FORMAT.parse(result);
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
	public static String TIME_ZONE = null;

	// @Deprecated
	// public static String getRelativeStringFromNow(Date target, Resources res)
	// {
	// if (NOW != null) {
	// return getRelativeString(target, NOW, res);
	// }
	// return getRelativeString(target, new Date(), res);
	// }
	//
	// @Deprecated
	// public static String getRelativeString(Date target, Date base, Resources
	// res) {
	//
	// long delta = (target.getTime() - base.getTime()) / 1000;
	// boolean isNegative = false;
	// if (delta < 0) {
	// isNegative = true;
	// delta = Math.abs(delta);
	// }
	//
	// long t = delta;
	// int seconds = (int) (t % 60);
	// t /= 60;
	// int minues = (int) (t % 60);
	// t /= 60;
	// int hours = (int) (t % 24);
	// t /= 24;
	// int days = (int) (t % 30);
	// t /= 30;
	// int months = (int) (t % 12);
	// t /= 12;
	// int years = (int) t;
	//
	// String relative = null;
	// if (delta < 1 * MINUTE) {
	// if (isNegative) {
	// relative = res.getQuantityString(R.plurals.seconds_ago,
	// seconds, seconds);
	// } else {
	// relative = res.getQuantityString(R.plurals.seconds_later,
	// seconds, seconds);
	// }
	// } else if (delta < 1 * HOUR) {
	// if (isNegative) {
	// relative = res.getQuantityString(R.plurals.minutes_ago, minues,
	// minues);
	// } else {
	// relative = res.getQuantityString(R.plurals.minutes_later,
	// minues, minues);
	// }
	// } else if (delta < 1 * DAY) {
	// if (isNegative) {
	// relative = res.getQuantityString(R.plurals.hours_ago, hours,
	// hours);
	// } else {
	// relative = res.getQuantityString(R.plurals.hours_later, hours,
	// hours);
	// }
	// } else if (delta < 1 * MONTH) {
	// if (isNegative) {
	// relative = res
	// .getQuantityString(R.plurals.days_ago, days, days);
	// } else {
	// relative = res.getQuantityString(R.plurals.days_later, days,
	// days);
	// }
	// } else if (delta < 1 * YEAR) {
	// if (isNegative) {
	// relative = res.getQuantityString(R.plurals.months_ago, months,
	// months);
	// } else {
	// relative = res.getQuantityString(R.plurals.months_later,
	// months, months);
	// }
	// } else {
	// if (isNegative) {
	// relative = res.getQuantityString(R.plurals.years_ago, years,
	// years);
	// } else {
	// relative = res.getQuantityString(R.plurals.years_later, years,
	// years);
	// }
	// }
	// return relative;
	// }

	// @Deprecated
	// public static CharSequence getRelativeShortString(Date target, Resources
	// res) {
	// if (NOW != null) {
	// return getRelativeShortString(target, NOW, res);
	// }
	// return getRelativeShortString(target, new Date(), res);
	// }
	//
	// @Deprecated
	// public static CharSequence getRelativeShortString(Date target, Date base,
	// Resources res) {
	//
	// long delta = (target.getTime() - base.getTime()) / 1000;
	// boolean isNegative = false;
	// if (delta < 0) {
	// isNegative = true;
	// delta = Math.abs(delta);
	// }
	//
	// long t = delta;
	// int seconds = (int) (t % 60);
	// t /= 60;
	// int minutes = (int) (t % 60);
	// t /= 60;
	// int hours = (int) (t % 24);
	// t /= 24;
	// int days = (int) (t % 30);
	// t /= 30;
	// int months = (int) (t % 12);
	// t /= 12;
	// int years = (int) t;
	//
	// if (!isNegative) {
	// return "";
	// }
	// if (years > 0) {
	// return String.format("%dy", years);
	// }
	// if (months > 0) {
	// return String.format("%dM", months);
	// }
	// if (days > 0) {
	// return String.format("%dd", days);
	// }
	// if (hours > 0) {
	// return String.format("%dh", hours);
	// }
	// if (minutes > 0) {
	// return String.format("%dm", minutes);
	// }
	// if (seconds > 0) {
	// return String.format("%ds", seconds);
	// }
	//
	// return "";
	// }

	public static CharSequence getXRelativeString(Date target, Resources res) {
		if (NOW != null) {
			return getXRelativeString(target, NOW, res);
		}
		return getXRelativeString(target, null, res);
	}

	public static CharSequence getXRelativeString(Date target, Date base,
			Resources res) {
		if (target == null || res == null) {
			throw new NullPointerException();
		}

		Calendar baseDay = new GregorianCalendar();
		if (base != null) {
			baseDay.setTime(base);
		} else {
			base = baseDay.getTime();
		}
		baseDay.set(Calendar.HOUR, 0);
		baseDay.set(Calendar.MINUTE, 0);
		baseDay.set(Calendar.SECOND, 0);
		baseDay.set(Calendar.MILLISECOND, 0);
		baseDay.set(Calendar.AM_PM, Calendar.AM);
		Calendar targetDay = new GregorianCalendar();
		targetDay.setTime(target);
		targetDay.set(Calendar.HOUR, 0);
		targetDay.set(Calendar.MINUTE, 0);
		targetDay.set(Calendar.SECOND, 0);
		targetDay.set(Calendar.MILLISECOND, 0);
		targetDay.set(Calendar.AM_PM, Calendar.AM);

		int dateOffSet = (int) TimeUnit.MILLISECONDS.toDays(targetDay
				.getTimeInMillis() - baseDay.getTimeInMillis());

		if (dateOffSet >= -1 && dateOffSet <= 1) {
			// date offset [-1,1]
			int minitesOffset = (int) TimeUnit.MILLISECONDS.toMinutes(target
					.getTime() - base.getTime());
			// blue conditions
			if (minitesOffset <= 0) {
				if (minitesOffset > -30) {
					return res.getString(R.string.now);
				} else if (minitesOffset > -60) {
					return res.getString(R.string.just_now);
				} else if (minitesOffset > -720
						|| (minitesOffset > -1440 && dateOffSet == 0)) {
					// xx hours ago , rounded by 7, up by 8
					float h = (minitesOffset * -1) / 60f;
					int cents = (int) (FloatMath.floor(h * 10) % 10);
					int hours = (int) FloatMath.floor(h);
					if (cents >= 8) {
						hours++;
					}
					return res.getQuantityString(R.plurals.hours_ago, hours,
							hours);
				}
			} else {
				if (minitesOffset < 60) {
					return res.getQuantityString(R.plurals.in_minutes,
							minitesOffset, minitesOffset);
				} else if (minitesOffset < 750) {
					// In xx hours , rounded by 7, up by 8
					float h = minitesOffset / 60f;
					int cents = (int) (FloatMath.floor(h * 10) % 10);
					int hours = (int) FloatMath.floor(h);
					if (cents >= 8) {
						hours++;
					}
					return res.getQuantityString(R.plurals.in_hours, hours,
							hours);
				}
			}

			// rest: today/tomorrow/ yesterday
			if (dateOffSet == 0) {
				return res.getString(R.string.today);
			} else if (dateOffSet == -1) {
				return res.getString(R.string.yesterday);
			} else if (dateOffSet == 1) {
				return res.getString(R.string.tomorrow);
			}
		} else if (dateOffSet == -2) {
			return res.getString(R.string.the_day_before_yesterday);
		} else if (dateOffSet == 2) {
			return res.getString(R.string.the_day_after_tomorrow);
		} else if (dateOffSet >= -30 && dateOffSet <= -3) {
			return res.getQuantityString(R.plurals.days_ago, dateOffSet * -1,
					dateOffSet * -1);
		} else if (dateOffSet >= 3 && dateOffSet <= 30) {
			long weekOffset = targetDay.get(Calendar.WEEK_OF_YEAR)
					- baseDay.get(Calendar.WEEK_OF_YEAR);
			if (weekOffset < -2
					&& (targetDay.get(Calendar.YEAR)
							- baseDay.get(Calendar.YEAR) == 1)) {
				weekOffset += baseDay.getActualMaximum(Calendar.WEEK_OF_YEAR);
			}
			if (weekOffset == 0) {
				return targetDay.getDisplayName(Calendar.DAY_OF_WEEK,
						Calendar.LONG, Locale.getDefault());
			} else if (weekOffset == 1) {
				return res.getString(R.string.next_xxx, targetDay
						.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
								Locale.getDefault()));
			}
			return res.getQuantityString(R.plurals.in_days, dateOffSet,
					dateOffSet);
		}

		// rest: long time
		boolean negative = dateOffSet < 0;
		int years = (int) Math.floor(Math.abs(dateOffSet) / 365.25);
		double m = ((Math.abs(dateOffSet) * 4) % 1461) / 4 / 30f;
		int months = (int) Math.floor(m);
		int cent = (int) Math.floor(m * 10) % 10;
		if (cent >= 8) {
			months++;
		}
		if (months >= 12) {
			years += months / 12;
			months = months % 12;
		}
		if (months == 0) {
			if (negative) {
				return res.getQuantityString(R.plurals.years_ago, years, years);
			} else {
				return res.getQuantityString(R.plurals.in_years, years, years);
			}
		} else if (years == 0) {
			if (negative) {
				return res.getQuantityString(R.plurals.months_ago,
						(int) months, months);
			} else {
				return res.getQuantityString(R.plurals.in_months, months,
						months);
			}
		} else {
			String ys = res.getQuantityString(R.plurals.n_years, years, years);
			String ms = res.getQuantityString(R.plurals.n_months, months,
					months);
			if (negative) {
				return res.getString(R.string.years_months_ago, ys, ms);
			} else {
				return res.getString(R.string.in_years_months, ys, ms);
			}
		}
	}

	public static boolean isInSame(int field, Date targetDate) {
		Calendar cal = new GregorianCalendar();
		int current = cal.get(field);
		cal.setTime(targetDate);
		int target = cal.get(field);
		return current == target;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap input,
			float cornerRadius, int w, int h) {
		return getRoundedCornerBitmap(input, cornerRadius, w, h, false, false,
				false, false);
	}

	/**
	 * Create rounded corner bitmap from original bitmap.
	 * <p>
	 * Reference
	 * http://stackoverflow.com/questions/2459916/how-to-make-an-imageview
	 * -to-have-rounded-corners
	 * 
	 * @param input
	 *            Original bitmap.
	 * @param cornerRadius
	 *            Corner radius in pixel.
	 * @param w
	 * @param h
	 * @param squareTL
	 * @param squareTR
	 * @param squareBL
	 * @param squareBR
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap input,
			float cornerRadius, int w, int h, boolean squareTL,
			boolean squareTR, boolean squareBL, boolean squareBR) {

		Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);

		// make sure that our rounded corner is scaled appropriately
		final float roundPx = cornerRadius;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		// draw rectangles over the corners we want to be square
		if (squareTL) {
			canvas.drawRect(0, 0, w / 2, h / 2, paint);
		}
		if (squareTR) {
			canvas.drawRect(w / 2, 0, w, h / 2, paint);
		}
		if (squareBL) {
			canvas.drawRect(0, h / 2, w / 2, h, paint);
		}
		if (squareBR) {
			canvas.drawRect(w / 2, h / 2, w, h, paint);
		}

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(input, 0, 0, paint);

		return output;
	}

	public static CharSequence highlightKeyword(CharSequence original,
			String keyword, Object style) {
		SpannableString text = new SpannableString(original);
		int pos = original.toString().indexOf(keyword);
		text.setSpan(style, pos, pos + keyword.length(), 0);
		return text;
	}

	public static CharSequence highlightFirstLine(CharSequence original,
			boolean mergeLines, Object style) {
		String abc = original.toString();
		int pos = abc.indexOf("\n");
		SpannableString text = null;
		if (mergeLines) {
			abc.replace("\n", "");
			text = new SpannableString(abc);
		} else {
			text = new SpannableString(original);
		}
		text.setSpan(style, 0, pos, 0);
		return text;
	}

	public static String converTimeZoneId(String three_letters) {
		String tz_str = three_letters;
		if (tz_str == null) {
			tz_str = "";
		}

		Matcher m = ZonePattern.matcher(tz_str);
		if (m.find()) {
			return "GMT".concat(m.group().trim());
		}
		return "UTC";
	}

	public static void appendSpaceWhenNeeded(StringBuilder sb) {
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
			sb.append(" ");
		}
	}

	public static boolean isSameWithLocalZone(String zone_string) {
		TimeZone tz = TimeZone.getDefault();
		if (NOW != null && !TextUtils.isEmpty(TIME_ZONE)) {
			tz = TimeZone.getTimeZone(converTimeZoneId(TIME_ZONE));
		}
		TimeZone target = TimeZone.getTimeZone(converTimeZoneId(zone_string));
		return isSameTimeZone(tz, target);
	}

	public static boolean isSameTimeZone(TimeZone current, TimeZone target) {
		int local_offset = current.getOffset(System.currentTimeMillis());
		int target_offset = target.getRawOffset();
		return local_offset / (60 * 1000) == target_offset / (60 * 1000);
	}

}
