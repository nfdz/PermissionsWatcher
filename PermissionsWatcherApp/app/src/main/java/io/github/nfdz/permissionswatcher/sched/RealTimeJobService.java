package io.github.nfdz.permissionswatcher.sched;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import timber.log.Timber;

public class RealTimeJobService extends JobService implements AnalysisUtils.AnalysisCallback {

    private AnalysisUtils.AnalysisTask analysisTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Timber.d("Real time service started");
        analysisTask = AnalysisUtils.createAnalysisTask(this, this);
        analysisTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (analysisTask != null) {
            analysisTask.cancel(true);
            analysisTask = null;
        }
        SchedUtils.scheduleRealmTime(this);
        return false;
    }

    @Override
    public void onAnalysisSuccess(List<ApplicationInfo> appsWithChanges) {
        Timber.d("Real time service finished successfully.");
        NotificationUtils.notifyReport(this, appsWithChanges);
        finishJob();
    }

    @Override
    public void onAnalysisError(Throwable e) {
        Timber.e(e, "There was an error during real time service.");
        FirebaseCrash.report(e);
        finishJob();
    }

    private void finishJob() {
        analysisTask = null;
        SchedUtils.unscheduleRealTime(this);
    }
}