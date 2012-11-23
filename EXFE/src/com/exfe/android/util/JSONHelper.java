package com.exfe.android.util;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

public class JSONHelper {

	public static String optString(JSONObject json, String path) {
		return optString(json, path, "");
	}

	public static String optString(JSONObject json, String path, String fallback) {
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optString(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optString(s);
					}
				}
			}
		}
		return fallback;
	}

	public static double optDouble(JSONObject json, String path) {
		return optDouble(json, path, 0);
	}

	public static double optDouble(JSONObject json, String path, double fallback) {
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optDouble(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optDouble(s);
					}
				}
			}
		}
		return fallback;
	}

	public static boolean optBoolean(JSONObject json, String path) {
		return optBoolean(json, path, false);
	}

	public static boolean optBoolean(JSONObject json, String path,
			boolean fallback) {
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optBoolean(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optBoolean(s);
					}
				}
			}
		}
		return fallback;
	}

	public static int optInt(JSONObject json, String path) {
		return optInt(json, path, 0);
	}

	public static int optInt(JSONObject json, String path, int fallback) {
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optInt(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optInt(s);
					}
				}
			}
		}
		return fallback;
	}

	public static long optLong(JSONObject json, String path) {
		return optLong(json, path, 0);
	}

	public static long optLong(JSONObject json, String path, long fallback) {
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optLong(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optLong(s);
					}
				}
			}
		}
		return fallback;
	}

	public static JSONArray optJSONArray(JSONObject json, String path) {
		JSONArray fallback = null;
		String[] abc = path.split("/");
		int count = 0;
		Object jo = json;
		for (String s : abc) {
			count++;
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (count < abc.length) {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							jo = ((JSONArray) jo).optJSONObject(index);
						} catch (NumberFormatException e) {
							jo = null;
						}
					} else {
						jo = ((JSONObject) jo).optJSONObject(s);
					}
				} else {
					if (s.startsWith("[") && s.endsWith("]")) {
						try {
							int index = Integer.valueOf(s.substring(1,
									s.length() - 2));
							return ((JSONArray) jo).optJSONArray(index);
						} catch (NumberFormatException e) {
							return fallback;
						}
					} else {
						return ((JSONObject) jo).optJSONArray(s);
					}
				}
			}
		}
		return fallback;
	}

	public static JSONObject optJSONObject(JSONObject json, String path) {
		JSONObject fallback = null;
		String[] abc = path.split("/");
		Object jo = json;
		for (String s : abc) {
			if (!TextUtils.isEmpty(s)) {
				if (jo == null) {
					return fallback;
				}
				if (s.startsWith("[") && s.endsWith("]")) {
					try {
						int index = Integer.valueOf(s.substring(1,
								s.length() - 2));
						jo = ((JSONArray) jo).optJSONObject(index);
					} catch (NumberFormatException e) {
						jo = null;
					}
				} else {
					jo = ((JSONObject) jo).optJSONObject(s);
				}
			}
		}
		return (JSONObject) jo;
	}
}
