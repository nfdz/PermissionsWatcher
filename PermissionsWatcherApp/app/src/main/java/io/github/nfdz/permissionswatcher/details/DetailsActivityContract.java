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
        void showPermissionsDetailsDialog(List<PermissionState> permissions, int permissionGroupType);
    }

    interface Presenter {
        void initialize(Context context, String packageName);
        void destroy();
        void resume();
        void onClickPermissionGroup();
        void onLongClickPermissionGroup(List<PermissionState> permissions, int permissionGroupType);
        void onIgnorePermissionClick(List<PermissionState> permissions, int permissionGroupType);
    }

    interface Model {
        void initialize(Context context);
        void destroy();
        void launchSynchronization();
        LiveData<ApplicationInfo> loadDataAsync(String packageName);
        void toggleIgnoreFlag(List<PermissionState> permissions);
        void clearChangesFlags(String packageName);
    }


}
