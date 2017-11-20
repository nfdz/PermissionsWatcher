package io.github.nfdz.permissionswatcher.main.model;

import android.content.pm.PackageManager;
import android.os.Handler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.model.Application;
import io.github.nfdz.permissionswatcher.utils.RealmUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivityInteractor implements MainActivityContract.Model {

    private Realm realm;
    private PackageManager pm;
    private Handler handler;

    public MainActivityInteractor(PackageManager pm) {
        this.pm = pm;
        this.handler = new Handler();
    }

    @Override
    public void initialize() {
        realm = Realm.getInstance(RealmUtils.getConfiguration());
    }

    @Override
    public void destroy() {
        realm.close();
        realm = null;
    }

    @Override
    public void updateApplications(final MainActivityContract.UpdateCallback callback) {
        final AtomicBoolean updateFinished = new AtomicBoolean(false);
        RealmUtils.updateRealmAppsAsync(realm, pm, new RealmUtils.UpdateRealmCallback() {
            @Override
            public void notifyUpdateProgress(final int progress, final int total) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!updateFinished.get()) {
                            callback.notifyUpdateProgress(progress, total);
                        }
                    }
                });
            }
            @Override
            public void onUpdateSuccess(List<Application> apps, Set<String> appsWithChanges) {
                updateFinished.set(true);
                callback.onUpdateSuccess(apps, appsWithChanges);
            }
            @Override
            public void onUpdateError() {
                updateFinished.set(true);
                callback.onUpdateError();
            }
        });
    }

    @Override
    public void loadApplications(final MainActivityContract.LoadCallback callback) {
        RealmResults<Application> sortedApps = realm.where(Application.class)
                .findAllSorted(Application.LABEL_FIELD, Sort.ASCENDING);
        callback.onLoadSuccess(realm.copyFromRealm(sortedApps));
    }

}
