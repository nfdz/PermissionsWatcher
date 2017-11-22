package io.github.nfdz.permissionswatcher.common.model;

import io.realm.RealmObject;

public class PermissionState extends RealmObject {

    public String permission;
    public final static String PERMISSION_FIELD = "permission";

    public boolean granted;
    public final static String GRANTED_FIELD = "granted";

    public boolean hasChanged;
    public final static String HAS_CHANGED_FLAG_FIELD = "hasChanged";

    public PermissionState() {
        permission = null;
        granted = false;
        hasChanged = false;
    }

    public PermissionState(String permission, boolean granted, boolean hasChanged) {
        this.permission = permission;
        this.granted = granted;
        this.hasChanged = hasChanged;
    }

}