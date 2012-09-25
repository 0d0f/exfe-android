package com.exfe.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class RSVPAvatar extends CompoundButton {

	private Drawable mAvatar;
	private Drawable mHostDrawable;
	private Drawable mMatesDrawable;
	private int mMates;
	private Drawable mRSVPDrawable;
	private int mRSVP;

	public RSVPAvatar(Context context) {
		this(context, null, 0);
		// TODO Auto-generated constructor stub
	}

	public RSVPAvatar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public RSVPAvatar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		//LayerDrawable dr = (LayerDrawable) getResources().getDrawable(R.drawable.rsvp_avatar);
		
	}

	/**
	 * @return the avatar
	 */
	public Drawable getAvatar() {
		return this.mAvatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(Drawable avatar) {
		this.mAvatar = avatar;
	}

	/**
	 * @return the hostDrawable
	 */
	public Drawable getHostDrawable() {
		return this.mHostDrawable;
	}

	/**
	 * @param hostDrawable
	 *            the hostDrawable to set
	 */
	public void setHostDrawable(Drawable hostDrawable) {
		this.mHostDrawable = hostDrawable;
	}

	/**
	 * @return the matesDrawable
	 */
	public Drawable getMatesDrawable() {
		return this.mMatesDrawable;
	}

	/**
	 * @param matesDrawable
	 *            the matesDrawable to set
	 */
	public void setMatesDrawable(Drawable matesDrawable) {
		this.mMatesDrawable = matesDrawable;
	}

	/**
	 * @return the mates
	 */
	public int getMates() {
		return this.mMates;
	}

	/**
	 * @param mates
	 *            the mates to set
	 */
	public void setMates(int mates) {
		this.mMates = mates;
	}

	/**
	 * @return the rSVPDrawable
	 */
	public Drawable getRSVPDrawable() {
		return this.mRSVPDrawable;
	}

	/**
	 * @param rSVPDrawable
	 *            the rSVPDrawable to set
	 */
	public void setRSVPDrawable(Drawable rSVPDrawable) {
		this.mRSVPDrawable = rSVPDrawable;
	}

	/**
	 * @return the rSVP
	 */
	public int getRSVP() {
		return this.mRSVP;
	}

	/**
	 * @param rSVP
	 *            the rSVP to set
	 */
	public void setRSVP(int rSVP) {
		this.mRSVP = rSVP;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

	}

}
