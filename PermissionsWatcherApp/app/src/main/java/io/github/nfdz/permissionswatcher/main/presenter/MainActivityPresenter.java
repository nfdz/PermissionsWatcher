package io.github.nfdz.permissionswatcher.main.presenter;

import android.content.pm.PackageManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.model.MainActivityInteractor;
import io.github.nfdz.permissionswatcher.model.Application;

public class MainActivityPresenter implements MainActivityContract.Presenter,
        MainActivityContract.LoadCallback, MainActivityContract.UpdateCallback {

    private MainActivityContract.View view;
    private MainActivityContract.Model interactor;

    public MainActivityPresenter(MainActivityContract.View view, PackageManager pm) {
        this.view = view;
        this.interactor = new MainActivityInteractor(pm);
    }

    @Override
    public void initialize() {
        if (view != null && interactor != null) {
            interactor.initialize();
            interactor.loadApplications(this);
        }
    }

    @Override
    public void destroy() {
        view = null;
        if (interactor != null) interactor.destroy();
        interactor = null;
    }

    @Override
    public void onUpdateSwipe() {
        if (interactor != null) {
            interactor.updateApplications(this);
        }
    }

    @Override
    public void onLoadSuccess(List<Application> applications) {
        if (view != null) {
            view.showData(applications, Collections.<String>emptySet());
        }
    }

    @Override
    public void notifyUpdateProgress(int progress, int total) {
        if (view != null) {
            view.showUpdating(progress, total);
        }
    }

    @Override
    public void onUpdateSuccess(List<Application> applications, Set<String> appsWithChanges) {
        if (view != null) {
            view.showData(applications, appsWithChanges);
        }
    }

    @Override
    public void onUpdateError() {
        if (view != null) {
            view.showUpdateErrorMessage();
            view.hideUpdating();
        }
    }
}
