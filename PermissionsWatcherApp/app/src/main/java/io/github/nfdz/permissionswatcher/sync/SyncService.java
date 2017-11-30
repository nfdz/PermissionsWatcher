package io.github.nfdz.permissionswatcher.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.realm.Realm;
import timber.log.Timber;

public class SyncService extends IntentService {

    public static final String SERVICE_NAME = "SyncService";

    private static final String FIRST_TIME_ACTION = "FIRST_TIME";

    public static void start(Context context) {
        context.startService(new Intent(context, SyncService.class));
    }

    public static void startFirstTimeMode(Context context) {
        Intent starter = new Intent(context, SyncService.class);
        starter.setAction(FIRST_TIME_ACTION);
        context.startService(starter);
    }

    public SyncService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        boolean firstTimeMode = isFirstTimeMode(intent);

        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, getPackageManager(), firstTimeMode);
            Timber.d("Synchronization finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during synchronization.");
        } finally {
            if (realm != null) realm.close();
        }
    }

    private boolean isFirstTimeMode(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && FIRST_TIME_ACTION.equals(action)) {
                return true;
            }
        }
        return false;
    }

}
