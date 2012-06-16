package com.exfe.android.test;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;

import com.exfe.android.controller.CrossActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.CrossTime;
import com.exfe.android.model.entity.EFTime;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.util.Tool;

/**
 * @author stony
 * 
 */
public class TestEntity extends ActivityInstrumentationTestCase2<CrossActivity> {
	public static final String TAG = TestEntity.class.getSimpleName();

	private Model mModel = null;
	private CrossActivity mActivity = null;

	/**
	 * @param name
	 */
	public TestEntity() {
		super(CrossActivity.class);
		Log.d(TAG, "init of TestServer");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Log.d(TAG, "setup of TestServer");
		mActivity = getActivity();
		mModel = mActivity.getModel();
		// target context's resouce.
		// getInstrumentation().getTargetContext().getResources()
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testCross() {
		String crossStr = Helpler.getFromAssets(this, "user_39_crosses.js");
		assertNotNull("test file user39.crosses.js is missing", crossStr);

		Response response = new Response(crossStr, null, null);

		JSONObject res = response.getResponse();
		JSONArray array = res.optJSONArray("crosses");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.optJSONObject(i);
				if (json != null) {
					Cross cross = (Cross) EntityFactory.create(json);
					Log.d(TAG, "%s", json.toString());
					assertTrue(cross.getId() > 0);
					Log.d(TAG, "%s", cross.toJSON().toString());
				}
			}
		}
	}

	public final void testCrossTime() {
		CrossTime.NOW = new Date(2012, 4, 4, 14, 8, 0);
		Tool.NOW = CrossTime.NOW;
		CrossTime c = null;
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:08:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 2:08:00 pm abc",
				CrossTime.MARK_ORIGINAL);
		assertEquals("2012-04-04 2:08:00 pm abc",
				c.getLongLocalTimeSring("+08:00 CST", null));

		// Time_word (at) Time Date_word (on) Date
		c = new CrossTime(new EFTime("This Week", "", "", "", "+08:00 CST"),
				"This week", CrossTime.MARK_FORMAT);
		assertEquals("This Week", c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "", "+08:00 CST"),
				"2012 4 4", CrossTime.MARK_FORMAT);
		assertEquals("Wed, Apr 4", c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "", "Dinner", "", "+08:00 CST"),
				"dinner", CrossTime.MARK_FORMAT);
		assertEquals("Dinner", c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "", "", "14:08:00", "+08:00 CST"),
				"14:08:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM", c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "",
				"+08:00 CST"), "This week 2012 04 04", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "",
				"+08:00 CST"), "dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "", "", "14:08:00",
				"+08:00 CST"), "14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012 04 04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(
				new EFTime("", "", "Dinner", "14:08:00", "+08:00 CST"),
				"dinner at 14:08", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "14:08 this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "14:08:00",
				"+08:00 CST"), "dinner 14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner",
				"14:08:00", "+08:00 CST"), "dinner 14:08 this week 2012-4-4",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));

		// different target zone format string ??
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring("", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4",
				c.getLongLocalTimeSring("+08:00 PST", null));

		// if Origin, use CrossTime zone
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00 abc",
				CrossTime.MARK_ORIGINAL);
		assertEquals("2012-04-04 14:8:00 abc +08:00 CST",
				c.getLongLocalTimeSring("+09:00 PST", null));

		// Time_word (at) Time Zone Date_word (on) Date
		// Only show Zone with Time_word or Time
		c = new CrossTime(new EFTime("This Week", "", "", "", "+08:00 CST"),
				"this week", CrossTime.MARK_FORMAT);
		assertEquals("This Week", c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "", "+08:00 CST"),
				"2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Wed, Apr 4", c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "", "Dinner", "", "+08:00 CST"),
				"dinner", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "", "", "14:08:00", "+08:00 CST"),
				"14:08", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "",
				"+08:00 CST"), "this week 2012 4 4", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "",
				"+08:00 CST"), "dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "", "", "14:08:00",
				"+08:00 CST"), "14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST This Week",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(
				new EFTime("", "", "Dinner", "14:08:00", "+08:00 CST"),
				"dinner 14:08", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "14:08 this week 2012 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "14:08:00",
				"+08:00 CST"), "14:08 dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST This Week",
				c.getLongLocalTimeSring("+09:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner",
				"14:08:00", "+08:00 CST"), "14:08 dinner this week 2012 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));

		// different target zone format
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring("", null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "14:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring("+09:00 PST", null));

		// different year
		// Time_word (at) Time Date_word (on) Date
		c = new CrossTime(new EFTime("", "2011-04-04", "", "", "+08:00 CST"),
				"2011-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "", "",
				"+08:00 CST"), "this week 2011-04-04", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2011-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2011-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("", "2011-04-04", "", "14:08:00",
				"+08:00 CST"), "2011-04-04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "Dinner", "",
				"+08:00 CST"), "2011-04-04 dinner this week",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "", "14:08:00",
				"+08:00 CST"), "this week 2011-04-04 14:8:00",
				CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "Dinner",
				"14:08:00", "+08:00 CST"), "14:08 this week 2011 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));

		// Time Zone & Different year
		c = new CrossTime(new EFTime("This Week", "2011-12-31", "Dinner",
				"23:08:00", "+08:00 CST"), "23:08 this week 2011 12 31",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 11:08PM This Week on Sat, Dec 31, 2011",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2011-12-31", "Dinner",
				"23:08:00", "-08:00 PST"), "23:08 this week 2011 12 31",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +08:00 CST This Week on Sun, Jan 1",
				c.getLongLocalTimeSring("+08:00 CST", null));
		c = new CrossTime(new EFTime("This Week", "2012-01-01", "Dinner",
				"08:08:00", "+08:00 CST"), "23:08 this week 2012 01 01",
				CrossTime.MARK_FORMAT);
		assertEquals(
				"Dinner at 4:08PM -08:00 PST This Week on Sat, Dec 31, 2011",
				c.getLongLocalTimeSring("-08:00 PST", null));
		c = new CrossTime(new EFTime("This Week", "2012-01-01", "Dinner", "",
				"+08:00 CST"), "23:08 this week 2012 01 01",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week on Sun, Jan 1",
				c.getLongLocalTimeSring("-08:00 PST", null));
	}

	public final void testEntityParseJSON() {
		Entity e = new Cross();
		assertNotNull("abc", e.toJSON());
		printInfo(e, null);

		User u = new User();
		printInfo(u, null);
		assertNotNull("abc", u.toJSON());
	}

	public void printInfo(Entity e, Class<? extends Entity> clz) {
		if (clz == null) {
			clz = e.getClass();
		}
		Log.d(TAG, "=====%s Start=====", clz.getName());

		Class s = clz.getSuperclass();
		if (!clz.equals(Entity.class)) {
			printInfo(e, s);
		}

		Field[] flds = clz.getDeclaredFields();
		for (Field f : flds) {
			Log.d(TAG, "parseJSON: field name:%s, type:%s, modifier: %d",
					f.getName(), f.getType(), f.getModifiers());
		}

		/*
		 * Method[] mths = clz.getMethods(); for(Method m: mths){ Log.d(TAG,
		 * "parseJSON: method name:%s, returntype:%s,  modifier: %d",
		 * m.getName(), m.getReturnType(), m.getModifiers()); }
		 */
		Log.d(TAG, "=====%s End=====", clz.getName());
	}

	public void testIsJson() {

		String data = "{\"meta\":{\"code\":200},\"response\":{\"device_token\":null,\"identity_id\":\"100\"}}";
		Pattern p1 = Pattern.compile("^\\{.*\\}$");
		Matcher m = p1.matcher(data);
		if (m.find()) {
			Log.d("RegEx", "match: %s", m.group());
		}

		assertEquals(true, Pattern.matches("\\{.*?\\}", data));
		// assertEquals(true, Pattern.matches("^\\{", data));
		assertEquals(true, Pattern.matches("^\\{.*", data));
		assertEquals(true, Pattern.matches("^\\{.*\\}", data));
		// assertEquals(true, Pattern.matches("\\}$", data));
		assertEquals(true, Pattern.matches(".*\\}$", data));
		assertEquals(true, Pattern.matches("\\{.*\\}$", data));
		assertEquals(true, Pattern.matches("^\\{.*\\}$", data));
	}
}
