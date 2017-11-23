package io.github.nfdz.permissionswatcher.common.utils;


import android.Manifest;
import android.os.Build;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.common.model.PermissionState;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class PermissionsUtils {

    public static final List<String> CALENDAR_GROUP_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR ));
    public static final List<String> CAMERA_GROUP_PERMISSIONS = Collections.unmodifiableList(Collections.singletonList(
            Manifest.permission.CAMERA));
    public static final List<String> CONTACTS_GROUP_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS ));
    public static final List<String> LOCATION_GROUP_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION ));
    public static final List<String> MICROPHONE_GROUP_PERMISSIONS = Collections.unmodifiableList(Collections.singletonList(
            Manifest.permission.RECORD_AUDIO ));

    public static final List<String> PHONE_GROUP_PERMISSIONS = Arrays.asList(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.PROCESS_OUTGOING_CALLS );

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PHONE_GROUP_PERMISSIONS.add(Manifest.permission.READ_PHONE_NUMBERS);
            PHONE_GROUP_PERMISSIONS.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
    }

    public static final List<String> SENSORS_GROUP_PERMISSIONS = Collections.unmodifiableList(Collections.singletonList(
            Manifest.permission.BODY_SENSORS ));

    public static final List<String> SMS_GROUP_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS ));

    public static final List<String> STORAGE_GROUP_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE ));

    @Retention(SOURCE)
    @IntDef({ CALENDAR_TYPE,
            CAMERA_TYPE,
            CONTACTS_TYPE,
            LOCATION_TYPE,
            MICROPHONE_TYPE,
            PHONE_TYPE,
            SENSORS_TYPE,
            SMS_TYPE,
            STORAGE_TYPE,
            UNKNOWN_TYPE })
    public @interface PermissionType {}
    public static final int CALENDAR_TYPE = 0;
    public static final int CAMERA_TYPE = 1;
    public static final int CONTACTS_TYPE = 2;
    public static final int LOCATION_TYPE = 3;
    public static final int MICROPHONE_TYPE = 4;
    public static final int PHONE_TYPE = 5;
    public static final int SENSORS_TYPE = 6;
    public static final int SMS_TYPE = 7;
    public static final int STORAGE_TYPE = 8;
    public static final int UNKNOWN_TYPE = 9;

    public static List<Integer> processPermissions(List<String> permissions) {
        List<Integer> result = new ArrayList<>();
        for (String permission : permissions) {
            result.add(getType(permission));
        }
        return result;
    }

    public static Set<Integer> processAndCompactPermissionStates(List<PermissionState> permissions, boolean onlyGranted) {
        return new HashSet<>(processPermissionStates(permissions, onlyGranted));
    }

    public static List<Integer> processPermissionStates(List<PermissionState> permissions, boolean onlyGranted) {
        List<Integer> result = new ArrayList<>();
        for (PermissionState permissionState : permissions) {
            if (onlyGranted && !permissionState.granted) continue;
            result.add(getType(permissionState.permission));
        }
        return result;
    }

    @PermissionType
    private static int getType(String permission) {
        if (CALENDAR_GROUP_PERMISSIONS.contains(permission)) {
            return CALENDAR_TYPE;
        } else if (CAMERA_GROUP_PERMISSIONS.contains(permission)) {
            return CAMERA_TYPE;
        } else if (CONTACTS_GROUP_PERMISSIONS.contains(permission)) {
            return CONTACTS_TYPE;
        } else if (LOCATION_GROUP_PERMISSIONS.contains(permission)) {
            return LOCATION_TYPE;
        } else if (MICROPHONE_GROUP_PERMISSIONS.contains(permission)) {
            return MICROPHONE_TYPE;
        } else if (PHONE_GROUP_PERMISSIONS.contains(permission)) {
            return PHONE_TYPE;
        } else if (SENSORS_GROUP_PERMISSIONS.contains(permission)) {
            return SENSORS_TYPE;
        } else if (SMS_GROUP_PERMISSIONS.contains(permission)) {
            return SMS_TYPE;
        } else if (STORAGE_GROUP_PERMISSIONS.contains(permission)) {
            return STORAGE_TYPE;
        } else {
            return UNKNOWN_TYPE;
        }
    }
}
