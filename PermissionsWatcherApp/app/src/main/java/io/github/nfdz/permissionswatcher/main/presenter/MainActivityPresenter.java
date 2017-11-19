package io.github.nfdz.permissionswatcher.main.presenter;

import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.model.MainActivityInteractor;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    private MainActivityContract.View view;
    private MainActivityContract.Model interactor;

    public MainActivityPresenter(MainActivityContract.View view) {
        this.view = view;
        this.interactor = new MainActivityInteractor();
    }

    @Override
    public void initialize() {
        if (view != null && interactor != null) {
            view.showLoading();
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onUpdateSwipe() {

    }

}
