package io.github.nfdz.permissionswatcher.common.model;

import java.util.Comparator;

public class AppComparator implements Comparator<ApplicationInfo> {
    @Override
    public int compare(ApplicationInfo app1, ApplicationInfo app2) {
        return app1.label.compareTo(app2.label);
    }
}
