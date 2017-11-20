package io.github.nfdz.permissionswatcher.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.nfdz.permissionswatcher.model.Application;
import io.realm.RealmList;

public class PermissionsParser {

    public interface RetrieveProcessCallback {
        void notifyProcessState(int current, int total);
    }

    private static final String ANDROID_PERMISSIONS_PREFIX = "android.permission.";

    private static final RetrieveProcessCallback NULL_CALLBACK = new RetrieveProcessCallback() {
        public void notifyProcessState(int current, int total) {}
    };

    private final PackageManager pm;
    private final RetrieveProcessCallback callback;

    public PermissionsParser(PackageManager pm, @Nullable RetrieveProcessCallback callback) {
        this.pm = pm;
        this.callback = callback == null ? NULL_CALLBACK : callback;
    }

    @Nullable
    public Map<String,Application> retrieveAllAppsWithPermissions() {
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        if (apps == null || apps.isEmpty()) return null;
        final int total = apps.size();
        int progress = 0;
        Map<String,Application> applications = new HashMap<>();
        for (ApplicationInfo app : apps) {
            callback.notifyProcessState(++progress, total);
            Set<String> permissionsSet = tryToGetPermissions(app);
            if (permissionsSet == null || permissionsSet.isEmpty()) continue;
            RealmList<String> permissionsList = new RealmList<>();
            permissionsList.addAll(permissionsSet);
            String packageName = app.packageName;
            String label = tryToGetLabel(app);
            Integer versionCode = tryToGetVersionCode(app);
            String versionName = tryToGetVersionName(app);
            boolean isSystemApplication = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            applications.put(packageName, new Application(packageName,
                    label,
                    versionCode,
                    versionName,
                    isSystemApplication,
                    permissionsList,
                    true));
        }
        return applications;
    }

    @Nullable
    private Set<String> tryToGetPermissions(ApplicationInfo app) {
        try {
            PackageInfo pi = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
            if (pi.requestedPermissions == null || pi.requestedPermissions.length == 0) return null;
            Set<String> permissions = new HashSet<>();
            for (int i = 0; i < pi.requestedPermissions.length; ++i) {
                String permission = pi.requestedPermissions[i];
                if (permission.startsWith(ANDROID_PERMISSIONS_PREFIX)) {
                    permissions.add(permission);
                }
            }
            return permissions;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private String tryToGetLabel(ApplicationInfo app) {
        try {
            return pm.getApplicationLabel(app).toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private Integer tryToGetVersionCode(ApplicationInfo app) {
        try {
            PackageInfo pi = pm.getPackageInfo(app.packageName, PackageManager.GET_META_DATA);
            return pi.versionCode;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private String tryToGetVersionName(ApplicationInfo app) {
        try {
            PackageInfo pi = pm.getPackageInfo(app.packageName, PackageManager.GET_META_DATA);
            return pi.versionName;
        } catch (Exception e) {
            return null;
        }
    }

}
