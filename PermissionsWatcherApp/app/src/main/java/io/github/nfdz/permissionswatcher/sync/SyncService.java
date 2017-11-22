package io.github.nfdz.permissionswatcher.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsParser;
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
        tryToSync();
    }

    private void tryToSync() {
        Realm realm = null;
        try {
            realm = Realm.getInstance(RealmUtils.getConfiguration());

            realm.beginTransaction();
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> storedApps = realm.where(ApplicationInfo.class).findAll();
            PermissionsParser parser = new PermissionsParser(pm, null);
            Map<String,ApplicationInfo> parsedApps = parser.retrieveAllAppsWithPermissions();

            for (ApplicationInfo storedApp : storedApps) {
                if (parsedApps.containsKey(storedApp.packageName)) {
                    ApplicationInfo parsedApp = parsedApps.get(storedApp.packageName);
                    storedApp.label = parsedApp.label;
                    storedApp.versionName = parsedApp.versionName;
                    storedApp.versionCode = parsedApp.versionCode;
                    storedApp.isSystemApplication = parsedApp.isSystemApplication;
                    Set<String> storedPermissions = new HashSet<>(storedApp.permissions);
                    Set<String> parsedPermissions = new HashSet<>(parsedApp.permissions);
                    if (!storedPermissions.equals(parsedPermissions)) {
                        storedApp.permissions.clear();
                        storedApp.permissions.addAll(parsedPermissions);
                        storedApp.hasChanges = true;
                    }
                    parsedApps.remove(storedApp.packageName);
                } else {
                    storedApp.deleteFromRealm();
                }
            }
            for (Map.Entry<String,ApplicationInfo> entry : parsedApps.entrySet()) {
                realm.copyToRealm(entry.getValue());
            }
            realm.commitTransaction();

            Timber.d("Synchronization finishes successfully");
        } catch (Exception e) {
            Timber.d(e, "There was an error during synchronization.");
        } finally {
            if (realm != null) realm.close();
        }
    }
}
