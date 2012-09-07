package com.exfe.android.view;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

//http://stackoverflow.com/questions/2332847/how-to-create-a-closed-circular-listview
public class CircularArrayAdapter<T> extends ArrayAdapter<T>{

	public static final int HALF_MAX_VALUE = Integer.MAX_VALUE/2;
    public final int MIDDLE;
    private T[] objects;
	
	public CircularArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		this.objects = (T[]) objects.toArray();
		MIDDLE = HALF_MAX_VALUE - HALF_MAX_VALUE % objects.size();
	}

	public CircularArrayAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
		this.objects = objects;
        MIDDLE = HALF_MAX_VALUE - HALF_MAX_VALUE % objects.length;
	}

	/*
	public CircularArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	public CircularArrayAdapter(Context context, int textViewResourceId,
			List objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public CircularArrayAdapter(Context context, int textViewResourceId,
			Object[] objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public CircularArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		// TODO Auto-generated constructor stub
	}
	 */
	
	@Override
    public int getCount()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public T getItem(int position) 
    {
        return objects[position % objects.length];
    }
	
}
