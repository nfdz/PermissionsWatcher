package io.github.nfdz.permissionswatcher.utils;

import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.nfdz.permissionswatcher.model.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

public class RealmUtils {

    private static final String DB_NAME = "permissions_watcher.realm";
    private static final long SCHEMA_VERSION_NAME = 0;

    public static RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(SCHEMA_VERSION_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    public interface UpdateRealmCallback {
        void notifyUpdateProgress(int progress, int total);
        void onUpdateSuccess(List<Application> apps, Set<String> appsWithChanges);
        void onUpdateError();
    }

    public static void updateRealmAppsAsync(Realm realm,
                                            final PackageManager pm,
                                            final UpdateRealmCallback callback) {
        final Set<String> appsWithChanges = new HashSet<>();
        final List<Application> apps = new ArrayList<>();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<Application> storedApps = realm.where(Application.class).findAll();
                PermissionsParser parser = new PermissionsParser(pm, null);
                Map<String,Application> parsedApps = parser.retrieveAllAppsWithPermissions();

                int progress = 0;
                int totalToProcess = storedApps.size() + parsedApps.size();

                for (Application storedApp : storedApps) {
                    callback.notifyUpdateProgress(++progress, totalToProcess);
                    if (parsedApps.containsKey(storedApp.packageName)) {
                        Application parsedApp = parsedApps.get(storedApp.packageName);
                        storedApp.label = parsedApp.label;
                        storedApp.versionName = parsedApp.versionName;
                        storedApp.versionCode = parsedApp.versionCode;
                        storedApp.isSystemApplication = parsedApp.isSystemApplication;
                        Set<String> storedPermissions = new HashSet<>(storedApp.permissions);
                        Set<String> parsedPermissions = new HashSet<>(storedApp.permissions);
                        if (!storedPermissions.equals(parsedPermissions)) {
                            appsWithChanges.add(storedApp.packageName);
                            storedApp.permissions.clear();
                            storedApp.permissions.addAll(parsedPermissions);
                        }
                        parsedApps.remove(storedApp.packageName);
                        totalToProcess--;
                    } else {
                        storedApp.deleteFromRealm();
                    }
                }
                for (Map.Entry<String,Application> entry : parsedApps.entrySet()) {
                    callback.notifyUpdateProgress(++progress, totalToProcess);
                    appsWithChanges.add(entry.getKey());
                    realm.copyToRealm(entry.getValue());
                }

                RealmResults<Application> sortedApps = realm.where(Application.class)
                        .findAllSorted(Application.LABEL_FIELD, Sort.ASCENDING);
                apps.addAll(realm.copyFromRealm(sortedApps));
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Timber.d("Application loaded successfully: apps.size="+apps.size()+", appsWithChanges.size="+appsWithChanges.size());
                callback.onUpdateSuccess(apps, appsWithChanges);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Timber.d(error, "Error loading applications. ");
                callback.onUpdateError();
            }
        });
    }


}
