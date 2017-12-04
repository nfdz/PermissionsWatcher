package io.github.nfdz.permissionswatcher.main;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.realm.RealmResults;

public interface MainActivityContract {

    interface View {
        void bindViewToLiveData(LiveData<RealmResults<ApplicationInfo>> data);
        void navigateToAppDetails(ApplicationInfo app, ImageView appIcon);
        void filterContent(@Nullable String query);
    }

    interface Presenter {
        void initialize(Context context);
        void resume();
        void destroy();
        void onSyncSwipe();
        void onIgnoreAppClick(ApplicationInfo app);
        void onAppClick(ApplicationInfo app, ImageView appIcon);
        void onShowSystemAppsFlagChanged();
        void onSearchQueryChanged(@Nullable String query);
    }

    interface Model {
        void initialize(Context context);
        void destroy();
        void launchSynchronization();
        LiveData<RealmResults<ApplicationInfo>> loadDataAsync();
        void toggleIgnoreFlag(ApplicationInfo app);
    }
}
