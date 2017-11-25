package io.github.nfdz.permissionswatcher.details.presenter;

import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.details.model.DetailsActivityInteractor;

public class DetailsActivityPresenter implements DetailsActivityContract.Presenter {

    private DetailsActivityContract.View view;
    private DetailsActivityContract.Model interactor;

    public DetailsActivityPresenter(DetailsActivityContract.View view) {
        this.view = view;
        interactor = new DetailsActivityInteractor();
    }

    @Override
    public void initialize(String packageName) {
        if (view != null && interactor != null) {
            interactor.initialize();
            view.bindViewToLiveData(interactor.loadDataAsync(packageName));
        }
    }

    @Override
    public void destroy() {
        view = null;
        if (interactor != null) interactor.destroy();
        interactor = null;
    }

    @Override
    public void onClickPermissionGroup(int permissionGroupType) {
        view.navigateToPermissionSettings(permissionGroupType);
    }

    @Override
    public void onLongClickPermissionGroup(List<PermissionState> permissions, int permissionGroupType) {
        view.showPermissionsDetailsDialog(permissions, permissionGroupType);
    }
}
