package io.github.nfdz.permissionswatcher.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsParser;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.realm.Realm;
import io.realm.RealmList;
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
            PermissionsParser parser = new PermissionsParser(pm);
            Map<String,ApplicationInfo> parsedApps = parser.retrieveAllAppsWithPermissions();

            for (ApplicationInfo storedApp : storedApps) {
                if (parsedApps.containsKey(storedApp.packageName)) {
                    ApplicationInfo parsedApp = parsedApps.get(storedApp.packageName);
                    storedApp.label = parsedApp.label;
                    storedApp.versionName = parsedApp.versionName;
                    storedApp.versionCode = parsedApp.versionCode;
                    storedApp.isSystemApplication = parsedApp.isSystemApplication;
                    boolean anyChange = processPermissions(realm, storedApp.permissions, parsedApp.permissions);
                    storedApp.hasChanges = storedApp.hasChanges || anyChange;
                    parsedApps.remove(storedApp.packageName);
                } else {
                    delete(storedApp);
                }
            }
            for (Map.Entry<String,ApplicationInfo> entry : parsedApps.entrySet()) {
                ApplicationInfo.copyToRealm(realm, entry.getValue());
            }
            realm.commitTransaction();

            Timber.d("Synchronization finishes successfully");
        } catch (Exception e) {
            Timber.d(e, "There was an error during synchronization.");
        } finally {
            if (realm != null) realm.close();
        }
    }

    private void delete(ApplicationInfo app) {
        for (PermissionState permission : app.permissions) {
            permission.deleteFromRealm();
        }
        app.deleteFromRealm();
    }

    private boolean processPermissions(Realm realm,
                                       RealmList<PermissionState> storedPermissions,
                                       RealmList<PermissionState> parsedPermissions) {
        boolean anyChange = false;

        // update or delete stored ones
        Iterator<PermissionState> it = storedPermissions.iterator();
        while(it.hasNext()) {
            PermissionState storedPermission = it.next();
            boolean deleted = true;
            for (PermissionState parsedPermission : parsedPermissions) {
                if (storedPermission.permission.equals(parsedPermission.permission)) {
                    // notify only if was not granted and now is granted
                    boolean wasGranted = storedPermission.granted;
                    boolean isGranted = parsedPermission.granted;
                    if (wasGranted != isGranted) {
                        storedPermission.granted = parsedPermission.granted;
                        if (!wasGranted && isGranted) {
                            storedPermission.hasChanged = true;
                            anyChange = true;
                        }
                    }
                    deleted = false;
                    break;
                }
            }
            if (deleted) {
                storedPermission.deleteFromRealm();
                it.remove();
            }
        }

        // look for new ones
        for (PermissionState parsedPermission : parsedPermissions) {
            boolean add = true;
            for (PermissionState storedPermission : storedPermissions) {
                if (storedPermission.permission.equals(parsedPermission.permission)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                storedPermissions.add(realm.copyToRealm(parsedPermission));
                anyChange = true;
            }
        }

        return anyChange;
    }
}
