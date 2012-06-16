package com.exfe.android.util;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;

// http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
public class Flip3dAnimation extends Animation {
	private float mFromDegrees;
	private float mToDegrees;
	private float mCenterX;
	private float mCenterY;
	private Camera mCamera;

	public Flip3dAnimation(){
		super();
	}
	
	public Flip3dAnimation(Context context, AttributeSet attrs){
		super(context, attrs);
		// TODO load attrs from xml
	}
	
	public Flip3dAnimation(float fromDegrees, float toDegrees, float centerX,
			float centerY) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees
				+ ((mToDegrees - fromDegrees) * interpolatedTime);
		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;
		
		final Matrix matrix = t.getMatrix();
		
		camera.save();
		camera.rotateY(degrees);
		camera.getMatrix(matrix);
		camera.restore();
		
		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}
}
