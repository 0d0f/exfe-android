package com.exfe.android.model;

import com.example.android.bitmapfun.util.ImageCache;

public class CacheModel {

	private Model mRoot = null;

	private ImageCache mImageCache = null;


	public CacheModel(Model m) {
		mRoot = m;
	}

	public ImageCache ImageCache() {
		if (mImageCache == null) {
			synchronized (CacheModel.class) {
				if (mImageCache == null) {
					mImageCache = new ImageCache(mRoot.getAppContext(),
							new ImageCache.ImageCacheParams("images"));
				}
			}
		}
		return mImageCache;
	}
}
