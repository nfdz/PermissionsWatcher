package io.github.nfdz.permissionswatcher.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.sched.SchedUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.prefs_notifications_enable_key))) {
            SchedUtils.rescheduleAlarm(getActivity());
        } else if (key.equals(getString(R.string.prefs_notifications_time_key))) {
            SchedUtils.rescheduleAlarm(getActivity());
        }
    }
}
