package io.github.nfdz.permissionswatcher.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region First time flag
    public final static String FIRST_TIME_FLAG_KEY = "isFirstTime";
    public final static boolean FIRST_TIME_FLAG_DEFAULT = true;

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(FIRST_TIME_FLAG_KEY, FIRST_TIME_FLAG_DEFAULT);
    }

    public static void setFirstTime(Context context, boolean firstTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(FIRST_TIME_FLAG_KEY, firstTime).apply();
    }
    //endregion

    //region Show system applications flag
    public final static String SHOW_SYSTEM_APPS_FLAG_KEY = "showSystemAppsFlag";
    public final static boolean SHOW_SYSTEM_APPS_FLAG_DEFAULT = false;

    public static boolean showSystemApps(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SHOW_SYSTEM_APPS_FLAG_KEY, SHOW_SYSTEM_APPS_FLAG_DEFAULT);
    }

    public static void setShowSystemApps(Context context, boolean showSystemApps) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(SHOW_SYSTEM_APPS_FLAG_KEY, showSystemApps).apply();
    }
    //endregion

}
