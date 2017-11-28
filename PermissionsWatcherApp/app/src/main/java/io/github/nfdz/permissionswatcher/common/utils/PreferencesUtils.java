package io.github.nfdz.permissionswatcher.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.nfdz.permissionswatcher.R;

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

    public static boolean showSystemApps(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_show_system_apps_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_show_system_apps_default)));
    }

    public static boolean showAppsWithoutPermissions(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_show_apps_without_perm_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_show_apps_without_perm_default)));
    }

    public static boolean notificationsEnable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_notifications_enable_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_notifications_enable_default)));
    }

    public static String notificationsTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.prefs_notifications_time_key),
                context.getString(R.string.prefs_notifications_time_default));
    }
}
