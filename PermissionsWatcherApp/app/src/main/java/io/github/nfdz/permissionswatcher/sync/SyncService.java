package io.github.nfdz.permissionswatcher.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.realm.Realm;
import timber.log.Timber;

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
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, getPackageManager());
            Timber.d("Synchronization finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during synchronization.");
        } finally {
            if (realm != null) realm.close();
        }
    }
}
