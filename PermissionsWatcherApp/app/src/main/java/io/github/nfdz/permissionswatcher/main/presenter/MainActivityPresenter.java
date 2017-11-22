package io.github.nfdz.permissionswatcher.main.presenter;

import android.content.Context;

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
}
