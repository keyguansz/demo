package com.yck.ktc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SpUtil {

	private static final String TABLE_NAME = "SP";
	public static final String PHONE = "phone";
	public static final String PASSWORD = "password";

	public static boolean save(Context c, String key, String value) {
		//单例获取，不必担心性能问题
		Editor editor = c.getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE).edit();
		return editor.putString(key, value).commit();
	}

	//默认值为""
	public static String get(Context c, String key) {
		return c.getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE).getString(key, "");
	}
}
