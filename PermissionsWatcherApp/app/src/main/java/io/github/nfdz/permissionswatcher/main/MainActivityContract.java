package io.github.nfdz.permissionswatcher.main;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.realm.RealmResults;

public interface MainActivityContract {

    interface View {
        void bindViewToLiveData(LiveData<RealmResults<ApplicationInfo>> data);
        void navigateToAppDetails(ApplicationInfo app);
    }

    interface Presenter {
        void initialize(Context context);
        void destroy();
        void onSyncSwipe();
        void onIgnoreAppClick(ApplicationInfo app);
        void onAppClick(ApplicationInfo app);
        void onShowSystemAppsFlagChanged();
    }

    interface Model {
        void initialize(Context context);
        void destroy();
        void launchSynchronization();
        LiveData<RealmResults<ApplicationInfo>> loadDataAsync();
        void toggleIgnoreFlag(ApplicationInfo app);
    }
}
