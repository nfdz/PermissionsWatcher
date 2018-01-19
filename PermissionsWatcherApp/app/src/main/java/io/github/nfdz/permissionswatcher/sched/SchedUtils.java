package io.github.nfdz.permissionswatcher.sched;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.settings.TimePreference;
import timber.log.Timber;

public class SchedUtils {

    private static final int REAL_TIME_JOB_ID = 2638;
    private static final long REAL_TIME_FEQ_MILLIS = TimeUnit.MINUTES.toMillis(15);

    private static final int REPORT_JOB_ID = 2567;
    private static final long SCHEDULE_MARGIN = TimeUnit.MINUTES.toMillis(2);

    private static boolean sInitialized = false;

    synchronized public static void initialize(@NonNull final Context context) {
        if (sInitialized) return;
        sInitialized = true;
        if (!isJobScheduled(context, REPORT_JOB_ID)) scheduleReport(context);
        if (!isJobScheduled(context, REAL_TIME_JOB_ID)) scheduleRealmTime(context);
    }

    public static void scheduleReport(@NonNull Context context) {
        boolean alarmEnabled = PreferencesUtils.isReportEnable(context);
        if (!alarmEnabled) return;

        long triggerAtMillis;
        String timeValue = PreferencesUtils.notificationsTime(context);
        int hour = TimePreference.getHourFromValue(timeValue);
        int minutes = TimePreference.getMinutesFromValue(timeValue);
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        long todayTrigger = cal.getTimeInMillis();
        if (todayTrigger > (now + SCHEDULE_MARGIN)) {
            triggerAtMillis = todayTrigger;
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            triggerAtMillis = cal.getTimeInMillis();
        }

        long timeToTrigger = triggerAtMillis - now;

        ComponentName component = new ComponentName(context.getPackageName(), ReportJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(REPORT_JOB_ID, component);
        builder.setMinimumLatency(timeToTrigger); // wait at least
        builder.setOverrideDeadline(timeToTrigger); // maximum delay
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot schedule report job because JobScheduler is not available.");
            FirebaseCrash.report(new Exception("Cannot schedule report job because JobScheduler is not available."));
        } else if (scheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Report job scheduled successfully. Trigger at: %s.", new Date(triggerAtMillis));
        } else {
            Timber.e("Cannot schedule report job.");
            FirebaseCrash.report(new Exception("Cannot schedule report job."));
        }
    }

    public static void unscheduleReport(@NonNull Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot unschedule report job because JobScheduler is not available.");
            FirebaseCrash.report(new Exception("Cannot unschedule report job because JobScheduler is not available."));
        } else {
            scheduler.cancel(REPORT_JOB_ID);
            Timber.d("Report job unscheduled successfully.");
        }
    }

    public static void scheduleRealmTime(@NonNull Context context) {
        boolean realmTimeEnabled = PreferencesUtils.isRealTimeEnable(context);
        if (!realmTimeEnabled) return;

        ComponentName component = new ComponentName(context.getPackageName(), RealTimeJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(REAL_TIME_JOB_ID, component);
        builder.setMinimumLatency(REAL_TIME_FEQ_MILLIS); // wait at least
        builder.setOverrideDeadline((long) (REAL_TIME_FEQ_MILLIS * 1.05)); // maximum delay
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot schedule real time job because JobScheduler is not available.");
            FirebaseCrash.report(new Exception("Cannot schedule real time job because JobScheduler is not available."));
        } else if (scheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Real time job scheduled successfully.");
        } else {
            Timber.e("Cannot schedule real time job.");
            FirebaseCrash.report(new Exception("Cannot schedule real time job."));
        }
    }

    public static void unscheduleRealTime(@NonNull Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Timber.e("Cannot unschedule real time job.");
            FirebaseCrash.report(new Exception("Cannot unschedule real time job because JobScheduler is not available."));
        } else {
            scheduler.cancel(REAL_TIME_JOB_ID);
            Timber.d("Real time job unscheduled successfully.");
        }
    }

    public static boolean isJobScheduled(@NonNull Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            for (JobInfo job : scheduler.getAllPendingJobs()) {
                if (job.getId() == jobId) return true;
            }
        }
        return false;
    }

}
