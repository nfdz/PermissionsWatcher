package io.github.nfdz.permissionswatcher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

public class Application {

    @NonNull
    public final String packageName;

    @Nullable
    public final String label;

    @Nullable
    public final Integer versionCode;

    @Nullable
    public final String versionName;

    public final boolean isSystemApplication;

    @NonNull
    public final Set<String> permissions;

    public Application(@NonNull String packageName,
                       @Nullable String label,
                       @Nullable Integer versionCode,
                       @Nullable String versionName,
                       boolean isSystemApplication,
                       @NonNull Set<String> permissions) {
        this.packageName = packageName;
        this.label = label;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.isSystemApplication = isSystemApplication;
        this.permissions = permissions;
    }

}
