package io.github.nfdz.permissionswatcher;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.sched.SchedUtils;
import io.github.nfdz.permissionswatcher.sync.SyncJobIntentService;
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

        SchedUtils.initialize(this);

        if (PreferencesUtils.isFirstTime(this)) {
            SyncJobIntentService.startFirstTimeMode(this);
            PreferencesUtils.setFirstTime(PermissionsWatcherApp.this, false);
        }
    }

}
