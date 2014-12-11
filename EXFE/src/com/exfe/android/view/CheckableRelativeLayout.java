package com.exfe.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements
		Checkable {

	private boolean mChecked;

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public CheckableRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	public boolean performClick() {
		/*
		 * XXX: These are tiny, need some surrounding 'expanded touch area',
		 * which will need to be implemented in Button if we only override
		 * performClick()
		 */

		/* When clicked, toggle the state */
		toggle();
		return super.performClick();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onCreateDrawableState(int)
	 */
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

}
