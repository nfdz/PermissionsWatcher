package io.github.nfdz.permissionswatcher.main;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.realm.RealmResults;

public interface MainActivityContract {

    interface View {
        void bindViewToLiveData(LiveData<RealmResults<ApplicationInfo>> data);
    }

    interface Presenter {
        void initialize(Context context);
        void destroy();
        void onSyncSwipe();
    }

    interface Model {
        void initialize(Context context);
        void destroy();
        void launchSynchronization();
        LiveData<RealmResults<ApplicationInfo>> loadDataAsync();
    }
}
