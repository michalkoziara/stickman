package com.litkaps.stickman.preference;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.litkaps.stickman.R;

import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Configures live preview demo settings.
 */
public class LivePreviewPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Schedulers.io().createWorker().schedule(
                () -> {
                    addPreferencesFromResource(R.xml.preference_live_preview_quickstart);
                    setUpCameraXTargetAnalysisSizePreference();
                }
        );
    }

    private void setUpCameraXTargetAnalysisSizePreference() {
        ListPreference pref =
                (ListPreference) findPreference(getString(R.string.pref_key_camera_target_resolution));
        String[] entries =
                new String[]{
                        "960x1280",
                        "480x640",
                        "240x320"
                };

        pref.setEntries(entries);
        pref.setEntryValues(entries);

        pref.setSummary(
                pref.getEntry() == null
                        ? getString(R.string.pref_title_camera_target_resolution_default)
                        : pref.getEntry()
        );
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
