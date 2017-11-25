package io.github.nfdz.permissionswatcher.details;

import android.arch.lifecycle.LiveData;

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
        void initialize(String packageName);
        void destroy();
        void onClickPermissionGroup();
        void onLongClickPermissionGroup(List<PermissionState> permissions, int permissionGroupType);
    }

    interface Model {
        void initialize();
        void destroy();
        LiveData<ApplicationInfo> loadDataAsync(String packageName);
    }


}
