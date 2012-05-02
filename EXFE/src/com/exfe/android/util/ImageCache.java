package com.exfe.android.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.exfe.android.exception.ExfeException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;

public class ImageCache {

	private static ImageCache sInst = null;
	private static HashMap<String, Bitmap> sImgs = null;
	private static Context sContext = null;

	public synchronized static ImageCache getInst(Context appContext) {

		if (sInst == null) {
			sInst = new ImageCache();
			sImgs = new HashMap<String, Bitmap>();
			sContext = appContext;
		}

		return sInst;
	}

	public synchronized static ImageCache getInst() {

		if (sInst == null) {
			throw new ExfeException(
					"Call ImageCache.getInst(Context appContext) first.");
		}

		return sInst;
	}

	public static String getCachePath() {
		String imageCachePath = String.format("%s/%s", sContext.getCacheDir(),
				"images");
		File cache = new File(imageCachePath);
		if (!cache.exists()) {
			cache.mkdirs();
		}
		return imageCachePath;
	}

	public static String getImageName(String url) {
		String key = "";
		try {
			key = MD5.getMD5(url);
			String cacheFilename = String.format("%s/%s", getCachePath(), key);
			File cacheFile = new File(cacheFilename);
			if (!cacheFile.exists()) {
				new DownloadFile().execute(url, cacheFile.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return key;
	}

	public static String getImageUrl(String fileName) {
		if (TextUtils.isEmpty(fileName) || fileName.length() < 2) {
			fileName = "default.png";
		}
		if (fileName.startsWith("http://")) {
			return fileName;
		}
		if ("default.png".equalsIgnoreCase(fileName)) {
			return String.format("http://img.exfe.com/web/80_80_%s", fileName);
		}

		return String.format("http://img.exfe.com/%c/%c/80_80_%s",
				fileName.charAt(0), fileName.charAt(1), fileName);
	}

	public Drawable getImageFrom(String url) {
		String key = "";
		try {
			key = MD5.getMD5(url);
			if (sImgs.containsKey(key)) {
				Bitmap bm = sImgs.get(key);
				if (bm != null) {
					return new BitmapDrawable(bm);
				}
			}

			String cacheFilename = String.format("%s/%s", getCachePath(), key);
			File cacheFile = new File(cacheFilename);
			if (!cacheFile.exists()) {
				new DownloadFile().execute(url, cacheFile.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static class DownloadFile extends
			AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			URLConnection connection = null;
			InputStream input = null;
			OutputStream output = null;
			try {
				URL url = new URL(params[0]);
				File local = new File(params[1]);
				if (local.exists()) {
					return Boolean.FALSE;
				}

				connection = url.openConnection();
				connection.connect();
				int lenghtOfFile = connection.getContentLength();

				input = new BufferedInputStream(url.openStream());

				output = new FileOutputStream(local);

				byte data[] = new byte[1024];

				long total = 0;
				int count = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / lenghtOfFile));
					output.write(data, 0, count);
				}
				output.flush();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			return Boolean.TRUE;
		}

	}
}
