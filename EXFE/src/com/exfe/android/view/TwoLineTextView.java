package com.exfe.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

public class TwoLineTextView extends DoubleTextView {

	private TextPaint p1 = new TextPaint();
	private TextPaint p2 = new TextPaint();
	private TextPaint p = new TextPaint();

	public TwoLineTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TwoLineTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TwoLineTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
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
		p1.setTextAlign(Align.CENTER);
		p1.setFakeBoldText(true);
		p2.set(paint);
		p2.setTextAlign(Align.CENTER);
		p2.setFakeBoldText(true);
		p2.setTextSize(paint.getTextSize() * 9f / 13f);

		float x = this.getWidth() / 2;
		float y = this.getHeight() / 2;

		float offset = paint.ascent() + paint.descent();
		float dx = -offset * 0.2f;
		float dy = offset * 0.35f;

		CharSequence text = getText();
		if (!TextUtils.isEmpty(text)) {
			canvas.drawText(text.toString(), x, y  - offset, p1);
		}
		CharSequence alttext = getAltText();
		if (!TextUtils.isEmpty(alttext)) {
			canvas.drawText(alttext.toString(), x, y + dy, p2);
		}
	}

}
