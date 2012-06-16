package com.exfe.android.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.exfe.android.PrefKeys;
import com.exfe.android.model.entity.Cross;
import com.j256.ormlite.dao.Dao;

public class CrossesModel {

	public static final int ACTION_TYPE_UPDATE_CROSSES = Model.ACTION_TYPE_CROSSES_BASE + 1;

	private Model mRoot = null;

	public CrossesModel(Model m) {
		mRoot = m;
	}

	private Dao<Cross, Long> getDao() {
		return mRoot.getHelper().getCrossDao();
	}

	private Date mLastUpdateQuery = null;

	public void addCrosses(List<Cross> xs) {

		if (xs != null && !xs.isEmpty()) {
			List<Long> update = new ArrayList<Long>();
			for (Cross x : xs) {
				if (x != null /* && x.getByIdentitiy() != null */) {
					x.saveToDao(mRoot.getHelper());
					update.add(x.getId());
				}
			}
			if (update.size() > 0) {
				mRoot.setChanged();
				Bundle data = new Bundle();
				data.putInt(Model.OBSERVER_FIELD_TYPE,
						ACTION_TYPE_UPDATE_CROSSES);
				long[] value = new long[update.size()];
				for (int i = 0; i < value.length; i++) {
					value[i] = update.get(i);
				}

				data.putLongArray("update", value);
				mRoot.notifyObservers(data);
			}
		}
	}

	/**
	 * @return the lastUpdateQuery
	 */
	public Date getLastUpdateQuery() {
		if (mLastUpdateQuery == null) {
			long last = mRoot.getDefaultSharedPreference().getLong(
					PrefKeys.LAST_UPDATE_QUERY_TIME, 0);
			if (last > 0) {
				mLastUpdateQuery = new Date(last);
			}
		}
		return mLastUpdateQuery;
	}

	/**
	 * @param lastUpdateQuery
	 *            the lastUpdateQuery to set
	 */
	public void setLastUpdateQuery(Date lastUpdateQuery) {
		Editor edit = mRoot.getDefaultSharedPreference().edit();
		if (lastUpdateQuery != null) {
			edit.putLong(PrefKeys.LAST_UPDATE_QUERY_TIME,
					lastUpdateQuery.getTime());
		} else {
			edit.remove(PrefKeys.LAST_UPDATE_QUERY_TIME);
		}
		edit.commit();
		this.mLastUpdateQuery = lastUpdateQuery;
	}

	public void clearCrosses() {
		try {
			getDao().delete(getDao().queryForAll());
			mRoot.setChanged();
			Bundle data = new Bundle();
			data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_UPDATE_CROSSES);
			mRoot.notifyObservers(data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<Cross> getCrosses() {
		try {
			return getDao().queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Cross>();
	}

	public Cross getCrossById(long id) {
		try {
			Cross result = getDao().queryForId(id);
			if (result != null) {
				result.loadFromDao(mRoot.getHelper());
				return result;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void updateLastView(Cross cross) {
		if (cross != null) {
			try {
				cross.setLastViewAt(new Date());
				this.getDao().update(cross);
				mRoot.setChanged();
				Bundle data = new Bundle();
				data.putInt(Model.OBSERVER_FIELD_TYPE,
						ACTION_TYPE_UPDATE_CROSSES);
				mRoot.notifyObservers(data);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
