package com.septi.resq.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppUtils {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_FIRST_TIME = "isFirstTime";

    public static boolean isFirstTimeLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_TIME, true);
    }

    public static void setFirstTimeLaunch(Context context, boolean isFirstTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FIRST_TIME, isFirstTime);
        editor.apply();
    }
}
