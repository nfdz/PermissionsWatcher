package io.github.nfdz.permissionswatcher.common.model;


import java.util.List;

import io.github.nfdz.permissionswatcher.common.utils.SimpleDiffUtilListCallback;

public class AppEqualsStrategy implements SimpleDiffUtilListCallback.EqualsStrategy<ApplicationInfo> {

    @Override
    public boolean sameItem(ApplicationInfo item1, ApplicationInfo item2) {
        boolean samePkg = item1.packageName.equals(item2.packageName);
        boolean sameSystemApplicationFlag = item1.isSystemApplication == item2.isSystemApplication;
        boolean sameNotifyPermissionsFlag = item1.notifyPermissions == item2.notifyPermissions;
        boolean sameHasChangesFlag = item1.hasChanges == item2.hasChanges;
        boolean sameLabel = item1.label != null ? item1.label.equals(item2.label) : item2.label == null;
        boolean sameVersionCode = item1.versionCode != null ? item1.versionCode.equals(item2.versionCode) : item2.versionCode == null;
        boolean sameVersionName = item1.versionName != null ? item1.versionName.equals(item2.versionName) : item2.versionName == null;

        return samePkg &&
                sameSystemApplicationFlag &&
                sameNotifyPermissionsFlag &&
                sameHasChangesFlag &&
                sameLabel &&
                sameVersionCode &&
                sameVersionName &&
                samePermissionsSummary(item1.permissions, item2.permissions);
    }

    private boolean samePermissionsSummary(List<PermissionState> permissions1, List<PermissionState> permissions2) {
        int size1 = permissions1.size();
        int size2 = permissions2.size();
        if (size1 == size2) {
            int granted1 = 0;
            for (PermissionState permission : permissions1) {
                if (permission.granted) granted1++;
            }
            int granted2 = 0;
            for (PermissionState permission : permissions2) {
                if (permission.granted) granted2++;
            }
            return granted1 == granted2;
        }
        return false;
    }
}
