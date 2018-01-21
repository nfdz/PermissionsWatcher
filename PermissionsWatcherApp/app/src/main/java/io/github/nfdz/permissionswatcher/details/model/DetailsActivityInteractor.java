package io.github.nfdz.permissionswatcher.details.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.sched.TasksIntentService;
import io.github.nfdz.permissionswatcher.sync.SyncJobIntentService;
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
            SyncJobIntentService.start(context);
        }
    }

    @Override
    public LiveData<ApplicationInfo> loadDataAsync(String packageName) {
        return RealmUtils.asLiveData(realm.where(ApplicationInfo.class)
                .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, packageName)
                .findFirstAsync());
    }

    @Override
    public void toggleIgnoreFlag(final String packageName, final List<PermissionState> permissions, final OperationCallback callback) {
        if (permissions == null || permissions.isEmpty()) return;
        boolean anyNotify = false;
        final Set<String> permissionsToToggle = new HashSet<>();
        for (PermissionState permission : permissions) {
            permissionsToToggle.add(permission.permission);
            anyNotify = anyNotify || permission.notifyChanges;
        }

        final boolean toggledFlag = !anyNotify;

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ApplicationInfo app = realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.PACKAGE_NAME_FIELD, packageName)
                        .findFirst();
                if (app != null) {
                    for (PermissionState permissionState : app.permissions) {
                        if (permissionsToToggle.contains(permissionState.permission)) {
                            permissionState.notifyChanges =  toggledFlag;
                            if (!toggledFlag /* not notify */) {
                                permissionState.hasChanged = false;
                            }
                        }
                    }
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }
        });
    }

    @Override
    public void clearChangesFlags(String packageName) {
        if (context != null) {
            TasksIntentService.startClearChanges(context, packageName);
        }
    }
}
