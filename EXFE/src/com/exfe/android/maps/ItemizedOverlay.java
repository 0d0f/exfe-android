package com.exfe.android.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ItemizedOverlay<T extends ItemizedOverlay.IOverlayItem> extends
		com.google.android.maps.ItemizedOverlay<OverlayItem> {

	public interface IOverlayItem {
		OverlayItem getOverlayItem();
	}

	protected List<T> mOverlays = new ArrayList<T>();
	protected Context mContext = null;

	public ItemizedOverlay(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i).getOverlayItem();
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addItem(T item) {
		mOverlays.add(item);
		populate();
	}

	public T getListItem(int location) {
		if (mOverlays.size() > 0) {
			return mOverlays.get(location);
		}
		return null;
	}

	public void removeItem(T item) {
		int index = mOverlays.indexOf(item);
		if (index >= 0 && getLastFocusedIndex() == index) {
			setLastFocusedIndex(-1);
		}
		mOverlays.remove(item);
		populate();
	}

	public void addAll(Collection<? extends T> list) {
		mOverlays.addAll(list);
		populate();
	}

	public void clear() {
		if (mOverlays.size() > 0) {
			mOverlays.clear();
			setLastFocusedIndex(-1);
			populate();
		}
	}

}
