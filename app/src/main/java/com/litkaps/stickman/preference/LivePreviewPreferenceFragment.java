package com.litkaps.stickman.preference;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.litkaps.stickman.R;

/**
 * Configures live preview demo settings.
 */
public class LivePreviewPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_live_preview_quickstart);
        setUpCameraXTargetAnalysisSizePreference();
    }

    private void setUpCameraXTargetAnalysisSizePreference() {
        ListPreference pref =
                (ListPreference) findPreference(getString(R.string.pref_key_camera_target_resolution));
        String[] entries =
                new String[]{
                        "2000x2000",
                        "1600x1600",
                        "1200x1200",
                        "1000x1000",
                        "800x800",
                        "600x600",
                        "400x400",
                        "200x200",
                        "100x100",
                };
        pref.setEntries(entries);
        pref.setEntryValues(entries);
        pref.setSummary(pref.getEntry() == null ? "Default" : pref.getEntry());
        pref.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    String newStringValue = (String) newValue;
                    pref.setSummary(newStringValue);
                    PreferenceUtils.saveString(
                            getActivity(), R.string.pref_key_camera_target_resolution, newStringValue);
                    return true;
                });
    }
}
