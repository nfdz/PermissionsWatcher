package io.github.nfdz.permissionswatcher.main.presenter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.model.MainActivityInteractor;

public class MainActivityPresenter implements MainActivityContract.Presenter  {

    private MainActivityContract.View view;
    private MainActivityContract.Model interactor;

    public MainActivityPresenter(MainActivityContract.View view) {
        this.view = view;
        this.interactor = new MainActivityInteractor();
    }

    @Override
    public void initialize(Context context) {
        if (view != null && interactor != null) {
            interactor.initialize(context);
            view.bindViewToLiveData(interactor.loadDataAsync());
        }
    }

    @Override
    public void resume() {
        if (interactor != null) interactor.launchSynchronization();
    }

    @Override
    public void destroy() {
        view = null;
        if (interactor != null) interactor.destroy();
        interactor = null;
    }

    @Override
    public void onSyncSwipe() {
        if (interactor != null) {
            interactor.launchSynchronization();
        }
    }

    @Override
    public void onIgnoreAppClick(ApplicationInfo app) {
        if (interactor != null) {
            interactor.toggleIgnoreFlag(app);
        }
    }

    @Override
    public void onAppClick(ApplicationInfo app, ImageView appIcon) {
        view.navigateToAppDetails(app, appIcon);
    }

    @Override
    public void onShowSystemAppsFlagChanged() {
        if (view != null && interactor != null) {
            view.bindViewToLiveData(interactor.loadDataAsync());
        }
    }

    @Override
    public void onSearchQueryChanged(@Nullable String query) {
        view.filterContent(query);
    }
}
