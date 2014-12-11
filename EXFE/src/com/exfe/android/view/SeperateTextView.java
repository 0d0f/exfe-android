package com.exfe.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

public class SeperateTextView extends DoubleTextView {

	private String mSeperateString = null;
	private TextPaint p1 = new TextPaint();
	private TextPaint p2 = new TextPaint();
	private TextPaint p = new TextPaint();

	public SeperateTextView(Context context) {
		super(context);
		mSeperateString = "/";
	}

	public SeperateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSeperateString = "/";
	}

	public SeperateTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mSeperateString = "/";

	}
	
	@Override
	public int length() {
		int len = TextUtils.isEmpty(mSeperateString)?0:mSeperateString.length();
		return super.length() + len;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		final TextPaint paint = getPaint();
		paint.setColor(getTextColors().getColorForState(getDrawableState(),
				paint.getColor()));
		p1.set(paint);
		p1.setTextAlign(Align.RIGHT);
		p1.setFakeBoldText(true);
		p2.set(paint);
		p2.setTextAlign(Align.LEFT);
		p.set(paint);
		p.setTextAlign(Align.CENTER);
		p.setTextSize(paint.getTextSize() * 1.1f);

		float x = this.getWidth() / 2;
		float y = this.getHeight() / 2;

		float offset = paint.ascent() + paint.descent();
		float dx = -offset * 0.2f;
		float dy = offset * 0.2f;
		if (!TextUtils.isEmpty(mSeperateString)) {
			canvas.drawText(mSeperateString, x, y - offset / 2, p);
		}
		CharSequence text = getText().toString();
		if (text != null) {
			canvas.drawText(text.toString(), x - dx, y - dy, p1);
		}
		CharSequence altText = getAltText();
		if (altText != null) {
			canvas.drawText(altText.toString(), x + dx, y - offset + dy, p2);
		}
		
	}

}
