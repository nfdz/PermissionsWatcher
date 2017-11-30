package io.github.nfdz.permissionswatcher.sched;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.sync.SyncUtils;
import io.realm.Realm;
import timber.log.Timber;

public class ReportService extends IntentService {

    public static final String SERVICE_NAME = "ReportService";

    public static void start(Context context) {
        context.startService(new Intent(context, ReportService.class));
    }

    public ReportService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Timber.d("Report service started");
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, getPackageManager());
            boolean ignoreSystemApps = !PreferencesUtils.showSystemApps(this);
            List<ApplicationInfo> apps;
            if (ignoreSystemApps) {
                apps = realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.IS_SYSTEM_APP_FLAG_FIELD, false)
                        .equalTo(ApplicationInfo.HAS_CHANGES_FLAG_FIELD, true)
                        .findAll();
            } else {
                apps = realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.HAS_CHANGES_FLAG_FIELD, true)
                        .findAll();
            }
            NotificationUtils.notifyReport(this, apps);
            Timber.d("Report finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during reporting.");
        } finally {
            if (realm != null) realm.close();
            SchedUtils.rescheduleReport(this);
        }

    }
}
