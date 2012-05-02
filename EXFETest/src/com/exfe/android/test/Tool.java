package com.exfe.android.test;

import java.io.InputStream;

import org.apache.http.util.EncodingUtils;

import android.test.InstrumentationTestCase;

public class Tool {

	
	// 从resources中的raw 文件夹中获取文件并读取数据
	public static String getFromRaw(InstrumentationTestCase inst, int rawId) {
		String result = null;
		try {
			InputStream in = inst.getInstrumentation().getContext().getResources().openRawResource(rawId);
			// 获取文件的字节数
			int lenght = in.available();
			// 创建byte数组
			byte[] buffer = new byte[lenght];
			// 将文件中的数据读到byte数组中
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// 从assets 文件夹中获取文件并读取数据
	public static String getFromAssets(InstrumentationTestCase inst, String fileName) {
		
		String result = null;
		try {
			InputStream in = inst.getInstrumentation().getContext().getAssets().open(fileName);
			int lenght = in.available();
			byte[] buffer = new byte[lenght];
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
