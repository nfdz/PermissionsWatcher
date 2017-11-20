package io.github.nfdz.permissionswatcher;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.utils.RealmUtils;
import io.realm.Realm;
import timber.log.Timber;

public class PermissionsWatcherApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
        Realm.init(this);

        if (PreferencesUtils.isFirstTime(this)) {
            handleFirstTime();
        }
    }

    private void handleFirstTime() {
        Realm realm = Realm.getInstance(RealmUtils.getConfiguration());
        RealmUtils.updateRealmAppsAsync(realm,
                getPackageManager(),
                new RealmUtils.UpdateRealmCallback() {
                    @Override
                    public void notifyUpdateProgress(int progress, int total) {
                        // swallow
                    }
                    @Override
                    public void onUpdateSuccess(List<io.github.nfdz.permissionswatcher.model.Application> apps, Set<String> appsWithChanges) {
                        PreferencesUtils.setFirstTime(PermissionsWatcherApp.this, false);
                    }
                    @Override
                    public void onUpdateError() {
                        //swallow
                    }
                });
    }
}
