package io.github.nfdz.permissionswatcher.details.presenter;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.details.model.DetailsActivityInteractor;

public class DetailsActivityPresenter implements DetailsActivityContract.Presenter {

    private DetailsActivityContract.View view;
    private DetailsActivityContract.Model interactor;
    private String packageName;

    public DetailsActivityPresenter(DetailsActivityContract.View view) {
        this.view = view;
        interactor = new DetailsActivityInteractor();
    }

    @Override
    public void initialize(String packageName) {
        if (view != null && interactor != null) {
            this.packageName = packageName;
            interactor.initialize();
            view.bindViewToLiveData(interactor.loadDataAsync(packageName));
        }
    }

    @Override
    public void destroy() {
        packageName = null;
        view = null;
        if (interactor != null) interactor.destroy();
        interactor = null;
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
