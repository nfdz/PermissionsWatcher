package io.github.nfdz.permissionswatcher.main;

import android.app.Application;

import java.util.List;
import java.util.Set;

public interface MainActivityContract {

    interface View {
        void showEmpty();
        void showLoading();
        void showData(List<Application> applications);
    }

    interface Presenter {
        void initialize();
        void destroy();
        void onUpdateSwipe();
    }

    interface Model {
        void initialize();
        void destroy();
        void update(UpdateCallback callback);
        void loadApplications(LoadCallback callback);
    }

    interface UpdateCallback {
        void notifyUpdateProgress(int progress, int total);
        void onUpdateSuccess();
    }

    interface LoadCallback {
        void onLoadSuccess(List<Application> applications, Set<String> appsWithChanges);
    }

}
