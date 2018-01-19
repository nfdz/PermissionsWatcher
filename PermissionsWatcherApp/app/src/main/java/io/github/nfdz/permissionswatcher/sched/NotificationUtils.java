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

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.details.view.DetailsActivityView;
import io.github.nfdz.permissionswatcher.main.view.MainActivityView;
import timber.log.Timber;

public class NotificationUtils {

    private static final String NOTIFICATION_ERROR = "Cannot send notification because NotificationManager is not available.";
    private static final int NOTIFICATION_ID = 4658;
    private static final String NOTICIATION_CHANNEL_ID = "permissions_watcher_channel";

    public static void notifyReport(Context context, List<ApplicationInfo> appsWithChanges) {
        notifyReport(context, appsWithChanges, false);
    }

    public static void notifyReport(Context context, List<ApplicationInfo> appsWithChanges, boolean notifyEverythingAlright) {
        boolean noApps = appsWithChanges == null || appsWithChanges.isEmpty();
        if (noApps && notifyEverythingAlright) {
            notifyEverythingAlright(context);
        } else if (!noApps) {
            notifyWarnings(context, appsWithChanges);
        }
    }

    private static void notifyWarnings(Context context, List<ApplicationInfo> appsWithChanges) {
        String notificationTitle = context.getString(R.string.app_name);
        String notificationTextShort;
        String notificationTextLong;
        Intent intent;
        List<NotificationCompat.Action> actions = new ArrayList<>();

        int changes = countChanges(appsWithChanges);
        if (appsWithChanges.size() == 1) {
            ApplicationInfo app = appsWithChanges.get(0);
            intent = DetailsActivityView.starter(context, app.packageName);
            String label = TextUtils.isEmpty(app.label) ? app.packageName : app.label;
            if (changes == 1) {
                String permission = getPermissionChangeText(context, app);
                notificationTextLong = context.getString(R.string.notification_app_change_format_long, label, permission);
                notificationTextShort = context.getString(R.string.notification_app_change_format_short, label, permission);
            } else {
                notificationTextLong = context.getString(R.string.notification_app_changes_format_long, label, changes);
                notificationTextShort = context.getString(R.string.notification_app_changes_format_short, label, changes);
            }

            Intent okIntent = TasksIntentService.starterClearChanges(context);
            actions.add(new NotificationCompat.Action.Builder(R.drawable.ic_notification_ok,
                    context.getString(R.string.notification_app_action_ok),
                    PendingIntent.getService(context, 0, okIntent, PendingIntent.FLAG_UPDATE_CURRENT)).build());
            Intent setUpIntent = PermissionsUtils.starterSettingsActivity(app.packageName);
            actions.add(new NotificationCompat.Action.Builder(R.drawable.ic_notification_setup,
                    context.getString(R.string.notification_app_action_set_up),
                    PendingIntent.getActivity(context, 0, setUpIntent, PendingIntent.FLAG_UPDATE_CURRENT)).build());
            Intent ignoreAppIntent = TasksIntentService.starterIgnoreApp(context, app.packageName);
            actions.add(new NotificationCompat.Action.Builder(R.drawable.ic_notification_ignore,
                    context.getString(R.string.notification_app_action_ignore_app),
                    PendingIntent.getService(context, 0, ignoreAppIntent, PendingIntent.FLAG_UPDATE_CURRENT)).build());
        } else {
            intent = MainActivityView.starter(context);
            notificationTextLong = context.getString(R.string.notification_apps_changes_format_long, changes);
            notificationTextShort = context.getString(R.string.notification_apps_changes_format_short, changes);

            Intent okIntent = TasksIntentService.starterClearChanges(context);
            actions.add(new NotificationCompat.Action.Builder(R.drawable.ic_notification_ok,
                    context.getString(R.string.notification_app_action_ok),
                    PendingIntent.getService(context, 0, okIntent, PendingIntent.FLAG_UPDATE_CURRENT)).build());
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

//        Intent clearChangesIntent = TasksIntentService.starterClearChanges(context);
//        notificationBuilder.setDeleteIntent(PendingIntent.getService(context, 0, clearChangesIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        for (NotificationCompat.Action action : actions) {
            notificationBuilder.addAction(action);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } else {
            Timber.e(NOTIFICATION_ERROR);
            FirebaseCrash.report(new Exception(NOTIFICATION_ERROR));
        }
    }

    private static String getPermissionChangeText(final Context context, ApplicationInfo app) {
        for (PermissionState permissionState : app.permissions) {
            if (permissionState.hasChanged) {
                final String permission = permissionState.permission;
                int permissionType = PermissionsUtils.getType(permission);
                final AtomicReference<String> permissionGroupName = new AtomicReference<>("");
                PermissionsUtils.visitPermissionType(permissionType, new PermissionsUtils.PermissionTypeVisitor() {
                    @Override
                    public void visitCalendarType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_calendar));
                    }
                    @Override
                    public void visitCameraType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_camera));
                    }
                    @Override
                    public void visitContactsType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_contacts));
                    }
                    @Override
                    public void visitLocationType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_location));
                    }
                    @Override
                    public void visitMicrophoneType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_mic));
                    }
                    @Override
                    public void visitPhoneType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_phone));
                    }
                    @Override
                    public void visitSensorsType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_sensors));
                    }
                    @Override
                    public void visitSMSType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_sms));
                    }
                    @Override
                    public void visitStorageType() {
                        permissionGroupName.set(context.getString(R.string.permissions_type_storage));
                    }
                    @Override
                    public void visitUnknownType() {
                        permissionGroupName.set(permission);
                    }
                });
                return permissionGroupName.get();
            }
        }
        return context.getString(R.string.permissions_type_unknown);
    }

    private static int countChanges(List<ApplicationInfo> apps) {
        int result = 0;
        for (ApplicationInfo app : apps) {
            Set<Integer> countedTypes = new HashSet<>();
            for (PermissionState permissionState : app.permissions) {
                if (permissionState.hasChanged) {
                    int type = PermissionsUtils.getType(permissionState.permission);
                    if (countedTypes.add(type)) result++;
                }
            }
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static String createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence channelName = context.getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel(NOTICIATION_CHANNEL_ID, channelName, importance);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
            return NOTICIATION_CHANNEL_ID;
        } else {
            return "";
        }
    }

    private static void notifyEverythingAlright(Context context) {
        String notificationTitle = context.getString(R.string.app_name);
        Intent intent = MainActivityView.starter(context);
        String notificationTextLong = context.getString(R.string.notification_alright_long);
        String notificationTextShort = context.getString(R.string.notification_alright_short);
        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(context, createNotificationChannel(context));
        } else {
            notificationBuilder = new NotificationCompat.Builder(context, "");
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);

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
            Timber.e(NOTIFICATION_ERROR);
            FirebaseCrash.report(new Exception(NOTIFICATION_ERROR));
        }
    }

    public static void cancelAll(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        } else {
            Timber.e(NOTIFICATION_ERROR);
            FirebaseCrash.report(new Exception(NOTIFICATION_ERROR));
        }
    }

}
