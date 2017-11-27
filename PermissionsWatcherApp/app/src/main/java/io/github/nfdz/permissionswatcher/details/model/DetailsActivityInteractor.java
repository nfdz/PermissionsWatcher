package io.github.nfdz.permissionswatcher.details.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.realm.Realm;
import io.realm.Sort;

public class DetailsActivityInteractor implements DetailsActivityContract.Model {

    private Realm realm;

    @Override
    public void initialize() {
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        realm.close();
        realm = null;
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
        }
        realm.commitTransaction();
    }
}
