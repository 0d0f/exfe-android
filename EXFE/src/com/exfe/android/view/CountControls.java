package com.exfe.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.exfe.android.R;

public class CountControls extends LinearLayout {

	private final ImageButton mZoomIn;
	private final ImageButton mZoomOut;
	private final TextView mText;

	public CountControls(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public CountControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		setFocusable(false);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.comp_counter, this, true);

		mZoomIn = (ImageButton) findViewById(R.id.zoom_in);
		mZoomOut = (ImageButton) findViewById(R.id.zoom_out);
		mText = (TextView) findViewById(R.id.zoom_text);
	}

	public void setOnZoomInClickListener(View.OnClickListener listener) {
		mZoomIn.setOnClickListener(listener);
	}

	public void setOnZoomOutClickListener(View.OnClickListener listener) {
		mZoomOut.setOnClickListener(listener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		/*
		 * Consume all touch events so they don't get dispatched to the view
		 * beneath this view.
		 */
		return true;
	}

	public void show() {
		fade(View.VISIBLE, 0.0f, 1.0f);
	}

	public void hide() {
		fade(View.GONE, 1.0f, 0.0f);
	}

	private void fade(int visibility, float startAlpha, float endAlpha) {
		AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
		anim.setDuration(500);
		startAnimation(anim);
		setVisibility(visibility);
	}

	public void setIsZoomInEnabled(boolean isEnabled) {
		mZoomIn.setEnabled(isEnabled);
	}

	public void setIsZoomOutEnabled(boolean isEnabled) {
		mZoomOut.setEnabled(isEnabled);
	}

	@Override
	public boolean hasFocus() {
		return mZoomIn.hasFocus() || mZoomOut.hasFocus();
	}
	
	public TextView getTextView(){
		return this.mText;
	}

}
