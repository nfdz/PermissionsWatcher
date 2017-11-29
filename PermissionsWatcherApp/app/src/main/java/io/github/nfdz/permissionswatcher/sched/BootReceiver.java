package io.github.nfdz.permissionswatcher.sched;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.nfdz.permissionswatcher.sched.SchedUtils;

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        SchedUtils.initialize(context);
    }

}