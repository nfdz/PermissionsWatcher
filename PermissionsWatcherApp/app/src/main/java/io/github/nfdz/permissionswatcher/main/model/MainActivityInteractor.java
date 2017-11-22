package io.github.nfdz.permissionswatcher.main.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.sync.SyncService;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivityInteractor implements MainActivityContract.Model {

    private Realm realm;
    private Context context;

    @Override
    public void initialize(Context context) {
        this.context = context;
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        realm.close();
        realm = null;
        context = null;
    }

    @Override
    public void launchSynchronization() {
        if (context != null) {
            SyncService.start(context);
        }
    }

    @Override
    public LiveData<RealmResults<ApplicationInfo>> loadDataAsync() {
        if (realm != null) {
            return RealmUtils.asLiveData(realm.where(ApplicationInfo.class)
                    .findAllSortedAsync(ApplicationInfo.LABEL_FIELD, Sort.ASCENDING));
        }
        return null;
    }

}
