package io.github.nfdz.permissionswatcher.common.model;

import java.util.Comparator;

public class AppIgnoreComparator implements Comparator<ApplicationInfo> {
    @Override
    public int compare(ApplicationInfo app1, ApplicationInfo app2) {
        if (!app1.notifyPermissions && app2.notifyPermissions) {
            return 1;
        } else if (app1.notifyPermissions && !app2.notifyPermissions) {
            return -1;
        } else {
            return app1.label.compareTo(app2.label);
        }
    }
}
