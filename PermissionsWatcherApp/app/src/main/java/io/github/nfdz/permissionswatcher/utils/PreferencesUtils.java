package io.github.nfdz.permissionswatcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    private final static String FIRST_TIME_FLAG_KEY = "isFirstTime";
    private final static boolean FIRST_TIME_FLAG_DEFAULT = true;

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(FIRST_TIME_FLAG_KEY, FIRST_TIME_FLAG_DEFAULT);
    }

    public static void setFirstTime(Context context, boolean firstTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(FIRST_TIME_FLAG_KEY, firstTime).apply();
    }

}
