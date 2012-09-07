package com.exfe.android.controller;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.exfe.android.Const;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.util.Tool;

public class PortalFragment extends Fragment implements Observer {

	private ViewFlipper mFlipper;
	private Fragment.ActivityCallBack mCallBack;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_portal, container, false);
		return v;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		View v = null;

		v = view.findViewById(R.id.btn_sign_in);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.x_title_and_definition);
		if (v != null) {
			mFlipper = (ViewFlipper) v;
		}

		v = view.findViewById(R.id.btn_login_way_1);
		if (v != null) {
			v.setOnClickListener(mClickListener);
		}

		v = view.findViewById(R.id.btn_login_way_2);
		if (v != null) {
			v.setOnClickListener(mClickListener);
			v.setTag(Provider.STR_TWITTER);
		}

		v = view.findViewById(R.id.x_exfe_title);
		if (v != null) {
			TextView tv = (TextView) v;
			SpannableString text = new SpannableString(tv.getText());
			text.setSpan(new RelativeSizeSpan(16 / 9f), 0, tv.getText()
					.toString().indexOf("\n"), 0);
			text.setSpan(new RelativeSizeSpan(3 / 8f), tv.getText().toString()
					.indexOf("["), tv.getText().toString().indexOf("]") + 1, 0);
			tv.setText(text);
		}

		v = view.findViewById(R.id.x_cross_title);
		if (v != null) {
			TextView tv = (TextView) v;
			CharSequence text = Tool.highlightFirstLine(tv.getText(), false,
					new RelativeSizeSpan(16 / 9f));
			tv.setText(text);
		}

		@SuppressWarnings("deprecation")
		final AbsoluteLayout container = (AbsoluteLayout) getActivity()
				.findViewById(R.id.x_button_layer);

		container.postDelayed(new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				int w = container.getWidth();
				int h = container.getHeight();
				float r = Math.round(Math.min(w, h) * 0.48);
				if (r == 0) {
					container.postDelayed(this, 300);
					return;
				}

				double[] arg = { Math.PI / 4, Math.PI * 5 / 6, Math.PI * 5 / 4,
						Math.PI * 11 / 6 };
				int[] ids = { R.id.x_handy_button, R.id.x_safe_button,
						R.id.x_cross_button, R.id.x_rsvp_button };
				View v = null;
				for (int i = 0; i < 4; i++) {
					v = container.findViewById(ids[i]);
					if (v != null) {
						AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) v
								.getLayoutParams();
						params.x = (int) (w / 2 + Math.cos(arg[i]) * r)
								- v.getWidth() / 2;
						params.y = (int) (h / 2 + Math.sin(arg[i]) * r)
								- v.getHeight() / 2;
						v.setLayoutParams(params);
						v.setVisibility(View.VISIBLE);
						v.requestLayout();
						container.recomputeViewAttributes(v);
					}
				}
				container.requestLayout();
			}
		}, 300);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(android.app.Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (activity instanceof Fragment.ActivityCallBack) {
			mCallBack = (Fragment.ActivityCallBack) activity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mCallBack = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mFlipper != null) {
			mFlipper.startFlipping();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if (mFlipper != null) {
			mFlipper.stopFlipping();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.exfe.android.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
				if (mCallBack != null) {
					Bundle param = new Bundle();
					param.putInt("action",
							LandingActivity.ACTIVITY_RESULT_SIGNIN);
					param.putString("provider", Provider.STR_EMAIL);
					mCallBack.onSwitch(PortalFragment.this, param);
				}
				break;
			case R.id.btn_login_way_1:
			case R.id.btn_login_way_2:
				String tag = (String) v.getTag();
				if (mCallBack != null) {
					Bundle param = new Bundle();
					param.putInt("action",
							LandingActivity.ACTIVITY_RESULT_SIGNIN);
					param.putString("provider", tag);
					mCallBack.onSwitch(PortalFragment.this, param);
				}
			default:
				break;
			}
		}
	};

}
