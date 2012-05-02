package com.exfe.android.test;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;

import com.exfe.android.CrossActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.CrossTime;
import com.exfe.android.model.entity.EFTime;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;

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
		String crossStr = Tool.getFromAssets(this, "user_39_crosses.js");
		assertNotNull("test file user39.crosses.js is missing", crossStr);
		
		Response response = new Response(crossStr);
		
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
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		CrossTime c = new CrossTime(new EFTime("", "2012-06-30", "", "14:08:00", "+08:00 CST"), "2012-04-04 14:08:00", 
				CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on 2012", c.getLongLocalTimeSring("+08:00 CST"));
		
		c = new CrossTime(new EFTime("", "2012-06-30", "", "14:08:00", "+08:00 CST"), "2012-04-04 2:08:00 pm abc", 
				CrossTime.MARK_ORIGINAL);
		assertEquals("2012-04-04 2:08:00 pm abc", c.getLongLocalTimeSring("+08:00 CST"));
		
	}
	
	public final void testEntityParseJSON() {
		Entity e = new Entity();
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
}
