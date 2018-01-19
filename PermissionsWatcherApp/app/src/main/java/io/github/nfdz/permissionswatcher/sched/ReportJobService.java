package io.github.nfdz.permissionswatcher.sched;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import timber.log.Timber;

public class ReportJobService extends JobService implements AnalysisUtils.AnalysisCallback {

    private AnalysisUtils.AnalysisTask analysisTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Timber.d("Report service started");
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
        SchedUtils.scheduleReport(this);
        return false;
    }

    @Override
    public void onAnalysisSuccess(List<ApplicationInfo> appsWithChanges) {
        Timber.d("Report service finished successfully.");
        NotificationUtils.notifyReport(this, appsWithChanges);
        finishJob();
    }

    @Override
    public void onAnalysisError(Throwable e) {
        Timber.e(e, "There was an error during report service.");
        FirebaseCrash.report(e);
        finishJob();
    }

    private void finishJob() {
        analysisTask = null;
        SchedUtils.unscheduleReport(this);
    }
}
