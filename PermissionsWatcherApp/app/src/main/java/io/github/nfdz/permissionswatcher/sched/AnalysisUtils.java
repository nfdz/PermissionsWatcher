package io.github.nfdz.permissionswatcher.sched;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.common.utils.RealmUtils;
import io.github.nfdz.permissionswatcher.sync.SyncUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class AnalysisUtils {

    public interface AnalysisCallback {
        void onAnalysisSuccess(List<ApplicationInfo> appsWithChanges);
        void onAnalysisError(Throwable e);
    }

    public static List<ApplicationInfo> tryPerformAnalysis(Context context) throws Exception {
        Realm realm = null;
        try {
            Realm.init(context);
            realm = Realm.getInstance(RealmUtils.getConfiguration());
            SyncUtils.tryToSync(realm, context.getPackageManager(), false);
            boolean ignoreSystemApps = !PreferencesUtils.showSystemApps(context);
            RealmResults<ApplicationInfo> apps;
            if (ignoreSystemApps) {
                apps = realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.IS_SYSTEM_APP_FLAG_FIELD, false)
                        .equalTo(ApplicationInfo.HAS_CHANGES_FLAG_FIELD, true)
                        .findAll();
            } else {
                apps = realm.where(ApplicationInfo.class)
                        .equalTo(ApplicationInfo.HAS_CHANGES_FLAG_FIELD, true)
                        .findAll();
            }
            return realm.copyFromRealm(apps);
        } finally {
            if (realm != null) realm.close();
        }
    }

    public static AnalysisTask createAnalysisTask(Context context, AnalysisCallback callback) {
        return new AnalysisTask(context, callback);
    }

    public static class AnalysisTask extends AsyncTask<Void,Void,Void> {

        private final WeakReference<Context> contextReference;
        private final WeakReference<AnalysisCallback> callbackReference;
        private Exception error = null;
        private List<ApplicationInfo> result = null;

        public AnalysisTask(Context context, AnalysisCallback callback) {
            this.contextReference = new WeakReference<>(context);
            this.callbackReference = new WeakReference<>(callback);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Context context = contextReference.get();
                if (context == null) throw new Exception("Context is not available");
                result = tryPerformAnalysis(context);
            } catch (Exception e) {
                error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AnalysisCallback callback = callbackReference.get();
            if (callback == null) return;
            if (result != null && error == null) {
                callback.onAnalysisSuccess(result);
            } else {
                callback.onAnalysisError(error);
            }
        }
    }
}
