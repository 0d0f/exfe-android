package com.exfe.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShapeImageView extends ImageView {

	private final Path mTmpPath = new Path();
	private final RectF mTmpRectF = new RectF();
	private final Rect mTmpRect = new Rect();
	private float mRadius = 8f;

	private PaintDrawable mMask = new PaintDrawable();

	public ShapeImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ShapeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ShapeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		getDrawingRect(mTmpRect);
		final RectF rf = mTmpRectF;
		rf.set(mTmpRect);

		final Path path = mTmpPath;
		path.reset();
		path.addRoundRect(rf, mRadius, mRadius, Path.Direction.CW);

		canvas.save();
		canvas.clipPath(path);
		super.onDraw(canvas);
		canvas.restore();

		mMask.setCornerRadius(mRadius);
		mMask.getPaint().setColor(0x99FFFFFF);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
	}

	public float getRadius() {
		return mRadius;
	}

	public void setRadius(float raduis) {
		this.mRadius = raduis;
	}
}
