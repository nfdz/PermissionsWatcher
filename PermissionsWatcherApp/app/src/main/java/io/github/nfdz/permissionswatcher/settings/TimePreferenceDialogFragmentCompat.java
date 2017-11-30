package io.github.nfdz.permissionswatcher.settings;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import io.github.nfdz.permissionswatcher.R;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    private TimePicker timePicker;
    private TimePreference timePreference;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        timePicker = view.findViewById(R.id.time_picker);
        if (timePicker == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'time_picker'");
        }
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            timePreference = ((TimePreference) preference);
            String value = timePreference.getValue();
            timePicker.setHour(TimePreference.getHourFromValue(value));
            timePicker.setMinute(TimePreference.getMinutesFromValue(value));
            timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        } else {
            throw new IllegalStateException("Dialog view must be used by TimePreference");
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            timePicker.clearFocus();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String newValue = TimePreference.getValueFromHourAndMinutes(hour, minute);

            if (timePreference.callChangeListener(newValue)) {
                timePreference.setValue(newValue);
            }
        }
    }
}
