package io.github.nfdz.permissionswatcher.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;

import com.google.firebase.crash.FirebaseCrash;

import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.realm.Realm;
import timber.log.Timber;

public class SyncJobIntentService extends JobIntentService {

    private static final int JOB_ID = 2378;

    private static final String FIRST_TIME_ACTION = "FIRST_TIME";

    public static void start(Context context) {
        enqueueWork(context, new Intent(context, SyncJobIntentService.class));
    }

    public static void startFirstTimeMode(Context context) {
        Intent starter = new Intent(context, SyncJobIntentService.class);
        starter.setAction(FIRST_TIME_ACTION);
        enqueueWork(context, starter);
    }

    private static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SyncJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        boolean firstTimeMode = isFirstTimeMode(intent);
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, getPackageManager(), firstTimeMode);
            Timber.d("Synchronization finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during synchronization.");
            FirebaseCrash.report(e);
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
