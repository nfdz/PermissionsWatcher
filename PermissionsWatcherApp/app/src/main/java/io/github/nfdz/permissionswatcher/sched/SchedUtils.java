package io.github.nfdz.permissionswatcher.sched;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.settings.TimePreference;
import timber.log.Timber;

public class SchedUtils {

    private static final long DAILY_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(1);

    private static boolean sInitialized = false;

    synchronized public static void initialize(@NonNull final Context context) {
        if (sInitialized) return;
        sInitialized = true;
        rescheduleAlarm(context);
    }

    public static void rescheduleAlarm(@NonNull Context context) {
        boolean alarmEnabled = PreferencesUtils.notificationsEnable(context);
        if (!alarmEnabled) {
            disableAlarm(context);
            return;
        }
        disableAlarm(context);

        long triggerAtMillis;
        String timeValue = PreferencesUtils.notificationsTime(context);
        int hour = TimePreference.getHourFromValue(timeValue);
        int minutes = TimePreference.getMinutesFromValue(timeValue);
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        long todayTrigger = cal.getTimeInMillis();
        if (todayTrigger > now) {
            triggerAtMillis = todayTrigger;
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            triggerAtMillis = cal.getTimeInMillis();
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Timber.d("Scheduling report alarm at %s", new Date(triggerAtMillis));
            manager.setInexactRepeating(AlarmManager.RTC, triggerAtMillis, DAILY_INTERVAL_MILLIS, getReportOperation(context));
        } else {
            Timber.d("Cannot schedule report alarm because AlarmManager is not available.");
        }
    }

    private static PendingIntent getReportOperation(@NonNull Context context) {
        return PendingIntent.getService(context, 0, getReportIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent getReportIntent(@NonNull Context context) {
        return new Intent(context, ReportService.class);
    }

    public static void disableAlarm(@NonNull Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(getReportOperation(context));
        } else {
            Timber.d("Cannot cancel report alarm because AlarmManager is not available.");
        }
    }
}
