package io.github.nfdz.permissionswatcher.details;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;

public interface DetailsActivityContract {

    interface View {
        void bindViewToLiveData(LiveData<ApplicationInfo> data);
        void navigateToPermissionSettings();
        void navigateToFinish();
        void showPermissionsDetailsDialog(List<PermissionState> permissions, int permissionGroupType);
    }

    interface Presenter {
        void initialize(Context context, String packageName);
        void destroy();
        void resume();
        void onUserFinish();
        void onNoData();
        void onClickPermissionGroup();
        void onLongClickPermissionGroup(List<PermissionState> permissions, int permissionGroupType);
        void onIgnorePermissionClick(List<PermissionState> permissions, int permissionGroupType);
    }

    interface Model {
        interface OperationCallback {
            void onSuccess();
        }

        void initialize(Context context);
        void destroy();
        void launchSynchronization();
        LiveData<ApplicationInfo> loadDataAsync(String packageName);
        void toggleIgnoreFlag(String packageName, List<PermissionState> permissions, OperationCallback callback);
        void clearChangesFlags(String packageName);
    }


}
