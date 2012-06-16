package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.R.anim;
import com.exfe.android.R.id;
import com.exfe.android.R.layout;
import com.exfe.android.R.string;
import com.exfe.android.debug.Log;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class PortalActivity extends Activity implements Observer {

	private static final int ACTIVITY_RESULT_CROSS = 12345;
	private static final int ACTIVITY_RESULT_SIGNIN = 12346;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);

		setContentView(R.layout.scr_portal);

		if (!TextUtils.isEmpty(mModel.Me().getUsername())) {
			jumpToCross();
		}

		View v = null;

		v = findViewById(R.id.btn_sign_in);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.btn_new_user);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.exfe_and_x_defininition);
		if (v != null) {
			TextView x = (TextView) v;
			// Resources res = getResources();
			// CharSequence styledText = Html.fromHtml(res
			// .getString(R.string.exfe_and_x_definition));
			String htmlcode = "<div align=\"center\"><font size=\"40pt\" color=\"#1175A5\">EXFE</font>[ˈɛksfi]"
					+ "<div>is an utilit for hanging out with friends.</div>"
					+ "<div>Stop calling up every one RSVP, losing in endless emails and messages off the point.</div></div>"
					+ "<div align=\"right\"><font color=\"#1175A5\">X</font>[krɔs]"
					+ "<div>is a gathering of people, on purpose or not.</div>"
					+ "<div>All <font size=\"40pt\" color=\"#1175A5\">X</font> are private by default, accessible to only attendees.</div></div>";

			CharSequence styledText = Html.fromHtml(htmlcode);
			x.setText(styledText);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.exfe.android.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "RESULT: %d, %d", requestCode, resultCode);
		switch (requestCode) {
		case ACTIVITY_RESULT_CROSS:
			if (resultCode == CrossActivity.RESULT_EXIT_APP) {
				finish();
				Log.d(TAG, "CROSS CANCEL: Exit app");
			} else if (resultCode == RESULT_OK) {
				// login successfully
				Log.d(TAG, "CROSS OK");
			} else {
				Log.d(TAG, "CROSS OTHERS");
			}
			break;
		case ACTIVITY_RESULT_SIGNIN:
			if (resultCode == CrossActivity.RESULT_EXIT_APP) {
				finish();
				Log.d(TAG, "LOGIN CANCEL: Exit app");
			} else if (resultCode == RESULT_OK) {
				// login successfully
				jumpToCross();
				Log.d(TAG, "LOGIN OK: Login successfully");
			} else {
				Log.d(TAG, "LOGIN OTHERS");
			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.btn_sign_in:
				Intent it = new Intent();
				it.setClass(v.getContext(), LoginActivity.class);
				startActivityForResult(it, ACTIVITY_RESULT_SIGNIN);
				overridePendingTransition(R.anim.slide_up_in, 0);
				break;
			case R.id.btn_new_user:
				// 1 regist new user using device token.
				// 2 jump to cross activity;

				break;
			default:
				break;
			}
		}
	};

	private void jumpToCross() {
		Log.d(TAG, "Jump to Cross");
		Intent it = new Intent();
		it.setClass(this, CrossActivity.class);
		startActivityForResult(it, ACTIVITY_RESULT_CROSS);
	}

	private void registC2DM() {
		Intent regIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		regIntent.putExtra(Const.C2DM_FIELD_APP,
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		regIntent.putExtra(Const.C2DM_FIELD_SENDER, Const.PUSH_SERVER_ACCOUNT);
		startService(regIntent);
	}

}
