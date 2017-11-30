package io.github.nfdz.permissionswatcher.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import io.github.nfdz.permissionswatcher.R;
import timber.log.Timber;

public class TimePreference extends DialogPreference {

    private String value;

    public static int getHourFromValue(String value) {
        try {
            return Integer.parseInt(value.split(":")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getMinutesFromValue(String value) {
        try {
            return Integer.parseInt(value.split(":")[1]);
        } catch (Exception e) {
            Timber.e(e, "Wrong stored data: %s", value);
            return 0;
        }
    }

    public static String getValueFromHourAndMinutes(int hour, int minutes) {
        return hour + ":" + minutes;
    }

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // init
    }

    public void setValue(String value) {
        this.value = value;
        persistString(value);
        notifyChanged();
    }

    public String getValue() {
        return value;
    }

    @Override
    public CharSequence getSummary() {
        int hour = getHourFromValue(value);
        int minutes = getMinutesFromValue(value);
        String hourString = Integer.toString(hour);
        if (hourString.length() == 1) hourString = "0" + hourString;
        String minutesString = Integer.toString(minutes);
        if (minutesString.length() == 1) minutesString = "0" + minutesString;
        return getContext().getString(R.string.prefs_report_time_summary_format, (hourString + ":" + minutesString));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedString(value) : (String) defaultValue);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_pref_report_time;
    }

}