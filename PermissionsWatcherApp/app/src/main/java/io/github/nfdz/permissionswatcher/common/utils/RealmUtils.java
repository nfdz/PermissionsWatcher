package io.github.nfdz.permissionswatcher.common.utils;

import android.arch.lifecycle.LiveData;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmResults;

public class RealmUtils {

    private static final String DB_NAME = "permissions_watcher.realm";
    private static final long SCHEMA_VERSION_NAME = 0;

    public static RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(SCHEMA_VERSION_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    public static LiveData<RealmResults<ApplicationInfo>> asLiveData(RealmResults<ApplicationInfo> results) {
        return new RealmLiveData<>(results);
    }

    public static class RealmLiveData<T extends RealmModel> extends LiveData<RealmResults<T>> {

        private RealmResults<T> results;

        private final RealmChangeListener<RealmResults<T>> listener =
                new RealmChangeListener<RealmResults<T>>() {
                    @Override
                    public void onChange(RealmResults<T> results) {
                        setValue(results);
                    }
                };

        public RealmLiveData(RealmResults<T> realmResults) {
            results = realmResults;
        }

        @Override
        protected void onActive() {
            results.addChangeListener(listener);
        }

        @Override
        protected void onInactive() {
            results.removeChangeListener(listener);
        }
    }
}
