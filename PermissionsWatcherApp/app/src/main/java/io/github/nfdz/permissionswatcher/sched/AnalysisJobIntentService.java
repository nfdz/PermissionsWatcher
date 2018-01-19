package io.github.nfdz.permissionswatcher.sched;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import timber.log.Timber;

public class AnalysisJobIntentService extends JobIntentService {

    private static final int JOB_ID = 9827;

    public static void start(Context context) {
        Intent starter = new Intent(context, AnalysisJobIntentService.class);
        enqueueWork(context, starter);
    }

    private static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AnalysisJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.d("Analysis service started");
        try {
            List<ApplicationInfo> apps = AnalysisUtils.tryPerformAnalysis(this);
            NotificationUtils.notifyReport(this, apps, true);
            Timber.d("Analysis service finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during analysis.");
            FirebaseCrash.report(e);
        }
    }
}
