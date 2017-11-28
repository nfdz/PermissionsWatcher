package io.github.nfdz.permissionswatcher.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TimePicker;

import io.github.nfdz.permissionswatcher.R;
import timber.log.Timber;

public class TimePreference extends DialogPreference {

    private TimePicker timePicker;
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

    public TimePreference(Context context) {
        super(context);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        timePicker = new TimePicker(getContext());
        timePicker.setLayoutParams(layoutParams);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.prefs_time_picker_padding);
        timePicker.setPadding(padding, padding, padding, padding);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(timePicker);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        timePicker.setHour(getHourFromValue(value));
        timePicker.setMinute(getMinutesFromValue(value));
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            timePicker.clearFocus();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String newValue = getValueFromHourAndMinutes(hour, minute);

            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    public void setValue(String value) {
        this.value = value;
        persistString(value);
        notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
        return value;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedString(value) : (String) defaultValue);
    }

    private static String getValueFromHourAndMinutes(int hour, int minutes) {
        return hour + ":" + minutes;
    }

}