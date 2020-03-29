package com.kodeholic.itbook.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.kodeholic.itbook.lib.util.Log;


public class MySettings {
	public static final String  LOG_TAG = "MySettings";

	////////////////////////////////////////////////
	private final static String NAME = "my_settings";
	private Context mContext;

	protected SharedPreferences mSharedPrefs;

	private volatile static MySettings sInstance;

	private MySettings(Context context) {
		this.mContext     = context;
		this.mSharedPrefs = context.getSharedPreferences(NAME, Context.MODE_MULTI_PROCESS);
	}

	public static MySettings getInstance(Context context) {
		if (sInstance == null) {
			synchronized (MySettings.class) {
				if (sInstance == null) {
					sInstance = new MySettings(context);
				}
			}
		}

		return sInstance;
	}

	protected long save(String key, long value) {
		Log.d(LOG_TAG, "save - " + key + "=" + value);
		SharedPreferences.Editor edit = mSharedPrefs.edit();
		edit.putLong(key, value);
		edit.commit();
		
		return value;
	}
	protected int save(String key, int value) {
		Log.d(LOG_TAG, "save - " + key + "=" + value);
		SharedPreferences.Editor edit = mSharedPrefs.edit();
		edit.putInt(key, value);
		edit.commit();
		
		return value;
	}
	protected String save(String key, String value) {
		Log.d(LOG_TAG, "save - " + key + "=" + value);
		SharedPreferences.Editor edit = mSharedPrefs.edit();
		edit.putString(key, value);
		edit.commit();
		
		return value;
	}
	protected boolean save(String key, boolean value) {
		Log.d(LOG_TAG, "save - " + key + "=" + value);
		SharedPreferences.Editor edit = mSharedPrefs.edit();
		edit.putBoolean(key, value);
		edit.commit();
		
		return value;
	}
	
	public void clear() {
		Log.e(LOG_TAG, "MySettings are Cleared!!!");
		//
		SharedPreferences.Editor edit = mSharedPrefs.edit();
		edit.clear();
		edit.commit();
		
		return;
	}
	//
	public int getSortOption()               { return mSharedPrefs.getInt("sortOption", MyIntent.SORT_OPTION.RECENTS); }
	public int setSortOption(int sortOption) { return                save("sortOption", sortOption); }

	public int getOrderOption()                { return mSharedPrefs.getInt("orderOption", MyIntent.ORDER_OPTION.DESC); }
	public int setOrderOption(int orderOption) { return                save("orderOption", orderOption); }
}
