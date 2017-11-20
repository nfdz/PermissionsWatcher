package io.github.nfdz.permissionswatcher.main;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.model.Application;

public interface MainActivityContract {

    interface View {
        void showEmpty();
        void showData(List<Application> applications, @Nullable Set<String> appsWithChanges);
        void showUpdating(int progress, int total);
        void hideUpdating();
        void showUpdateErrorMessage();
    }

    interface Presenter {
        void initialize();
        void destroy();
        void onUpdateSwipe();
    }

    interface Model {
        void initialize();
        void destroy();
        void updateApplications(UpdateCallback callback);
        void loadApplications(LoadCallback callback);
    }

    interface UpdateCallback {
        void notifyUpdateProgress(int progress, int total);
        void onUpdateSuccess(List<Application> applications, Set<String> appsWithChanges);
        void onUpdateError();
    }

    interface LoadCallback {
        void onLoadSuccess(List<Application> applications);
    }

}
