package com.exfe.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.TableLayout;

public class LinearGridView extends TableLayout {

	private ListAdapter mAdapter;
	private int mColumnNumber = 6;
	
	public LinearGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public LinearGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the adapter
	 */
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	/**
	 * @param adapter the adapter to set
	 */
	public void setAdapter(ListAdapter adapter) {
		if (this.mAdapter != null){
			// clean up work:
			// unreg data observer
		}
		this.mAdapter = adapter;
		if (this.mAdapter != null){
			// do init work
			int count = adapter.getCount();
			
		}
	}
	


}
