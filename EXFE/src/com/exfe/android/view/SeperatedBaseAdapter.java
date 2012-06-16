package com.exfe.android.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SeperatedBaseAdapter extends BaseAdapter {

	private Context mCtx = null;
	private DataSetObserver mObserver = new SeperatedDataSetObserver();
	private List<BaseAdapter> mAdapters = new ArrayList<BaseAdapter>();
	private List<AdapterMeta> mMetas = new ArrayList<AdapterMeta>();
	private SeperatedSectionFactory mFactory = null;

	public SeperatedBaseAdapter(Context ctx, SeperatedSectionFactory factory) {
		// TODO Auto-generated constructor stub
		mCtx = ctx;
		mFactory = factory;
	}

	public Context getContext() {
		return mCtx;
	}

	public void addAdapter(BaseAdapter adapter) {
		mAdapters.add(adapter);
		adapter.registerDataSetObserver(mObserver);
		mObserver.onChanged();
	}

	public void removeAdapter(BaseAdapter adapter) {
		adapter.unregisterDataSetObserver(mObserver);
		mAdapters.remove(adapter);
		mObserver.onChanged();
	}

	public void setHeadFootViewFactory(SeperatedSectionFactory factory) {
		mFactory = factory;
		mObserver.onChanged();
	}

	public SeperatedPos translate(int position) {
		int lastAll = 0;
		for (AdapterMeta meta : mMetas) {
			if (position < meta.all) {
				if (meta.hasHead && position == lastAll) {
					return new SeperatedPos(meta.index, -1);
				} else if (meta.hasFoot && position == meta.all - 1) {
					return new SeperatedPos(meta.index, meta.itemcount);
				} else {
					return new SeperatedPos(meta.index, position - lastAll
							- (meta.hasHead ? 1 : 0));
				}
			}
			lastAll = meta.all;
		}
		return null;
	}

	@Override
	public int getCount() {
		if (mMetas != null && mMetas.size() > 0) {
			return mMetas.get(mMetas.size() - 1).all;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		SeperatedPos pos = translate(position);
		BaseAdapter adapter = mAdapters.get(pos.index);
		if (pos.pos >= 0 && pos.pos < adapter.getCount()) {
			return adapter.getItem(pos.pos);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		SeperatedPos pos = translate(position);
		BaseAdapter adapter = mAdapters.get(pos.index);
		if (pos.pos >= 0 && pos.pos < adapter.getCount()) {
			return adapter.getItemId(pos.pos);
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SeperatedPos pos = translate(position);
		BaseAdapter adapter = mAdapters.get(pos.index);
		if (pos.pos >= 0 && pos.pos < adapter.getCount()) {
			return adapter.getView(pos.pos, convertView, parent);
		}
		if (pos.pos == -1) {
			return mFactory.getHeader(this, adapter, pos.index);
		}
		if (pos.pos == adapter.getCount()) {
			return mFactory.getFoot(this, adapter, pos.index);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		boolean enabled = super.areAllItemsEnabled();
		for (BaseAdapter adapter : mAdapters) {
			enabled = enabled && adapter.areAllItemsEnabled();
		}
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getDropDownView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.getDropDownView(position, convertView, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		SeperatedPos pos = translate(position);
		BaseAdapter adapter = mAdapters.get(pos.index);
		int baseType = 0;
		if (pos.index > 0) {
			baseType = mMetas.get(pos.index - 1).viewTypeCountInAll;
		}
		if (pos.pos >= 0 && pos.pos < adapter.getCount()) {
			int type = adapter.getItemViewType(pos.pos);
			return baseType + type;
		} else if (pos.pos == -1) {
			return baseType + adapter.getViewTypeCount();
		} else if (pos.pos == adapter.getCount()) {
			return baseType + adapter.getViewTypeCount()
					+ (mMetas.get(pos.index).hasHead ? 1 : 0);
		}
		return super.getItemViewType(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		if (mMetas != null && mMetas.size() > 0) {
			return mMetas.get(mMetas.size() - 1).viewTypeCountInAll;
		}
		return super.getViewTypeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		/*
		 * boolean stableIds = super.hasStableIds(); for (BaseAdapter adapter :
		 * mAdapters) { stableIds = stableIds && adapter.hasStableIds(); }
		 */
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		boolean empty = true;
		for (BaseAdapter adapter : mAdapters) {
			empty = empty && adapter.isEmpty();
		}
		return empty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		SeperatedPos pos = translate(position);
		BaseAdapter adapter = mAdapters.get(pos.index);
		if (pos.pos >= 0 && pos.pos < adapter.getCount()) {
			return adapter.isEnabled(pos.pos);
		}
		if (pos.pos == -1) {
			return mFactory.hasHeader(this, adapter, pos.index);
		}
		if (pos.pos == adapter.getCount()) {
			return mFactory.hasFooter(this, adapter, pos.index);
		}
		return super.isEnabled(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#notifyDataSetChanged()
	 */
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#notifyDataSetInvalidated()
	 */
	@Override
	public void notifyDataSetInvalidated() {
		// TODO Auto-generated method stub
		super.notifyDataSetInvalidated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#registerDataSetObserver(android.database.
	 * DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		super.registerDataSetObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.BaseAdapter#unregisterDataSetObserver(android.database
	 * .DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		super.unregisterDataSetObserver(observer);
	}

	class SeperatedPos {
		SeperatedPos(int i, int p) {
			index = i;
			pos = p;
		}

		int index;
		int pos;
	}

	class AdapterMeta {

		AdapterMeta(int i, int h, int f, int c, int ca, boolean hh, boolean hf,
				int vtc) {
			index = i;
			headinall = h;
			footinall = f;
			itemcount = c;
			countinall = ca;
			hasHead = hh;
			hasFoot = hf;
			all = h + f + ca;
			viewTypeCountInAll = vtc;
		}

		int index;
		int headinall;
		boolean hasHead;
		int footinall;
		boolean hasFoot;
		int itemcount;
		int countinall;
		int all;
		int viewTypeCountInAll;
	}

	class SeperatedDataSetObserver extends DataSetObserver {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onChanged()
		 */
		@Override
		public void onChanged() {
			// TODO Auto-generated method stub
			super.onChanged();
			mMetas.clear();
			int index = 0;
			int head = 0;
			int foot = 0;
			int countinall = 0;
			int viewTypeCount = 0;
			for (BaseAdapter adapter : mAdapters) {
				boolean hasHead = false;
				boolean hasFoot = false;
				if (mFactory != null
						&& mFactory.hasHeader(SeperatedBaseAdapter.this,
								adapter, index)) {
					head++;
					hasHead = true;
				}
				if (mFactory != null
						&& mFactory.hasFooter(SeperatedBaseAdapter.this,
								adapter, index)) {
					foot++;
					hasFoot = true;
				}
				countinall += adapter.getCount();
				viewTypeCount += adapter.getViewTypeCount() + (hasHead ? 1 : 0)
						+ (hasFoot ? 1 : 0);
				mMetas.add(new AdapterMeta(index, head, foot, adapter
						.getCount(), countinall, hasHead, hasFoot, viewTypeCount));
				index++;
			}
			notifyDataSetChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.DataSetObserver#onInvalidated()
		 */
		@Override
		public void onInvalidated() {
			// TODO Auto-generated method stub
			super.onInvalidated();
			notifyDataSetInvalidated();
		}

	}

	public interface SeperatedSectionFactory {
		boolean hasHeader(SeperatedBaseAdapter container, BaseAdapter adapter,
				int index);

		boolean hasFooter(SeperatedBaseAdapter container, BaseAdapter adapter,
				int index);

		View getHeader(SeperatedBaseAdapter container, BaseAdapter adapter,
				int index);

		View getFoot(SeperatedBaseAdapter container, BaseAdapter adapter,
				int index);
	}

}
