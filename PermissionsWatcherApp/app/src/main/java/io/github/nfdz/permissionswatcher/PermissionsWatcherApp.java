package io.github.nfdz.permissionswatcher;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

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
    }

}
