package io.github.nfdz.permissionswatcher.sched;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.List;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.details.view.DetailsActivityView;
import io.github.nfdz.permissionswatcher.main.view.MainActivityView;
import timber.log.Timber;

public class NotificationUtils {

    private static final int NOTIFICATION_ID = 4658;
    private static final String NOTICIATION_CHANNEL_ID = "permissions_watcher_channel";

    public static void notifyReport(Context context, List<ApplicationInfo> appsWithChanges) {
        if (appsWithChanges == null || appsWithChanges.isEmpty()) return;

        String notificationTitle = context.getString(R.string.app_name);
        String notificationTextShort;
        String notificationTextLong;
        Intent intent;

        int changes = countChanges(appsWithChanges);
        if (appsWithChanges.size() == 1) {
            ApplicationInfo app = appsWithChanges.get(0);
            intent = DetailsActivityView.starter(context, app.packageName);
            String label = TextUtils.isEmpty(app.label) ? app.packageName : app.label;
            if (changes == 1) {
                notificationTextLong = context.getString(R.string.notification_app_change_format_long, label, changes);
                notificationTextShort = context.getString(R.string.notification_app_change_format_short, label, changes);
            } else {
                notificationTextLong = context.getString(R.string.notification_app_changes_format_long, label, changes);
                notificationTextShort = context.getString(R.string.notification_app_changes_format_short, label, changes);
            }
        } else {
            intent = MainActivityView.starter(context);
            notificationTextLong = context.getString(R.string.notification_apps_changes_format_long, changes);
            notificationTextShort = context.getString(R.string.notification_apps_changes_format_short, changes);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);

        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(context, createNotificationChannel(context));
        } else {
            notificationBuilder = new NotificationCompat.Builder(context, "");
        }

        notificationBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(icon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationTextShort)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextLong))
                .setAutoCancel(true);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } else {
            Timber.e("Cannot send notification because NotificationManager is not available.");
        }
    }

    private static int countChanges(List<ApplicationInfo> apps) {
        int result = 0;
        for (ApplicationInfo app : apps) {
            for (PermissionState permission : app.permissions) {
                if (permission.hasChanged) result++;
            }
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static String createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence channelName = context.getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = new NotificationChannel(NOTICIATION_CHANNEL_ID, channelName, importance);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
            return NOTICIATION_CHANNEL_ID;
        } else {
            return "";
        }
    }
}
