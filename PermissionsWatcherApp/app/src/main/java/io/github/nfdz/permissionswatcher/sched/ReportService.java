package io.github.nfdz.permissionswatcher.sched;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import io.github.nfdz.permissionswatcher.sync.SyncUtils;
import timber.log.Timber;

public class ReportService extends IntentService {

    public static final String SERVICE_NAME = "ReportService";

    public ReportService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Timber.d("Report service started");

        SyncUtils.tryToSync(getPackageManager());

    }
}
