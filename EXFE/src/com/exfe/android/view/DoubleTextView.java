package com.exfe.android.view;

import com.exfe.android.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class DoubleTextView extends TextView {

	private CharSequence mAltText = null;

	public DoubleTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mAltText = "";
	}

	public DoubleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mAltText = "";

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DoubleTextView);
		mAltText = a.getString(R.styleable.DoubleTextView_altText);
		a.recycle();
	}

	public DoubleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mAltText = "";

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DoubleTextView);
		mAltText = a.getString(R.styleable.DoubleTextView_altText);
		a.recycle();

	}

	public CharSequence getAltText() {
		return mAltText;
	}
	
	public void setAltText(CharSequence text){
		mAltText = text;
	}

	@Override
	public int length() {
		return super.length() + mAltText.length();
	}

}
