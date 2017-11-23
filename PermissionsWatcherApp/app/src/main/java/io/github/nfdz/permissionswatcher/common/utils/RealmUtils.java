package io.github.nfdz.permissionswatcher.common.utils;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmObject;
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

    public static <T extends RealmObject> LiveData<T> asLiveData(T result) {
        return new RealmObjectLiveData<>(result);
    }

    public static class RealmObjectLiveData<T extends RealmObject> extends LiveData<T> {

        private T object;

        private final RealmChangeListener<T> listener =
                new RealmChangeListener<T>() {
                    @Override
                    public void onChange(@NonNull T result) {
                        setValue(result);
                    }
                };

        public RealmObjectLiveData(T object) {
            this.object = object;
        }

        @Override
        protected void onActive() {
            object.addChangeListener(listener);
        }

        @Override
        protected void onInactive() {
            object.removeChangeListener(listener);
        }
    }
}
