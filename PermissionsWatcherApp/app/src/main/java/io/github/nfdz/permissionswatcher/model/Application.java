package io.github.nfdz.permissionswatcher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Application extends RealmObject {

    @PrimaryKey
    public String packageName;
    public final static String PACKAGE_NAME_FIELD = "packageName";

    public String label;
    public final static String LABEL_FIELD = "label";

    public Integer versionCode;
    public final static String VERSION_CODE_FIELD = "versionCode";

    public String versionName;
    public final static String VERSION_NAME_FIELD = "versionName";

    public boolean isSystemApplication;
    public final static String IS_SYSTEM_APP_FLAG_FIELD = "isSystemApplication";

    public RealmList<String> permissions;
    public final static String PERMISSIONS_FIELD = "permissions";

    public boolean notifyPermissions;
    public final static String NOTIFY_FLAG_FIELD = "notifyPermissions";

    public Application() {
        this.packageName = null;
        this.label = null;
        this.versionCode = null;
        this.versionName = null;
        this.isSystemApplication = false;
        this.permissions = null;
        this.notifyPermissions = false;
    }

    public Application(@NonNull String packageName,
                       @Nullable String label,
                       @Nullable Integer versionCode,
                       @Nullable String versionName,
                       boolean isSystemApplication,
                       @NonNull RealmList<String> permissions,
                       boolean notifyPermissions) {
        this.packageName = packageName;
        this.label = label == null ? packageName : label;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.isSystemApplication = isSystemApplication;
        this.permissions = permissions;
        this.notifyPermissions = notifyPermissions;
    }

}
