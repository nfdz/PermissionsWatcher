package io.github.nfdz.permissionswatcher.settings;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.sched.BootReceiver;
import io.github.nfdz.permissionswatcher.sched.SchedUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        handleReportDependency(PreferencesUtils.isReportEnable(getActivity()));
        handleRealTimeDependency(PreferencesUtils.isRealTimeEnable(getActivity()));
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
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.prefs_report_enable_key))) {
            boolean reportEnabled = PreferencesUtils.isReportEnable(getActivity());
            if (reportEnabled) {
                SchedUtils.rescheduleReport(getActivity());
            } else {
                SchedUtils.unscheduleReport(getActivity());
            }
            boolean realTimeEnabled = PreferencesUtils.isRealTimeEnable(getActivity());
            updateBootReceiver(reportEnabled, realTimeEnabled);
            handleReportDependency(reportEnabled);
        } else if (key.equals(getString(R.string.prefs_report_time_key))) {
            SchedUtils.rescheduleReport(getActivity());
        } else if (key.equals(getString(R.string.prefs_real_time_enable_key))) {
            boolean realTimeEnabled = PreferencesUtils.isRealTimeEnable(getActivity());
            if (realTimeEnabled) {
                SchedUtils.rescheduleRealmTime(getActivity());
            } else {
                SchedUtils.unscheduleRealTime(getActivity());
            }
            boolean reportEnabled = PreferencesUtils.isReportEnable(getActivity());
            updateBootReceiver(reportEnabled, realTimeEnabled);
            handleRealTimeDependency(realTimeEnabled);
        }
    }

    private void handleReportDependency(boolean state) {
        Preference preference = findPreference(getString(R.string.prefs_real_time_enable_key));
        preference.setEnabled(!state);
    }

    private void handleRealTimeDependency(boolean state) {
        Preference preference = findPreference(getString(R.string.prefs_report_enable_key));
        preference.setEnabled(!state);
    }

    private void updateBootReceiver(boolean reportEnabled, boolean realTimeEnabled) {
        boolean anyService = realTimeEnabled || reportEnabled;
        boolean noService = !realTimeEnabled && !reportEnabled;
        if (anyService) {
            enableBootReceiver();
        } else if (noService) {
            disableBootReceiver();
        }
    }

    private void enableBootReceiver() {
        ComponentName receiver = new ComponentName(getActivity(), BootReceiver.class);
        PackageManager pm = getActivity().getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableBootReceiver() {
        ComponentName receiver = new ComponentName(getActivity(), BootReceiver.class);
        PackageManager pm = getActivity().getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
