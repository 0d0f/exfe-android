package com.exfe.android.controller;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.R;
import com.exfe.android.model.CrossesModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.flurry.android.FlurryAgent;

public class CrossDetailActivity extends Activity implements Observer {

	public static final String FIELD_CROSS_ID = "cross_id";
	public static final String FIELD_CROSS_SIDE = "cross_side";

	private long mCrossId = 0;
	private boolean mSideA = true;

	private ImageButton mBtnAction = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		setContentView(R.layout.activity_cross_detail);

		Intent it = getIntent();
		mCrossId = it.getLongExtra(FIELD_CROSS_ID, 0L);
		mSideA = it.getBooleanExtra(FIELD_CROSS_SIDE, true);
		boolean needRefresh = it.getBooleanExtra(Const.PUSH_FIELD_SOURCE_PUSH,
				false);

		View v = findViewById(R.id.nav_btn_back);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = findViewById(R.id.nav_btn_action);
		if (v != null) {
			mBtnAction = (ImageButton) v;
			v.setOnClickListener(mClickListener);
		}

		Date lastUpdate = null;
		v = findViewById(R.id.nav_title);
		if (v != null && mCrossId != 0) {
			TextView tv = (TextView) v;
			Cross x = mModel.Crosses().getCrossById(mCrossId);
			if (x != null) {
				tv.setText(x.getTitle());
				lastUpdate = x.getUpdateAt();
			} else {
				needRefresh = true;
			}
		}

		mModel.Crosses().fetchCross(mCrossId, lastUpdate);

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
		final Bundle bundle = (Bundle) data;
		int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
		switch (type) {
		case CrossesModel.ACTION_TYPE_REMOVE_CROSS:
			mModel.mHandler.post(new Runnable() {

				@Override
				public void run() {
					long cross_id = bundle
							.getLong(CrossesModel.FIELD_CHANGE_ID);
					if (cross_id == mCrossId) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								CrossDetailActivity.this);
						AlertDialog dialog = builder
								.setTitle(R.string.warning)
								.setMessage(
										Html.fromHtml(getResources()
												.getString(
														R.string.you_hove_no_access_to_the_private_x)))
								.setPositiveButton(R.string.i_see,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();

											}
										}).create();
						dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								performBack();
							}
						});
						dialog.show();
					}
				}
			});

			break;
		}
	}

	protected void showFragment(boolean sideA) {
		Fragment fragment = null;

		if (sideA) {
			fragment = new CrossDetailFragment();
			FlurryAgent.logEvent("Fragment_detail");
			mBtnAction.setImageResource(R.drawable.conv_navbarbtn);
		} else {
			fragment = new CrossConversationFragment();
			FlurryAgent.logEvent("Fragment_conversation");
			mBtnAction.setImageResource(R.drawable.x_navbarbtn);
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
				performBack();
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

	void performBack() {
		final Intent intent = new Intent(this, LandingActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

}
