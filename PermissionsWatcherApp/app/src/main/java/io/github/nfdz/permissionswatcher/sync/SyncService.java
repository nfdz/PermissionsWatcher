package io.github.nfdz.permissionswatcher.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class SyncService extends IntentService {

    public static final String SERVICE_NAME = "SyncService";

    public static void start(Context context) {
        context.startService(new Intent(context, SyncService.class));
    }

    public SyncService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncUtils.tryToSync(getPackageManager());
    }
}
