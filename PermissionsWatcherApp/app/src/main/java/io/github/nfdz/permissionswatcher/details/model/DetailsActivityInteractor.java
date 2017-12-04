package io.github.nfdz.permissionswatcher.details.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.sync.SyncService;
import io.realm.Realm;

public class DetailsActivityInteractor implements DetailsActivityContract.Model {

    private Realm realm;
    private Context context;

    @Override
    public void initialize(Context context) {
        realm = Realm.getInstance(RealmUtils.getConfiguration());
        this.context = context;
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
    public LiveData<ApplicationInfo> loadDataAsync(String packageName) {
        return RealmUtils.asLiveData(realm.where(ApplicationInfo.class)
                .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, packageName)
                .findFirstAsync());
    }

    @Override
    public void toggleIgnoreFlag(List<PermissionState> permissions) {
        if (permissions == null || permissions.isEmpty()) return;
        boolean anyNotify = false;
        for (PermissionState permission : permissions) {
            anyNotify = anyNotify || permission.notifyChanges;
        }

        boolean toggledFlag = !anyNotify;

        realm.beginTransaction();
        for (PermissionState permission : permissions) {
            permission.notifyChanges =  toggledFlag;
            if (!toggledFlag /* not notify */) {
                permission.hasChanged = false;
            }
        }
        realm.commitTransaction();
    }

    @Override
    public void clearChangesFlags(final String packageName) {
        ApplicationInfo managedApp = realm.where(ApplicationInfo.class)
                .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, packageName)
                .findFirst();
        if (managedApp != null) {
            realm.beginTransaction();
            managedApp.hasChanges = false;
            for (PermissionState permission : managedApp.permissions) {
                permission.hasChanged = false;
            }
            realm.commitTransaction();
        }
    }
}
