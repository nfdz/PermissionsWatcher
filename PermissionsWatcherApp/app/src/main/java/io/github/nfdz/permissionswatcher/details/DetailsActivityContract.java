package io.github.nfdz.permissionswatcher.details;

import android.arch.lifecycle.LiveData;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;

public interface DetailsActivityContract {

    interface View {
        void bindViewToLiveData(LiveData<ApplicationInfo> data);
        void navigateToPermissionSettings(PermissionState permission);
    }

    interface Presenter {
        void initialize(String packageName);
        void destroy();
        void onClickPermission(PermissionState permission);
    }

    interface Model {
        void initialize();
        void destroy();
        LiveData<ApplicationInfo> loadDataAsync(String packageName);
    }


}
