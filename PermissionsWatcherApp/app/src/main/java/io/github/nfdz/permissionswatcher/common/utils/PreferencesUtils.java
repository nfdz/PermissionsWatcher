package io.github.nfdz.permissionswatcher.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.nfdz.permissionswatcher.R;

public class PreferencesUtils {

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region First time flags

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

    public final static String SHOW_TUTORIAL_FLAG_KEY = "showTutorial";
    public final static boolean SHOW_TUTORIAL_FLAG_DEFAULT = true;

    public static boolean showTutorial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SHOW_TUTORIAL_FLAG_KEY, SHOW_TUTORIAL_FLAG_DEFAULT);
    }

    public static void setShowTutorial(Context context, boolean showTutorial) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(SHOW_TUTORIAL_FLAG_KEY, showTutorial).apply();
    }

    //endregion

    public static boolean sortByIgnoreFlag(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_sort_ignored_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_sort_ignored_default)));
    }

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

    public static boolean showIgnorePermissions(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_show_ignore_permission_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_show_ignore_permission_default)));
    }

    public static boolean isReportEnable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_report_enable_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_report_enable_default)));
    }

    public static boolean isRealTimeEnable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.prefs_real_time_enable_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_real_time_enable_default)));
    }

    public static String notificationsTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.prefs_report_time_key),
                context.getString(R.string.prefs_report_time_default));
    }
}
