package io.github.nfdz.permissionswatcher.sched;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.sync.SyncUtils;
import io.realm.Realm;
import timber.log.Timber;

public class ReportService extends IntentService {

    private static final String ANALYZE_ACTION = "ANALYZE";
    private static final String REAL_TIME_ACTION = "REAL_TIME";

    public static final String SERVICE_NAME = "ReportService";

    public static void startRealTimeMode(Context context) {
        Intent starter = new Intent(context, ReportService.class);
        starter.setAction(REAL_TIME_ACTION);
        context.startService(starter);
    }

    public static void startAnalysisMode(Context context) {
        Intent starter = new Intent(context, ReportService.class);
        starter.setAction(ANALYZE_ACTION);
        context.startService(starter);
    }

    public ReportService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Timber.d("Report service started");

        boolean analyzeMode = isAnalyzeMode(intent);
        boolean isRealTimeMode = isRealTimeMode(intent);

        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, getPackageManager(), false);
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
            NotificationUtils.notifyReport(this, apps, analyzeMode);
            Timber.d("Report finished successfully.");
        } catch (Exception e) {
            Timber.e(e, "There was an error during reporting.");
            FirebaseCrash.report(e);
        } finally {
            if (realm != null) realm.close();
            if (!isRealTimeMode) SchedUtils.rescheduleReport(this);
        }

    }

    private boolean isAnalyzeMode(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && ANALYZE_ACTION.equals(action)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRealTimeMode(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && REAL_TIME_ACTION.equals(action)) {
                return true;
            }
        }
        return false;
    }
}
