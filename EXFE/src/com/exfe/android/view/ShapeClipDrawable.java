package com.exfe.android.view;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.exfe.android.R;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.os.Build;
import android.util.AttributeSet;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * A Drawable that clips another Drawable based on this Drawable's current level
 * value. You can control how much the child Drawable gets clipped in width and
 * height based on the level, as well as a gravity to control where it is placed
 * in its overall container. Most often used to implement things like progress
 * bars, by increasing the drawable's level with
 * {@link android.graphics.drawable.Drawable#setLevel(int) setLevel()}.
 * <p class="note">
 * <strong>Note:</strong> The drawable is clipped completely and not visible
 * when the level is 0 and fully revealed when the level is 10,000.
 * </p>
 * 
 * <p>
 * It can be defined in an XML file with the
 * <code>&lt;clip></code> element.  For more
 * information, see the guide to <a
 * href="{@docRoot} element. For more information, see the guide to <a
 * href="{@docRoot} element. For more information, see the guide to <a
 * href="{@docRoot} element. For more information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable
 * Resources</a>.
 * </p>
 * 
 * @attr ref android.R.styleable#ClipDrawable_clipOrientation
 * @attr ref android.R.styleable#ClipDrawable_gravity
 * @attr ref android.R.styleable#ClipDrawable_drawable
 */
public class ShapeClipDrawable extends Drawable implements Drawable.Callback {
	private ClipState mClipState;
	private final Rect mTmpRect = new Rect();

	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;

	ShapeClipDrawable() {
		this(null, null);
	}

	/**
	 * @param orientation
	 *            Bitwise-or of {@link #HORIZONTAL} and/or {@link #VERTICAL}
	 */
	public ShapeClipDrawable(Drawable drawable, int gravity, int orientation) {
		this(null, null);

		mClipState.mDrawable = drawable;
		mClipState.mGravity = gravity;
		mClipState.mOrientation = orientation;

		if (drawable != null) {
			drawable.setCallback(this);
		}
	}

	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		super.inflate(r, parser, attrs);

		int type;

		TypedArray a = r.obtainAttributes(attrs, R.styleable.ShapeClipDrawable);

		int orientation = a.getInt(R.styleable.ShapeClipDrawable_clipOrientation,
				HORIZONTAL);
		int g = a.getInt(R.styleable.ShapeClipDrawable_gravity, Gravity.LEFT);
		Drawable dr = a.getDrawable(R.styleable.ShapeClipDrawable_drawable);

		a.recycle();

		final int outerDepth = parser.getDepth();
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
			if (type != XmlPullParser.START_TAG) {
				continue;
			}
			dr = Drawable.createFromXmlInner(r, parser, attrs);
		}

		if (dr == null) {
			throw new IllegalArgumentException(
					"No drawable specified for <clip>");
		}

		mClipState.mDrawable = dr;
		mClipState.mOrientation = orientation;
		mClipState.mGravity = g;

		dr.setCallback(this);
	}

	// overrides from Drawable.Callback

	@SuppressLint("NewApi")
	public void invalidateDrawable(Drawable who) {
		Drawable.Callback cb = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			cb = getCallback();
		} else {
			// cb = mCallback;
			Field f;
			try {
				f = Drawable.class.getDeclaredField("mCallback");
				cb = (Callback) f.get(this);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (cb != null) {
			cb.invalidateDrawable(this);
		}
	}

	@SuppressLint("NewApi")
	public void scheduleDrawable(Drawable who, Runnable what, long when) {

		Drawable.Callback cb = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			cb = getCallback();
		} else {
			// cb = mCallback;
			Field f;
			try {
				f = Drawable.class.getDeclaredField("mCallback");
				cb = (Callback) f.get(this);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (cb != null) {
			cb.scheduleDrawable(this, what, when);
		}
	}

	@SuppressLint("NewApi")
	public void unscheduleDrawable(Drawable who, Runnable what) {
		Drawable.Callback cb = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			cb = getCallback();
		} else {
			// cb = mCallback;
			Field f;
			try {
				f = Drawable.class.getDeclaredField("mCallback");
				cb = (Callback) f.get(this);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (cb != null) {
			cb.unscheduleDrawable(this, what);
		}
	}

	// overrides from Drawable

	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations()
				| mClipState.mChangingConfigurations
				| mClipState.mDrawable.getChangingConfigurations();
	}

	@Override
	public boolean getPadding(Rect padding) {
		// XXX need to adjust padding!
		return mClipState.mDrawable.getPadding(padding);
	}

	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		mClipState.mDrawable.setVisible(visible, restart);
		return super.setVisible(visible, restart);
	}

	@Override
	public void setAlpha(int alpha) {
		mClipState.mDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mClipState.mDrawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return mClipState.mDrawable.getOpacity();
	}

	@Override
	public boolean isStateful() {
		return mClipState.mDrawable.isStateful();
	}

	@Override
	protected boolean onStateChange(int[] state) {
		return mClipState.mDrawable.setState(state);
	}

	@Override
	protected boolean onLevelChange(int level) {
		mClipState.mDrawable.setLevel(level);
		invalidateSelf();
		return true;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		mClipState.mDrawable.setBounds(bounds);
	}

	@Override
	public void draw(Canvas canvas) {

		if (mClipState.mDrawable.getLevel() == 0) {
			return;
		}

		final Rect r = mTmpRect;
		final Rect bounds = getBounds();
		int level = getLevel();
		int w = bounds.width();
		final int iw = 0; // mClipState.mDrawable.getIntrinsicWidth();
		if ((mClipState.mOrientation & HORIZONTAL) != 0) {
			w -= (w - iw) * (10000 - level) / 10000;
		}
		int h = bounds.height();
		final int ih = 0; // mClipState.mDrawable.getIntrinsicHeight();
		if ((mClipState.mOrientation & VERTICAL) != 0) {
			h -= (h - ih) * (10000 - level) / 10000;
		}
		Gravity.apply(mClipState.mGravity, w, h, bounds, r);

		if (w > 0 && h > 0) {
			canvas.save();
			canvas.clipRect(r);
			mClipState.mDrawable.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	public int getIntrinsicWidth() {
		return mClipState.mDrawable.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return mClipState.mDrawable.getIntrinsicHeight();
	}

	@Override
	public ConstantState getConstantState() {
		if (mClipState.canConstantState()) {
			mClipState.mChangingConfigurations = super
					.getChangingConfigurations();
			return mClipState;
		}
		return null;
	}

	final static class ClipState extends ConstantState {
		Drawable mDrawable;
		int mChangingConfigurations;
		int mOrientation;
		int mGravity;

		private boolean mCheckedConstantState;
		private boolean mCanConstantState;

		ClipState(ClipState orig, ShapeClipDrawable owner, Resources res) {
			if (orig != null) {
				if (res != null) {
					mDrawable = orig.mDrawable.getConstantState().newDrawable(
							res);
				} else {
					mDrawable = orig.mDrawable.getConstantState().newDrawable();
				}
				mDrawable.setCallback(owner);
				mOrientation = orig.mOrientation;
				mGravity = orig.mGravity;
				mCheckedConstantState = mCanConstantState = true;
			}
		}

		@Override
		public Drawable newDrawable() {
			return new ShapeClipDrawable(this, null);
		}

		@Override
		public Drawable newDrawable(Resources res) {
			return new ShapeClipDrawable(this, res);
		}

		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}

		boolean canConstantState() {
			if (!mCheckedConstantState) {
				mCanConstantState = mDrawable.getConstantState() != null;
				mCheckedConstantState = true;
			}

			return mCanConstantState;
		}
	}

	private ShapeClipDrawable(ClipState state, Resources res) {
		mClipState = new ClipState(state, this, res);
	}
}
