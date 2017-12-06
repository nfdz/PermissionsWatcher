package io.github.nfdz.permissionswatcher.sched;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class RealTimeService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        ReportService.startRealTimeMode(this);
        SchedUtils.rescheduleRealmTime(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}