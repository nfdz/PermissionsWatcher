package io.github.nfdz.permissionswatcher.details.presenter;

import android.content.Context;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.details.model.DetailsActivityInteractor;

public class DetailsActivityPresenter implements DetailsActivityContract.Presenter {

    private DetailsActivityContract.View view;
    private DetailsActivityContract.Model interactor;
    private String packageName;
    private boolean skippedFirstResume = false;

    public DetailsActivityPresenter(DetailsActivityContract.View view) {
        this.view = view;
        interactor = new DetailsActivityInteractor();
    }

    @Override
    public void initialize(Context context, String packageName) {
        if (view != null && interactor != null) {
            this.packageName = packageName;
            interactor.initialize(context);
            view.bindViewToLiveData(interactor.loadDataAsync(packageName));
        }
    }

    @Override
    public void destroy() {
        if (interactor != null) {
            interactor.destroy();
        }
        interactor = null;
        packageName = null;
        view = null;
    }

    @Override
    public void resume() {
        if (skippedFirstResume) {
            if (interactor != null) {
                interactor.launchSynchronization();
            }
        } else {
            skippedFirstResume = true;
        }
    }

    @Override
    public void onUserFinish() {
        if (view != null && interactor != null && packageName != null) {
            interactor.clearChangesFlags(packageName);
            view.navigateToFinish();
        }
    }

    @Override
    public void onNoData() {
        if (view != null) {
            view.navigateToFinish();
        }
    }

    @Override
    public void onClickPermissionGroup() {
        view.navigateToPermissionSettings();
    }

    @Override
    public void onLongClickPermissionGroup(List<PermissionState> permissions, int permissionGroupType) {
        view.showPermissionsDetailsDialog(permissions, permissionGroupType);
    }

    @Override
    public void onIgnorePermissionClick(List<PermissionState> permissions, int permissionGroupType) {
        List<PermissionState> permissionsList = PermissionsUtils.filterPermissions(permissions, permissionGroupType, false);
        interactor.toggleIgnoreFlag(permissionsList);
        view.bindViewToLiveData(interactor.loadDataAsync(packageName));
    }
}
