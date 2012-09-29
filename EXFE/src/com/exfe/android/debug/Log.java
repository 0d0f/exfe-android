package com.exfe.android.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import com.exfe.android.BuildConfig;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;

public class Log {
	public static boolean DEBUG = BuildConfig.DEBUG;
	public static int LOG_LEVEL = DEBUG?android.util.Log.VERBOSE:android.util.Log.WARN;

	public static void v(Class<?> tag, String msg, Object... args) {
		v(tag.getSimpleName(), msg, args);
	}

	public static void v(String tag, String msg, Object... args) {
		v(tag, null, false, msg, args);
	}

	public static void v(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		v(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void v(String tag, boolean debugOn, String msg,
			Object... args) {
		v(tag, null, debugOn, msg, args);
	}

	public static void v(Class<?> tag, Throwable tr, String msg, Object... args) {
		v(tag.getSimpleName(), tr, msg, args);
	}

	public static void v(String tag, Throwable tr, String msg, Object... args) {
		v(tag, tr, false, msg, args);
	}

	public static void v(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		v(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void v(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.VERBOSE >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.v(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.v(tag, String.format(msg, args));
			}
		}
	}

	public static void d(Class<?> tag, String msg, Object... args) {
		d(tag.getSimpleName(), msg, args);
	}

	public static void d(String tag, String msg, Object... args) {
		d(tag, null, false, msg, args);
	}

	public static void d(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		d(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void d(String tag, boolean debugOn, String msg,
			Object... args) {
		d(tag, null, debugOn, msg, args);
	}

	public static void d(Class<?> tag, Throwable tr, String msg, Object... args) {
		d(tag.getSimpleName(), tr, msg, args);
	}

	public static void d(String tag, Throwable tr, String msg, Object... args) {
		d(tag, tr, false, msg, args);
	}

	public static void d(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		d(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void d(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.DEBUG >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.d(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.d(tag, String.format(msg, args));
			}
		}
	}

	public static void i(Class<?> tag, String msg, Object... args) {
		i(tag.getSimpleName(), msg, args);
	}

	public static void i(String tag, String msg, Object... args) {
		i(tag, null, false, msg, args);
	}

	public static void i(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		i(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void i(String tag, boolean debugOn, String msg,
			Object... args) {
		i(tag, null, debugOn, msg, args);
	}

	public static void i(Class<?> tag, Throwable tr, String msg, Object... args) {
		i(tag.getSimpleName(), tr, msg, args);
	}

	public static void i(String tag, Throwable tr, String msg, Object... args) {
		i(tag, tr, false, msg, args);
	}

	public static void i(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		i(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void i(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.INFO >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.i(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.i(tag, String.format(msg, args));
			}
		}
	}

	public static void w(Class<?> tag, String msg, Object... args) {
		w(tag.getSimpleName(), msg, args);
	}

	public static void w(String tag, String msg, Object... args) {
		w(tag, null, false, msg, args);
	}

	public static void w(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		w(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void w(String tag, boolean debugOn, String msg,
			Object... args) {
		w(tag, null, debugOn, msg, args);
	}

	public static void w(Class<?> tag, Throwable tr, String msg, Object... args) {
		w(tag.getSimpleName(), tr, msg, args);
	}

	public static void w(String tag, Throwable tr, String msg, Object... args) {
		w(tag, tr, false, msg, args);
	}

	public static void w(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		w(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void w(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.WARN >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.w(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.w(tag, String.format(msg, args));
			}
		}
	}

	public static void e(Class<?> tag, String msg, Object... args) {
		e(tag.getSimpleName(), msg, args);
	}

	public static void e(String tag, String msg, Object... args) {
		e(tag, null, false, msg, args);
	}

	public static void e(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		e(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void e(String tag, boolean debugOn, String msg,
			Object... args) {
		e(tag, null, debugOn, msg, args);
	}

	public static void e(Class<?> tag, Throwable tr, String msg, Object... args) {
		e(tag.getSimpleName(), tr, msg, args);
	}

	public static void e(String tag, Throwable tr, String msg, Object... args) {
		e(tag, tr, false, msg, args);
	}

	public static void e(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		e(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void e(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.ERROR >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.e(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.e(tag, String.format(msg, args));
			}
		}
	}
	
	public static void wtf(Class<?> tag, String msg, Object... args) {
		wtf(tag.getSimpleName(), msg, args);
	}

	public static void wtf(String tag, String msg, Object... args) {
		wtf(tag, null, false, msg, args);
	}

	public static void wtf(Class<?> tag, boolean debugOn, String msg,
			Object... args) {
		wtf(tag.getSimpleName(), debugOn, msg, args);
	}

	public static void wtf(String tag, boolean debugOn, String msg,
			Object... args) {
		wtf(tag, null, debugOn, msg, args);
	}

	public static void wtf(Class<?> tag, Throwable tr, String msg, Object... args) {
		wtf(tag.getSimpleName(), tr, msg, args);
	}

	public static void wtf(String tag, Throwable tr, String msg, Object... args) {
		wtf(tag, tr, false, msg, args);
	}

	public static void wtf(Class<?> tag, Throwable tr, boolean debugOn,
			String msg, Object... args) {
		wtf(tag.getSimpleName(), tr, debugOn, msg, args);
	}

	public static void wtf(String tag, Throwable tr, boolean debugOn, String msg,
			Object... args) {
		if (android.util.Log.ASSERT >= LOG_LEVEL || debugOn) {
			if (tr != null) {
				android.util.Log.wtf(tag, String.format(msg, args), tr);
			} else {
				android.util.Log.wtf(tag, String.format(msg, args));
			}
		}
	}

	public static String getStackTraceString(Throwable tr) {
		return android.util.Log.getStackTraceString(tr);
	}

	public static boolean isLoggable(Class<?> tag, int level) {
		return android.util.Log.isLoggable(tag.getSimpleName(), level) && level >= LOG_LEVEL ;
	}

	public static void println(int priority, Class<?> tag, String msg,
			Object... args) {
		println(priority, tag.getSimpleName(), false, msg, args);
	}

	public static void println(int priority, String tag, String msg,
			Object... args) {
		println(priority, tag, false, msg, args);
	}

	public static void println(int priority, Class<?> tag, boolean debugOn,
			String msg, Object... args) {
		println(priority, tag.getSimpleName(), debugOn, msg, args);
	}

	public static void println(int priority, String tag, boolean debugOn,
			String msg, Object... args) {
		if (android.util.Log.DEBUG >= LOG_LEVEL || debugOn)
			android.util.Log.println(priority, tag, String.format(msg, args));
	}

	public static void printCursor(Class<?> tag, Cursor cursor) {
		printCursor(tag.getSimpleName(), cursor);
	}

	public static void printCursor(String tag, Cursor cursor) {
		if (android.util.Log.DEBUG >= LOG_LEVEL) {
			if (cursor != null) {
				int count = cursor.getColumnCount();
				if (count == 0) {
					android.util.Log.e(tag, "The cursor is empty");
				}
				StringBuilder sb = new StringBuilder("Cursor Column");
				for (int i = 0; i < count; i++) {
					sb.append(cursor.getColumnName(i) + "\t");
				}
				android.util.Log.v(tag, sb.toString());

				if (cursor.moveToFirst()) {
					do {
						sb.delete(0, sb.length());
						sb.append("Cursor Data)");
						for (int i = 0; i < count; i++) {
							sb.append(cursor.getString(i) + "\t");
						}
						android.util.Log.v(tag, sb.toString());
					} while (cursor.moveToNext());
				}
			} else {
				android.util.Log.e(tag, "The cursor is null");
			}
		}
	}

	public static void print(Bundle b) {
		if (android.util.Log.DEBUG >= LOG_LEVEL) {
			if (b != null) {
				Set<String> set = b.keySet();
				if (set.size() > 0) {
					for (String key : set) {
						try {
							Bundle subb = (Bundle) b.get(key);
							if (subb != null && subb.size() > 0)
								print(subb);
						} catch (ClassCastException e) {
							android.util.Log.e(
									"Bundle Data",
									"Key = " + key + " : Value = "
											+ String.valueOf(b.get(key)));
						}
					}
				}
			}
		}
	}

	public static void print(Intent it) {
		if (android.util.Log.DEBUG >= LOG_LEVEL) {
			if (it != null) {
				android.util.Log.e("Intent Content", "TO BE IMPLEMENTED!!!!");
			}
		}
	}

	public static void printStackTrace(Exception e) {
		e.printStackTrace();
	}

	public static void dumpString(String s, Context ctx) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(
					ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
					"dumpString.txt");
			Log.d("LOG", "Dump String to %s", file.getAbsolutePath());
			try {
				OutputStream os = new FileOutputStream(file);
				os.write(s.getBytes());
				os.close();
			} catch (IOException e) {
				Log.w("ExternalStorage", e, "Error writing %s", file);
			}
		} else {
			Log.w("ExternalStorage", "No External Storage or not ready.");
		}
	}
}
