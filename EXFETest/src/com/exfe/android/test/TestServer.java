/**
 * 
 */
package com.exfe.android.test;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;

import com.exfe.android.Const;
import com.exfe.android.controller.CrossActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;

/**
 * @author stony
 * 
 */
public class TestServer extends ActivityInstrumentationTestCase2<CrossActivity> {
	public static final String TAG = TestServer.class.getSimpleName();

	private Model mModel = null;
	private CrossActivity mActivity = null;

	/**
	 * @param name
	 */
	public TestServer() {
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

	/**
	 * Test method for
	 * {@link com.exfe.android.net.ServerAPI1#checkLogin(java.lang.String, java.lang.String)}
	 * .
	 */
	public final void testCheckLogin() {

		// {"meta":{"code":200},"response":{"userid":71,"auth_token":"a133d99d125902566165cb483a4627d7"}}
		String jsonStr = mModel.getServerv1().checkLogin(Const.username,
				Const.pwd);
		try {
			JSONObject json = new JSONObject(jsonStr);
			assertEquals(200, json.getJSONObject("meta").getInt("code"));
			assertEquals(71, json.getJSONObject("response").getInt("userid"));
			assertNotNull(json.getJSONObject("response")
					.getString("auth_token"));
			// "a133d99d125902566165cb483a4627d7"
		} catch (JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// {"meta":{"code":404,"err":"login error"}}
		jsonStr = mModel.getServerv1().checkLogin(Const.username, Const.EMPTY);
		try {
			JSONObject json = new JSONObject(jsonStr);
			assertEquals(404, json.getJSONObject("meta").getInt("code"));
			assertEquals("login error",
					json.getJSONObject("meta").getString("err"));
		} catch (JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	

	/**
	 * Test method for
	 * {@link com.exfe.android.net.ServerAPI1#sendRSVP(int, java.lang.String)}.
	 */
	public final void testSendRSVP() {

		// http://api.exfe.com/v1/x/123/no?token=YsXM_E26Sq8rznxf_CeO
		// {"meta":{"code":403,"error":"forbidden"}}
		String jsonStr = mModel.getServerv1().sendRSVP(123, "yes");
		try {
			Log.d(TAG, "RESULT: %s", jsonStr);
			JSONObject json = new JSONObject(jsonStr);
			assertEquals(403, json.getJSONObject("meta").getInt("code"));
			assertEquals("forbidden",
					json.getJSONObject("meta").getString("error"));
		} catch (JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public final void testGetCrosses() {
		assertFalse("Missing token", TextUtils.isEmpty(mModel.Me().getToken()));

		Response response = mModel.getServer().getCrosses();
		assertEquals(200, response.getCode());

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
	
	public final void testGetUpdatedCrosses() {
		assertFalse("Missing token", TextUtils.isEmpty(mModel.Me().getToken()));

		@SuppressWarnings("deprecation")
		Date d = new Date("25 Apr 2012");
		Response response = mModel.getServer().getCrossesUpdatedAfter(d);
		assertEquals(200, response.getCode());

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








}
