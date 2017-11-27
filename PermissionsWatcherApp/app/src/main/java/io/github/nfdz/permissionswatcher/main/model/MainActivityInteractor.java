package io.github.nfdz.permissionswatcher.main.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.sync.SyncService;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivityInteractor implements MainActivityContract.Model {

    private Realm realm;
    private Context context;

    @Override
    public void initialize(Context context, boolean isFirstInitialize) {
        this.context = context;
        realm = Realm.getInstance(RealmUtils.getConfiguration());

        // launch synchronization each time that main activity starts
        // (avoid launch when activity has configuration changes or etc)
        if (isFirstInitialize) {
            launchSynchronization();
        }
    }

    @Override
    public void destroy() {
        realm.close();
        realm = null;
        context = null;
    }

    @Override
    public void launchSynchronization() {
        if (context != null) {
            SyncService.start(context);
        }
    }

    @Override
    public LiveData<RealmResults<ApplicationInfo>> loadDataAsync() {
        if (realm != null && context != null) {
            boolean showSystemApps = PreferencesUtils.showSystemApps(context);
            if (showSystemApps) {
                return RealmUtils.asLiveData(realm.where(ApplicationInfo.class)
                        .findAllSortedAsync(ApplicationInfo.LABEL_FIELD, Sort.ASCENDING));
            } else {
                return RealmUtils.asLiveData(realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.IS_SYSTEM_APP_FLAG_FIELD, false)
                        .findAllSortedAsync(ApplicationInfo.LABEL_FIELD, Sort.ASCENDING));
            }
        }
        return null;
    }

    @Override
    public void toggleIgnoreFlag(ApplicationInfo app) {
        if (realm != null) {
            final String pkgName = app.packageName;
            final boolean toggledFlag = !app.notifyPermissions;
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ApplicationInfo managedApp = realm.where(ApplicationInfo.class)
                            .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, pkgName)
                            .findFirst();
                    if (managedApp != null) {
                        managedApp.notifyPermissions = toggledFlag;
                        // remove changes flags
                        if (!toggledFlag /* not notify */) {
                            managedApp.hasChanges = false;
                            for (PermissionState permission : managedApp.permissions) {
                                permission.hasChanged = false;
                            }
                        }
                    }
                }
            });

        }
    }
}
