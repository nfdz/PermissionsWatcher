package io.github.nfdz.permissionswatcher;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.sync.SyncService;
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
        SyncService.start(this);
        PreferencesUtils.setFirstTime(PermissionsWatcherApp.this, false);
    }
}
