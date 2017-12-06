package io.github.nfdz.permissionswatcher.sched;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.Analytics;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class TasksService extends IntentService {

    private static final String CLEAR_CHANGES_ACTION = "CLEAR_CHANGES";
    private static final String IGNORE_APP_ACTION = "IGNORE_APP";
    private static final String IGNORE_APP_PKG_EXTRA = "package_name";

    public static Intent starterClearChanges(Context context) {
        Intent intent = new Intent(context, TasksService.class);
        intent.setAction(CLEAR_CHANGES_ACTION);
        return intent;
    }

    public static Intent starterIgnoreApp(Context context, String packageName) {
        Intent intent = new Intent(context, TasksService.class);
        intent.setAction(IGNORE_APP_ACTION);
        intent.putExtra(IGNORE_APP_PKG_EXTRA, packageName);
        return intent;
    }

    private static final String SERVICE_NAME = "TasksService";

    private FirebaseAnalytics firebaseAnalytics;

    public TasksService() {
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (CLEAR_CHANGES_ACTION.equals(action)) {
                    firebaseAnalytics.logEvent(Analytics.Event.NOTIFICATION_CLEAR_CHANGES, null);
                    Timber.d("Tasks service: CLEAR_CHANGES_ACTION");
                    clearChanges();
                    NotificationUtils.cancelAll(this);
                } else if (IGNORE_APP_ACTION.equals(action) && intent.hasExtra(IGNORE_APP_PKG_EXTRA)) {
                    firebaseAnalytics.logEvent(Analytics.Event.NOTIFICATION_IGNORE_APP, null);
                    String packageName = intent.getStringExtra(IGNORE_APP_PKG_EXTRA);
                    Timber.d("Tasks service: IGNORE_APP_ACTION (" + packageName + ")");
                    clearChanges();
                    ignoreApp(packageName);
                    NotificationUtils.cancelAll(this);
                }
            }
        }
    }

    private void clearChanges() {
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            RealmResults<ApplicationInfo> apps = realm.where(ApplicationInfo.class)
                    .equalTo(ApplicationInfo.HAS_CHANGES_FLAG_FIELD, true)
                    .findAll();
            realm.beginTransaction();
            for (ApplicationInfo app : apps) {
                for (PermissionState permission : app.permissions) {
                    permission.hasChanged = false;
                }
                app.hasChanges = false;
            }
            realm.commitTransaction();
        } catch (Exception e) {
            Timber.e(e, "There was an error during clearing changes.");
        } finally {
            if (realm != null) realm.close();
        }
    }

    private void ignoreApp(String packageName) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            ApplicationInfo app = realm.where(ApplicationInfo.class)
                    .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, packageName)
                    .findFirst();
            if (app != null) {
                realm.beginTransaction();
                app.notifyPermissions = false;
                realm.commitTransaction();
            }
        } catch (Exception e) {
            Timber.e(e, "There was an error during ignoring app.");
        } finally {
            if (realm != null) realm.close();
        }
    }
}
