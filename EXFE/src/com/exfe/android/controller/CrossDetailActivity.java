package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.exfe.android.Activity;
import com.exfe.android.R;
import com.exfe.android.model.entity.Cross;
import com.flurry.android.FlurryAgent;

public class CrossDetailActivity extends Activity implements Observer {

	public static final String FIELD_CROSS_ID = "cross_id";
	public static final String FIELD_CROSS_SIDE = "cross_side";

	private long mCrossId = 0;
	private boolean mSideA = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_cross_detail);

		Intent it = getIntent();
		mCrossId = it.getLongExtra(FIELD_CROSS_ID, 0L);
		mSideA = it.getBooleanExtra(FIELD_CROSS_SIDE, true);

		View v = findViewById(R.id.nav_btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_btn_action);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_title);
		if (v != null && mCrossId != 0) {
			TextView tv = (TextView) v;
			Cross x = mModel.Crosses().getCrossById(mCrossId);
			if (x != null) {
				tv.setText(x.getTitle());
			}
		}

		showFragment(mSideA);
		mSideA = !mSideA;
	}

	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	protected void showFragment(boolean sideA) {
		Fragment fragment = null;

		if (sideA) {
			fragment = new CrossDetailFragment();
			FlurryAgent.logEvent("Fragment_detail");
		} else {
			fragment = new CrossConversationFragment();
			FlurryAgent.logEvent("Fragment_conversation");
		}

		Bundle args = new Bundle();
		args.putLong(CrossConversationFragment.FIELD_CROSS_ID, mCrossId);
		fragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.x_container, fragment).commit();
	}

	View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.nav_btn_back:
				final Intent intent = new Intent(v.getContext(),
						LandingActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				break;
			case R.id.nav_btn_action:
				showFragment(mSideA);
				mSideA = !mSideA;
				break;
			default:
				break;
			}
		}
	};

}
