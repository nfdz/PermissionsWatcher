package io.github.nfdz.permissionswatcher.sched;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
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

    private static final int REAL_TIME_JOB_ID = 6238;
    private static final long REAL_TIME_FEQ_MILLIS = TimeUnit.MINUTES.toMillis(30);

    private static final long REPORT_WINDOW_LENGTH_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final long MARGIN_TO_SCHEDULE = TimeUnit.MINUTES.toMillis(5);

    private static boolean sInitialized = false;

    synchronized public static void initialize(@NonNull final Context context) {
        if (sInitialized) return;
        sInitialized = true;
        rescheduleReport(context);
        rescheduleRealmTime(context);
    }

    public static void rescheduleReport(@NonNull Context context) {
        boolean alarmEnabled = PreferencesUtils.isReportEnable(context);
        if (!alarmEnabled) {
            unscheduleReport(context);
            return;
        }
        unscheduleReport(context);

        long triggerAtMillis;
        String timeValue = PreferencesUtils.notificationsTime(context);
        int hour = TimePreference.getHourFromValue(timeValue);
        int minutes = TimePreference.getMinutesFromValue(timeValue);
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        long todayTrigger = cal.getTimeInMillis();
        if (todayTrigger > (now + MARGIN_TO_SCHEDULE)) {
            triggerAtMillis = todayTrigger;
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            triggerAtMillis = cal.getTimeInMillis();
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Timber.d("Scheduling report alarm at %s", new Date(triggerAtMillis));
            manager.setWindow(AlarmManager.RTC, triggerAtMillis, REPORT_WINDOW_LENGTH_MILLIS, getReportOperation(context));
            Timber.d("Report alarm scheduled successfully.");
        } else {
            Timber.e("Cannot schedule report alarm because AlarmManager is not available.");
        }
    }

    private static PendingIntent getReportOperation(@NonNull Context context) {
        return PendingIntent.getService(context, 0, getReportIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent getReportIntent(@NonNull Context context) {
        return new Intent(context, ReportService.class);
    }

    public static void unscheduleReport(@NonNull Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(getReportOperation(context));
        } else {
            Timber.e("Cannot cancel report alarm because AlarmManager is not available.");
        }
    }

    public static void rescheduleRealmTime(@NonNull Context context) {
        boolean realmTimeEnabled = PreferencesUtils.isRealTimeEnable(context);
        if (!realmTimeEnabled) {
            unscheduleRealTime(context);
            return;
        }
        unscheduleRealTime(context);

        ComponentName component = new ComponentName(context.getPackageName(), RealTimeService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(REAL_TIME_JOB_ID, component);
        builder.setMinimumLatency(REAL_TIME_FEQ_MILLIS); // wait at least
        builder.setOverrideDeadline(REAL_TIME_FEQ_MILLIS); // maximum delay
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot schedule real time job because JobScheduler is not available.");
        } else if (scheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Real time job scheduled successfully.");
        } else {
            Timber.e("Cannot schedule real time job.");
        }
    }

    public static void unscheduleRealTime(@NonNull Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot unschedule real time job.");
        } else {
            scheduler.cancel(REAL_TIME_JOB_ID);
        }
    }

}
