package com.exfe.android.view;

import java.sql.SQLException;
import java.util.Iterator;

import com.exfe.android.debug.Log;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;

import android.content.Context;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class IteratorAdapter<T> extends BaseAdapter {

	protected boolean mDataValid;
	protected boolean mAutoRequery;
	protected CloseableIterator<T> mIterator;
	protected Context mContext;

	// protected int mRowIDColumn;
	protected ChangeObserver mChangeObserver;
	protected DataSetObserver mDataSetObserver = new MyDataSetObserver();

	// protected CursorFilter mCursorFilter;
	// protected FilterQueryProvider mFilterQueryProvider;

	public IteratorAdapter(Context context, CloseableIterator<T> iterator) {
		init(context, iterator, true);
	}

	public IteratorAdapter(Context context, CloseableIterator<T> iterator,
			boolean autoRequery) {
		init(context, iterator, autoRequery);
	}

	protected void init(Context context, CloseableIterator<T> iterator,
			boolean autoRequery) {
		boolean iteratorPresent = iterator != null;
		mAutoRequery = autoRequery;
		mIterator = iterator;
		mDataValid = iteratorPresent;
		mContext = context;
		//mRowIDColumn = cursorPresent ? mCursorgetColumnIndexOrThrow("_id") : -1;
		mChangeObserver = new ChangeObserver();
		if (iteratorPresent) {
			registerCursorContentObserver(mChangeObserver);
			registerCursorDataSetObserver(mDataSetObserver);
		}
	}

	public Iterator<T> getIterator() {
		return mIterator;
	}

	@Override
	public int getCount() {
		if (mDataValid && mIterator != null) {
			return getCursorCount();
		} else {
			return 0;
		}
	}

	@Override
	public T getItem(int position) {
		if (mDataValid && mIterator != null) {
			moveCursorToPosition(position);
			try {
				return mIterator.current();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mIterator != null) {
			if (moveCursorToPosition(position)) {
				return getCursorId();
			}
		}
		return 0;

	}

	protected boolean moveCursorToPosition(int position) {
		return ((AndroidDatabaseResults) mIterator.getRawResults())
				.getRawCursor().moveToPosition(position);
	}

	protected int getCursorCount() {
		return ((AndroidDatabaseResults) mIterator.getRawResults())
				.getRawCursor().getCount();
	}

	protected void registerCursorContentObserver(ContentObserver o) {
		((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.registerContentObserver(o);
	}

	protected void registerCursorDataSetObserver(DataSetObserver o) {
		((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.registerDataSetObserver(o);
	}

	protected void unregisterCursorContentObserver(ContentObserver o) {
		((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.unregisterContentObserver(o);
	}

	protected void unregisterCursorDataSetObserver(DataSetObserver o) {
		((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.unregisterDataSetObserver(o);
	}
	
	protected boolean isCursorClosed() {
		return ((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.isClosed();
	}
	
	protected boolean cursorRequery() {
		return ((AndroidDatabaseResults) mIterator.getRawResults()).getRawCursor()
				.requery();
	}
	
	abstract protected long getCursorId();

	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * @see android.widget.ListAdapter#getView(int, View, ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
			throw new IllegalStateException(
					"this should only be called when the cursor is valid");
		}
		if (!moveCursorToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}
		View v;
		if (convertView == null) {
			v = newView(mContext, mIterator, parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, mIterator);
		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (mDataValid) {
			moveCursorToPosition(position);
			View v;
			if (convertView == null) {
				v = newDropDownView(mContext, mIterator, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, mIterator);
			return v;
		} else {
			return null;
		}
	}

	/**
	 * Makes a new view to hold the data pointed to by cursor.
	 * 
	 * @param context
	 *            Interface to application's global information
	 * @param iterator
	 *            The iterator from which to get the data. The cursor is already
	 *            moved to the correct position.
	 * @param parent
	 *            The parent to which the new view is attached to
	 * @return the newly created view.
	 */
	public abstract View newView(Context context, CloseableIterator<T> iterator,
			ViewGroup parent);

	/**
	 * Makes a new drop down view to hold the data pointed to by cursor.
	 * 
	 * @param context
	 *            Interface to application's global information
	 * @param iterator
	 *            The iterator from which to get the data. The cursor is already
	 *            moved to the correct position.
	 * @param parent
	 *            The parent to which the new view is attached to
	 * @return the newly created view.
	 */
	public View newDropDownView(Context context, CloseableIterator<T> iterator,
			ViewGroup parent) {
		return newView(context, iterator, parent);
	}

	/**
	 * Bind an existing view to the data pointed to by cursor
	 * 
	 * @param view
	 *            Existing view, returned earlier by newView
	 * @param context
	 *            Interface to application's global information
	 * @param iterator
	 *            The cursor from which to get the data. The cursor is already
	 *            moved to the correct position.
	 */
	public abstract void bindView(View view, Context context,
			CloseableIterator<T> iterator);

	/**
	 * Change the underlying cursor to a new cursor. If there is an existing
	 * cursor it will be closed.
	 * 
	 * @param cursor
	 *            the new cursor to be used
	 */
	public void changeIterator(CloseableIterator<T> iterator) {
		if (iterator == mIterator) {
			return;
		}
		if (mIterator != null) {
			unregisterCursorContentObserver(mChangeObserver);
			unregisterCursorDataSetObserver(mDataSetObserver);
			try {
				mIterator.close();
			} catch (SQLException e) {

			}
		}
		mIterator = iterator;
		if (iterator != null) {
			registerCursorContentObserver(mChangeObserver);
			registerCursorDataSetObserver(mDataSetObserver);
			// mRowIDColumn = cursor.getColumnIndexOrThrow("_id");
			mDataValid = true;
			// notify the observers about the new cursor
			notifyDataSetChanged();
		} else {
			// mRowIDColumn = -1;
			mDataValid = false;
			// notify the observers about the lack of a data set
			notifyDataSetInvalidated();
		}
	}

	/**
	 * <p>
	 * Converts the cursor into a CharSequence. Subclasses should override this
	 * method to convert their results. The default implementation returns an
	 * empty String for null values or the default String representation of the
	 * value.
	 * </p>
	 * 
	 * @param cursor
	 *            the cursor to convert to a CharSequence
	 * @return a CharSequence representing the value
	 */
	public CharSequence convertToString(CloseableIterator<?> iterator) {
		return iterator == null ? "" : iterator.toString();
	}

	/**
	 * Called when the {@link ContentObserver} on the cursor receives a change
	 * notification. The default implementation provides the auto-requery logic,
	 * but may be overridden by sub classes.
	 * 
	 * @see ContentObserver#onChange(boolean)
	 */
	protected void onContentChanged() {
		if (mAutoRequery && mIterator != null && !isCursorClosed()) {
			Log.v("Cursor", "Auto requerying " + mIterator + " due to update");
			mDataValid = cursorRequery();
		}
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			notifyDataSetInvalidated();
		}
	}

}
