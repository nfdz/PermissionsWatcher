package io.github.nfdz.permissionswatcher.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.permissionswatcher.R;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @BindView(R.id.time_picker_hour_value) EditText hourEditText;
    @BindView(R.id.time_picker_minutes_value) EditText minutesEditText;

    private TimePreference timePreference;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) dialogWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.windowBackground)));
        return dialog;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ButterKnife.bind(this, view);
        setListeners();
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            timePreference = ((TimePreference) preference);
            String value = timePreference.getValue();
            hourEditText.append(Integer.toString(TimePreference.getHourFromValue(value)));
            minutesEditText.append(Integer.toString(TimePreference.getMinutesFromValue(value)));
        } else {
            throw new IllegalStateException("Dialog view must be used by TimePreference");
        }

    }

    private void setListeners() {
        hourEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int hour = Integer.parseInt(s.toString());
                    if (validHour(hour)) {
                        removeLeadingZeros(s);
                        return;
                    }
                } catch (Exception ex) {
                    // swallow
                }
                s.replace(0, s.length(), "0");
            }
        });
        minutesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int minutes = Integer.parseInt(s.toString());
                    if (validMinutes(minutes)) {
                        removeLeadingZeros(s);
                        return;
                    }
                } catch (Exception ex) {
                    // swallow
                }
                s.replace(0, s.length(), "0");
            }
        });
    }

    private void removeLeadingZeros(Editable s) {
        String text = s.toString();
        if (text.length() > 1) {
            String processedText = text.replaceFirst("^0+(?!$)", "");
            if (!text.equals(processedText)) {
                s.replace(0, s.length(), processedText);
            }
        }
    }

    private boolean validHour(int hour) {
        return (hour >= 0 && hour <= 23);
    }

    private boolean validMinutes(int minutes) {
        return (minutes >= 0 && minutes <= 59);
    }

    @OnClick(R.id.time_picker_hour_value_decrease)
    public void onHourDecreaseClick() {
        Editable s = hourEditText.getText();
        try {
            int hour = Integer.parseInt(s.toString());
            if (validHour(hour)) {
                hour--;
                if (hour < 0) hour = 23;
                s.replace(0, s.length(), Integer.toString(hour));
                return;
            }
        } catch (Exception ex) {
            // swallow
        }
        s.replace(0, s.length(), "0");
    }

    @OnClick(R.id.time_picker_hour_value_increase)
    public void onHourIncreaseClick() {
        Editable s = hourEditText.getText();
        try {
            int hour = Integer.parseInt(s.toString());
            if (validHour(hour)) {
                hour++;
                if (hour > 23) hour = 0;
                s.replace(0, s.length(), Integer.toString(hour));
                return;
            }
        } catch (Exception ex) {
            // swallow
        }
        s.replace(0, s.length(), "0");
    }

    @OnClick(R.id.time_picker_minutes_value_decrease)
    public void onMinutesDecreaseClick() {
        Editable s = minutesEditText.getText();
        try {
            int minutes = Integer.parseInt(s.toString());
            if (validMinutes(minutes)) {
                minutes--;
                if (minutes < 0) minutes = 59;
                s.replace(0, s.length(), Integer.toString(minutes));
                return;
            }
        } catch (Exception ex) {
            // swallow
        }
        s.replace(0, s.length(), "0");
    }

    @OnClick(R.id.time_picker_minutes_value_increase)
    public void onMinutesIncreaseClick() {
        Editable s = minutesEditText.getText();
        try {
            int minutes = Integer.parseInt(s.toString());
            if (validMinutes(minutes)) {
                minutes++;
                if (minutes > 59) minutes = 0;
                s.replace(0, s.length(), Integer.toString(minutes));
                return;
            }
        } catch (Exception ex) {
            // swallow
        }
        s.replace(0, s.length(), "0");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            hourEditText.clearFocus();
            minutesEditText.clearFocus();
            int hour = Integer.parseInt(hourEditText.getText().toString());
            int minutes = Integer.parseInt(minutesEditText.getText().toString());
            String newValue = TimePreference.getValueFromHourAndMinutes(hour, minutes);
            if (validHour(hour) && validMinutes(minutes) && timePreference.callChangeListener(newValue)) {
                timePreference.setValue(newValue);
            }
        }
    }
}
