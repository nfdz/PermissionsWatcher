package io.github.nfdz.permissionswatcher.sync;

import android.content.pm.PackageManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsParser;
import io.realm.Realm;
import io.realm.RealmList;

public class SyncUtils {

    public static void tryToSync(Realm realm, PackageManager pm) {
        realm.beginTransaction();
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
    }

    private static void delete(ApplicationInfo app) {
        for (PermissionState permission : app.permissions) {
            permission.deleteFromRealm();
        }
        app.deleteFromRealm();
    }

    private static boolean processPermissions(Realm realm,
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
